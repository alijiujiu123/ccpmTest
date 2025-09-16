package com.cvagent.service;

import com.cvagent.model.OptimizationRule;
import com.cvagent.model.RuleVersion;
import com.cvagent.repository.OptimizationRuleRepository;
import com.cvagent.repository.RuleVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 规则版本控制服务
 * 管理规则的历史版本和变更记录
 */
@Service
public class RuleVersionControlService {

    private static final Logger logger = LoggerFactory.getLogger(RuleVersionControlService.class);

    @Autowired
    private RuleVersionRepository versionRepository;

    @Autowired
    private OptimizationRuleRepository ruleRepository;

    /**
     * 创建规则新版本
     */
    @Transactional
    public RuleVersion createNewVersion(OptimizationRule updatedRule, String changeReason, String changedBy) {
        logger.info("创建规则新版本: {}, 修改人: {}", updatedRule.getName(), changedBy);

        try {
            // 获取当前最新版本号
            Integer latestVersion = getLatestVersionNumber(updatedRule.getId());
            Integer newVersion = latestVersion + 1;

            // 创建新版本记录
            RuleVersion version = new RuleVersion(updatedRule, newVersion, changeReason, changedBy);

            // 保存版本记录
            RuleVersion savedVersion = versionRepository.save(version);

            // 标记前一个版本为过期
            if (latestVersion > 0) {
                expirePreviousVersion(updatedRule.getId(), latestVersion);
            }

            logger.info("规则版本创建成功: {}, 版本号: {}", updatedRule.getName(), newVersion);
            return savedVersion;

        } catch (Exception e) {
            logger.error("创建规则版本失败: {}", updatedRule.getName(), e);
            throw new RuntimeException("创建规则版本失败", e);
        }
    }

    /**
     * 获取规则版本历史
     */
    @Cacheable(value = "ruleVersionHistory", key = "#ruleId")
    public List<RuleVersion> getVersionHistory(String ruleId) {
        logger.info("获取规则版本历史: {}", ruleId);

        try {
            List<RuleVersion> versions = versionRepository.findByRuleIdOrderByVersionDesc(ruleId);
            logger.info("找到规则 {} 的 {} 个版本", ruleId, versions.size());
            return versions;
        } catch (Exception e) {
            logger.error("获取规则版本历史失败: {}", ruleId, e);
            throw new RuntimeException("获取规则版本历史失败", e);
        }
    }

    /**
     * 获取特定版本
     */
    @Cacheable(value = "ruleVersion", key = "#ruleId + '_' + #version")
    public RuleVersion getVersion(String ruleId, Integer version) {
        logger.info("获取规则特定版本: {}, 版本: {}", ruleId, version);

        try {
            RuleVersion ruleVersion = versionRepository.findByRuleIdAndVersion(ruleId, version);
            if (ruleVersion == null) {
                throw new RuntimeException("规则版本不存在: " + ruleId + ", v" + version);
            }
            return ruleVersion;
        } catch (Exception e) {
            logger.error("获取规则特定版本失败: {}, 版本: {}", ruleId, version, e);
            throw new RuntimeException("获取规则版本失败", e);
        }
    }

    /**
     * 恢复到特定版本
     */
    @Transactional
    public OptimizationRule restoreToVersion(String ruleId, Integer version, String restoredBy) {
        logger.info("恢复规则到特定版本: {}, 版本: {}, 恢复人: {}", ruleId, version, restoredBy);

        try {
            // 获取要恢复的版本
            RuleVersion targetVersion = getVersion(ruleId, version);

            // 获取当前规则
            OptimizationRule currentRule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("规则不存在: " + ruleId));

            // 备份当前状态
            createNewVersion(currentRule, "恢复到版本 " + version, restoredBy);

            // 恢复规则内容
            currentRule.setPattern(targetVersion.getPattern());
            currentRule.setSuggestion(targetVersion.getSuggestion());
            currentRule.setDescription(targetVersion.getDescription());
            currentRule.setCategory(targetVersion.getCategory());
            currentRule.setPriority(targetVersion.getPriority());
            currentRule.setTargetSection(targetVersion.getTargetSection());
            currentRule.setIsActive(targetVersion.getIsActive());
            currentRule.setUpdatedAt(LocalDateTime.now());

            // 保存恢复后的规则
            OptimizationRule restoredRule = ruleRepository.save(currentRule);

            logger.info("规则恢复成功: {}, 版本: {}", ruleId, version);
            return restoredRule;

        } catch (Exception e) {
            logger.error("恢复规则版本失败: {}, 版本: {}", ruleId, version, e);
            throw new RuntimeException("恢复规则版本失败", e);
        }
    }

    /**
     * 获取版本比较
     */
    public VersionComparison compareVersions(String ruleId, Integer version1, Integer version2) {
        logger.info("比较规则版本: {}, 版本: {} vs {}", ruleId, version1, version2);

        try {
            RuleVersion v1 = getVersion(ruleId, version1);
            RuleVersion v2 = getVersion(ruleId, version2);

            VersionComparison comparison = new VersionComparison();
            comparison.setRuleId(ruleId);
            comparison.setVersion1(version1);
            comparison.setVersion2(version2);
            comparison.setPatternChanged(!v1.getPattern().equals(v2.getPattern()));
            comparison.setSuggestionChanged(!v1.getSuggestion().equals(v2.getSuggestion()));
            comparison.setDescriptionChanged(!v1.getDescription().equals(v2.getDescription()));
            comparison.setCategoryChanged(!v1.getCategory().equals(v2.getCategory()));
            comparison.setPriorityChanged(!v1.getPriority().equals(v2.getPriority()));
            comparison.setTargetSectionChanged(!v1.getTargetSection().equals(v2.getTargetSection()));
            comparison.setActiveChanged(!v1.getIsActive().equals(v2.getIsActive()));

            return comparison;

        } catch (Exception e) {
            logger.error("比较规则版本失败: {}, 版本: {} vs {}", ruleId, version1, version2, e);
            throw new RuntimeException("比较规则版本失败", e);
        }
    }

    /**
     * 获取版本统计信息
     */
    @Cacheable(value = "ruleVersionStats", key = "#ruleId")
    public VersionStatistics getVersionStatistics(String ruleId) {
        logger.info("获取规则版本统计: {}", ruleId);

        try {
            List<RuleVersion> versions = getVersionHistory(ruleId);
            VersionStatistics stats = new VersionStatistics();
            stats.setRuleId(ruleId);
            stats.setTotalVersions(versions.size());
            stats.setLatestVersion(versions.isEmpty() ? 0 : versions.get(0).getVersion());

            // 计算修改次数
            stats.setModificationCount(versions.size() - 1);

            // 计算平均修改间隔
            if (versions.size() > 1) {
                long totalDays = 0;
                for (int i = 0; i < versions.size() - 1; i++) {
                    LocalDateTime time1 = versions.get(i).getCreatedAt();
                    LocalDateTime time2 = versions.get(i + 1).getCreatedAt();
                    totalDays += java.time.Duration.between(time2, time1).toDays();
                }
                stats.setAverageModificationIntervalDays(totalDays / (versions.size() - 1));
            }

            // 获取最新修改时间
            stats.setLastModifiedAt(versions.isEmpty() ? null : versions.get(0).getCreatedAt());

            return stats;

        } catch (Exception e) {
            logger.error("获取规则版本统计失败: {}", ruleId, e);
            throw new RuntimeException("获取规则版本统计失败", e);
        }
    }

    /**
     * 清理过期版本
     */
    @Transactional
    public void cleanupExpiredVersions(int keepVersions) {
        logger.info("清理过期版本，保留最新 {} 个版本", keepVersions);

        try {
            // 获取所有规则ID
            List<String> ruleIds = ruleRepository.findAllRuleIds();

            int totalCleaned = 0;
            for (String ruleId : ruleIds) {
                List<RuleVersion> versions = versionRepository.findByRuleIdOrderByVersionDesc(ruleId);

                // 如果版本数量超过保留数量，删除多余版本
                if (versions.size() > keepVersions) {
                    List<RuleVersion> toDelete = versions.subList(keepVersions, versions.size());
                    for (RuleVersion version : toDelete) {
                        versionRepository.delete(version);
                        totalCleaned++;
                    }
                }
            }

            logger.info("清理完成，共删除 {} 个过期版本", totalCleaned);

        } catch (Exception e) {
            logger.error("清理过期版本失败", e);
            throw new RuntimeException("清理过期版本失败", e);
        }
    }

    /**
     * 获取最新版本号
     */
    private Integer getLatestVersionNumber(String ruleId) {
        RuleVersion latestVersion = versionRepository.findLatestVersionByRuleId(ruleId);
        return latestVersion != null ? latestVersion.getVersion() : 0;
    }

    /**
     * 标记前一个版本为过期
     */
    private void expirePreviousVersion(String ruleId, Integer version) {
        try {
            RuleVersion previousVersion = versionRepository.findByRuleIdAndVersion(ruleId, version);
            if (previousVersion != null) {
                previousVersion.setExpiresAt(LocalDateTime.now());
                versionRepository.save(previousVersion);
            }
        } catch (Exception e) {
            logger.warn("标记前一个版本过期失败: {}, 版本: {}", ruleId, version, e);
        }
    }

    // 内部类定义
    public static class VersionComparison {
        private String ruleId;
        private Integer version1;
        private Integer version2;
        private boolean patternChanged;
        private boolean suggestionChanged;
        private boolean descriptionChanged;
        private boolean categoryChanged;
        private boolean priorityChanged;
        private boolean targetSectionChanged;
        private boolean activeChanged;

        // Getters and Setters
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public Integer getVersion1() { return version1; }
        public void setVersion1(Integer version1) { this.version1 = version1; }
        public Integer getVersion2() { return version2; }
        public void setVersion2(Integer version2) { this.version2 = version2; }
        public boolean isPatternChanged() { return patternChanged; }
        public void setPatternChanged(boolean patternChanged) { this.patternChanged = patternChanged; }
        public boolean isSuggestionChanged() { return suggestionChanged; }
        public void setSuggestionChanged(boolean suggestionChanged) { this.suggestionChanged = suggestionChanged; }
        public boolean isDescriptionChanged() { return descriptionChanged; }
        public void setDescriptionChanged(boolean descriptionChanged) { this.descriptionChanged = descriptionChanged; }
        public boolean isCategoryChanged() { return categoryChanged; }
        public void setCategoryChanged(boolean categoryChanged) { this.categoryChanged = categoryChanged; }
        public boolean isPriorityChanged() { return priorityChanged; }
        public void setPriorityChanged(boolean priorityChanged) { this.priorityChanged = priorityChanged; }
        public boolean isTargetSectionChanged() { return targetSectionChanged; }
        public void setTargetSectionChanged(boolean targetSectionChanged) { this.targetSectionChanged = targetSectionChanged; }
        public boolean isActiveChanged() { return activeChanged; }
        public void setActiveChanged(boolean activeChanged) { this.activeChanged = activeChanged; }
    }

    public static class VersionStatistics {
        private String ruleId;
        private int totalVersions;
        private int latestVersion;
        private int modificationCount;
        private double averageModificationIntervalDays;
        private LocalDateTime lastModifiedAt;

        // Getters and Setters
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public int getTotalVersions() { return totalVersions; }
        public void setTotalVersions(int totalVersions) { this.totalVersions = totalVersions; }
        public int getLatestVersion() { return latestVersion; }
        public void setLatestVersion(int latestVersion) { this.latestVersion = latestVersion; }
        public int getModificationCount() { return modificationCount; }
        public void setModificationCount(int modificationCount) { this.modificationCount = modificationCount; }
        public double getAverageModificationIntervalDays() { return averageModificationIntervalDays; }
        public void setAverageModificationIntervalDays(double averageModificationIntervalDays) { this.averageModificationIntervalDays = averageModificationIntervalDays; }
        public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
        public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
    }
}