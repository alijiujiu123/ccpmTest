package com.cvagent.service;

import com.cvagent.model.OptimizationRule;
import com.cvagent.repository.OptimizationRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 简历优化规则引擎服务
 * 管理和应用简历优化规则
 */
@Service
public class RuleEngineService {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineService.class);

    @Autowired
    private OptimizationRuleRepository ruleRepository;

    @Autowired
    private AiServiceManager aiServiceManager;

    @Autowired
    private RuleVersionControlService versionControlService;

    /**
     * 应用所有规则到简历内容
     */
    public List<OptimizationResult> applyAllRules(String resumeContent, String targetSection) {
        logger.info("开始应用优化规则到简历内容");

        List<OptimizationRule> applicableRules = getApplicableRules(targetSection);
        List<OptimizationResult> results = new ArrayList<>();

        for (OptimizationRule rule : applicableRules) {
            OptimizationResult result = applyRule(rule, resumeContent);
            if (result.hasMatches()) {
                results.add(result);
            }
        }

        logger.info("规则应用完成，共发现 {} 个优化建议", results.size());
        return results;
    }

    /**
     * 应用单个规则
     */
    public OptimizationResult applyRule(OptimizationRule rule, String content) {
        OptimizationResult result = new OptimizationResult();
        result.setRule(rule);
        result.setAppliedAt(LocalDateTime.now());

        if (rule.getPattern() != null && !rule.getPattern().trim().isEmpty()) {
            try {
                Pattern pattern = Pattern.compile(rule.getPattern(), Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(content);

                List<String> matches = new ArrayList<>();
                while (matcher.find()) {
                    matches.add(matcher.group());
                }

                result.setMatches(matches);
                result.setMatchCount(matches.size());

                if (matches.size() > 0) {
                    // 使用AI进行智能优化建议
                    String optimizedSuggestion = generateAIOptimization(rule, content, matches);
                    result.setOptimizedSuggestion(optimizedSuggestion);
                }

            } catch (Exception e) {
                logger.error("应用规则时出错: {}", rule.getName(), e);
                result.setError("规则模式无效: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 使用AI生成优化建议
     */
    private String generateAIOptimization(OptimizationRule rule, String content, List<String> matches) {
        try {
            String context = String.format(
                "简历优化规则：'%s'\n类别：%s\n目标区域：%s\n描述：%s\n\n匹配到的问题：%s\n\n原始内容：%s",
                rule.getName(),
                rule.getCategory(),
                rule.getTargetSection(),
                rule.getDescription(),
                String.join(", ", matches),
                content.substring(0, Math.min(content.length(), 500))
            );

            return aiServiceManager.improveResumeSection(context, rule.getCategory());
        } catch (Exception e) {
            logger.warn("AI优化生成失败，使用默认建议: {}", e.getMessage());
            return rule.getSuggestion() != null ? rule.getSuggestion() : "建议优化此部分内容";
        }
    }

    /**
     * 获取适用的规则
     */
    @Cacheable(value = "applicableRules", key = "#targetSection")
    public List<OptimizationRule> getApplicableRules(String targetSection) {
        List<OptimizationRule> rules;

        if ("ALL".equals(targetSection) || targetSection == null) {
            rules = ruleRepository.findByIsActiveTrueOrderByPriorityDesc();
        } else {
            rules = ruleRepository.findByTargetSectionAndIsActiveTrueOrderByPriorityDesc(targetSection);
        }

        return rules.stream()
                .filter(rule -> rule.getPattern() != null && !rule.getPattern().trim().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 创建新的优化规则
     */
    public OptimizationRule createRule(OptimizationRule rule) {
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());

        if (rule.getPriority() == null) {
            rule.setPriority(3);
        }
        if (rule.getIsActive() == null) {
            rule.setIsActive(true);
        }

        OptimizationRule savedRule = ruleRepository.save(rule);
        logger.info("创建新的优化规则: {}", rule.getName());

        return savedRule;
    }

    /**
     * 更新规则
     */
    public OptimizationRule updateRule(String id, OptimizationRule ruleDetails) {
        OptimizationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("规则不存在: " + id));

        // 创建版本记录
        try {
            versionControlService.createNewVersion(rule, "更新规则配置", "system");
        } catch (Exception e) {
            logger.warn("创建规则版本记录失败: {}", e.getMessage());
        }

        rule.setName(ruleDetails.getName());
        rule.setDescription(ruleDetails.getDescription());
        rule.setCategory(ruleDetails.getCategory());
        rule.setPattern(ruleDetails.getPattern());
        rule.setSuggestion(ruleDetails.getSuggestion());
        rule.setPriority(ruleDetails.getPriority());
        rule.setIsActive(ruleDetails.getIsActive());
        rule.setTargetSection(ruleDetails.getTargetSection());
        rule.setUpdatedAt(LocalDateTime.now());

        OptimizationRule updatedRule = ruleRepository.save(rule);
        logger.info("更新优化规则: {}", rule.getName());

        return updatedRule;
    }

    /**
     * 删除规则
     */
    public void deleteRule(String id) {
        OptimizationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("规则不存在: " + id));

        ruleRepository.delete(rule);
        logger.info("删除优化规则: {}", rule.getName());
    }

    /**
     * 激活或停用规则
     */
    public void toggleRuleStatus(String id, boolean isActive) {
        OptimizationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("规则不存在: " + id));

        rule.setIsActive(isActive);
        rule.setUpdatedAt(LocalDateTime.now());
        ruleRepository.save(rule);

        logger.info("规则状态更新: {} -> {}", id, isActive ? "激活" : "停用");
    }

    /**
     * 获取所有规则
     */
    public List<OptimizationRule> getAllRules() {
        return ruleRepository.findAll(Sort.by(Sort.Direction.DESC, "priority"));
    }

    /**
     * 获取活跃规则
     */
    public List<OptimizationRule> getActiveRules() {
        return ruleRepository.findByIsActiveTrueOrderByPriorityDesc();
    }

    /**
     * 按类别获取规则
     */
    public List<OptimizationRule> getRulesByCategory(String category) {
        return ruleRepository.findByCategoryAndIsActiveTrue(category);
    }

    /**
     * 搜索规则
     */
    public List<OptimizationRule> searchRules(String keyword) {
        return ruleRepository.searchByName(keyword);
    }

    /**
     * 获取所有规则类别
     */
    public Set<String> getAllCategories() {
        return ruleRepository.findAllCategories().stream()
                .map(OptimizationRule::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 批量应用规则
     */
    public BatchOptimizationResult batchApplyRules(String resumeContent) {
        BatchOptimizationResult batchResult = new BatchOptimizationResult();
        batchResult.setProcessedAt(LocalDateTime.now());

        // 分别处理各个区域
        for (String section : Arrays.asList("SUMMARY", "SKILLS", "EXPERIENCE", "EDUCATION")) {
            List<OptimizationResult> sectionResults = applyAllRules(resumeContent, section);
            batchResult.addSectionResults(section, sectionResults);
        }

        // 应用通用规则
        List<OptimizationResult> generalResults = applyAllRules(resumeContent, "ALL");
        batchResult.addSectionResults("GENERAL", generalResults);

        batchResult.calculateSummary();
        return batchResult;
    }

    /**
     * 优化结果类
     */
    public static class OptimizationResult {
        private OptimizationRule rule;
        private List<String> matches;
        private int matchCount;
        private String optimizedSuggestion;
        private String error;
        private LocalDateTime appliedAt;

        public boolean hasMatches() {
            return matchCount > 0;
        }

        // Getters and Setters
        public OptimizationRule getRule() { return rule; }
        public void setRule(OptimizationRule rule) { this.rule = rule; }

        public List<String> getMatches() { return matches; }
        public void setMatches(List<String> matches) { this.matches = matches; }

        public int getMatchCount() { return matchCount; }
        public void setMatchCount(int matchCount) { this.matchCount = matchCount; }

        public String getOptimizedSuggestion() { return optimizedSuggestion; }
        public void setOptimizedSuggestion(String optimizedSuggestion) { this.optimizedSuggestion = optimizedSuggestion; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public LocalDateTime getAppliedAt() { return appliedAt; }
        public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
    }

    /**
     * 批量优化结果类
     */
    public static class BatchOptimizationResult {
        private Map<String, List<OptimizationResult>> sectionResults = new HashMap<>();
        private LocalDateTime processedAt;
        private int totalMatches;
        private int totalRulesApplied;

        public void addSectionResults(String section, List<OptimizationResult> results) {
            sectionResults.put(section, results);
        }

        public void calculateSummary() {
            totalMatches = sectionResults.values().stream()
                    .flatMap(List::stream)
                    .mapToInt(OptimizationResult::getMatchCount)
                    .sum();
            totalRulesApplied = sectionResults.values().stream()
                    .mapToInt(List::size)
                    .sum();
        }

        // Getters
        public Map<String, List<OptimizationResult>> getSectionResults() { return sectionResults; }
        public LocalDateTime getProcessedAt() { return processedAt; }
        public int getTotalMatches() { return totalMatches; }
        public int getTotalRulesApplied() { return totalRulesApplied; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    }
}