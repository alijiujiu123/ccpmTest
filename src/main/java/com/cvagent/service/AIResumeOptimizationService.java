package com.cvagent.service;

import com.cvagent.model.EnhancedResume;
import com.cvagent.model.JobRequirement;
import com.cvagent.repository.EnhancedResumeRepository;
import com.cvagent.repository.JobRequirementRepository;
// import dev.langchain4j.model.chat.ChatLanguageModel; // 临时注释掉
// import dev.langchain4j.model.input.Prompt;
// import dev.langchain4j.model.input.PromptTemplate;
// import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AI简历优化服务
 * 使用LangChain4j和AI模型对简历内容进行智能优化
 */
@Service
@Transactional
public class AIResumeOptimizationService {

    @Autowired
    private EnhancedResumeRepository enhancedResumeRepository;

    @Autowired
    private JobRequirementRepository jobRequirementRepository;

    // @Autowired
    // private ChatLanguageModel chatLanguageModel;

    /**
     * 优化简历摘要
     */
    public String optimizeSummary(String originalSummary, String jobDescription) {
        // 简化的优化逻辑，实际应用中应该调用AI服务
        return "优化后的摘要：" + originalSummary + "（基于招聘要求：" + jobDescription + "）";
    }

    /**
     * 优化工作经验描述
     */
    public String optimizeExperience(String originalExperience, String jobRequirements) {
        // 简化的优化逻辑
        return "优化后的工作经验：" + originalExperience + "（匹配技能要求：" + jobRequirements + "）";
    }

    /**
     * 优化技能描述
     */
    public String optimizeSkills(String originalSkills, String requiredSkills) {
        // 简化的优化逻辑
        return "优化后的技能：" + originalSkills + "（重点突出：" + requiredSkills + "）";
    }

    /**
     * 全面优化简历
     */
    public EnhancedResume optimizeResume(String resumeId, String jobRequirementId) {
        EnhancedResume resume = enhancedResumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("简历不存在"));

        JobRequirement jobRequirement = jobRequirementRepository.findById(jobRequirementId)
                .orElseThrow(() -> new RuntimeException("招聘需求不存在"));

        // 创建优化后的简历副本
        EnhancedResume optimizedResume = createOptimizedCopy(resume);
        optimizedResume.setJobRequirementId(jobRequirementId);
        optimizedResume.setOptimizationStatus("processing");

        // 异步优化各个部分
        optimizeResumeSectionsAsync(optimizedResume, jobRequirement);

        return enhancedResumeRepository.save(optimizedResume);
    }

    /**
     * 异步优化简历各个部分
     */
    private void optimizeResumeSectionsAsync(EnhancedResume resume, JobRequirement jobRequirement) {
        new Thread(() -> {
            try {
                // 优化个人信息摘要
                if (resume.getPersonalInfo() != null && resume.getPersonalInfo().getSummary() != null) {
                    String optimizedSummary = optimizeSummary(
                        resume.getPersonalInfo().getSummary(),
                        jobRequirement.getDescription()
                    );
                    resume.getPersonalInfo().setSummary(optimizedSummary);
                }

                // 优化工作经验
                if (resume.getWorkExperience() != null) {
                    // 这里需要根据实际的工作经验数据结构来优化
                    // 暂时提供一个示例
                }

                // 优化技能
                if (resume.getSkills() != null) {
                    // 这里需要根据实际的技能数据结构来优化
                    // 暂时提供一个示例
                }

                // 计算匹配度
                double matchScore = calculateAdvancedMatchScore(resume, jobRequirement);
                resume.setMatchScore(matchScore);

                // 生成优化报告
                String optimizationReport = generateOptimizationReport(resume, jobRequirement);
                resume.setOptimizationReport(optimizationReport);

                // 更新状态
                resume.setOptimizationStatus("completed");
                resume.setOptimizedAt(LocalDateTime.now());

                // 设置优化指标
                resume.setOptimizationMetrics(Map.of(
                        "keywordMatch", matchScore * 0.4,
                        "skillRelevance", matchScore * 0.3,
                        "experienceMatch", matchScore * 0.2,
                        "formatScore", matchScore * 0.1,
                        "optimizationTime", System.currentTimeMillis()
                ));

                enhancedResumeRepository.save(resume);
            } catch (Exception e) {
                resume.setOptimizationStatus("failed");
                resume.setOptimizationReport("优化失败: " + e.getMessage());
                enhancedResumeRepository.save(resume);
            }
        }).start();
    }

    /**
     * 创建优化后的简历副本
     */
    private EnhancedResume createOptimizedCopy(EnhancedResume original) {
        EnhancedResume copy = new EnhancedResume();
        copy.setUserId(original.getUserId());
        copy.setBaseResumeId(original.getId());
        copy.setTitle(original.getTitle() + " - AI优化版");
        copy.setGeneratedBy("ai_optimized");
        copy.setVersion(generateNextVersion(original.getVersion()));
        copy.setOptimizationStatus("processing");
        copy.setIsPublic(false);

        // 复制原始数据
        copy.setPersonalInfo(original.getPersonalInfo());
        copy.setWorkExperience(original.getWorkExperience());
        copy.setEducation(original.getEducation());
        copy.setSkills(original.getSkills());
        copy.setProjects(original.getProjects());

        return copy;
    }

    /**
     * 计算高级匹配度
     */
    private double calculateAdvancedMatchScore(EnhancedResume resume, JobRequirement jobRequirement) {
        double score = 0.0;

        // 关键词匹配度
        double keywordScore = calculateKeywordMatchScore(resume, jobRequirement);
        score += keywordScore * 0.3;

        // 技能匹配度
        double skillScore = calculateSkillMatchScore(resume, jobRequirement);
        score += skillScore * 0.3;

        // 经验匹配度
        double experienceScore = calculateExperienceMatchScore(resume, jobRequirement);
        score += experienceScore * 0.2;

        // 教育背景匹配度
        double educationScore = calculateEducationMatchScore(resume, jobRequirement);
        score += educationScore * 0.1;

        // 格式和结构评分
        double formatScore = calculateFormatScore(resume);
        score += formatScore * 0.1;

        return Math.min(score, 1.0);
    }

    /**
     * 计算关键词匹配度
     */
    private double calculateKeywordMatchScore(EnhancedResume resume, JobRequirement jobRequirement) {
        if (jobRequirement.getParsedKeywords() == null) return 0.0;

        String[] keywords = jobRequirement.getParsedKeywords().split(",");
        int matchCount = 0;

        String resumeText = resume.getTextContent();
        for (String keyword : keywords) {
            if (resumeText.toLowerCase().contains(keyword.toLowerCase().trim())) {
                matchCount++;
            }
        }

        return keywords.length > 0 ? (double) matchCount / keywords.length : 0.0;
    }

    /**
     * 计算技能匹配度
     */
    private double calculateSkillMatchScore(EnhancedResume resume, JobRequirement jobRequirement) {
        if (resume.getSkills() == null || jobRequirement.getSkills() == null) return 0.0;

        long matchingSkills = resume.getSkills().getTechnicalSkills().stream()
                .filter(skill -> jobRequirement.getSkills().contains(skill))
                .count();

        return jobRequirement.getSkills().size() > 0 ?
               (double) matchingSkills / jobRequirement.getSkills().size() : 0.0;
    }

    /**
     * 计算经验匹配度
     */
    private double calculateExperienceMatchScore(EnhancedResume resume, JobRequirement jobRequirement) {
        // 简化的经验匹配逻辑
        // 实际应用中应该考虑工作年限、职位级别、公司规模等因素
        return 0.7; // 示例值
    }

    /**
     * 计算教育背景匹配度
     */
    private double calculateEducationMatchScore(EnhancedResume resume, JobRequirement jobRequirement) {
        // 简化的教育匹配逻辑
        // 实际应用中应该考虑学历、专业、学校等因素
        return 0.8; // 示例值
    }

    /**
     * 计算格式评分
     */
    private double calculateFormatScore(EnhancedResume resume) {
        // 简化的格式评分逻辑
        // 实际应用中应该检查简历结构的完整性和专业性
        return 0.9; // 示例值
    }

    /**
     * 生成优化报告
     */
    private String generateOptimizationReport(EnhancedResume resume, JobRequirement jobRequirement) {
        // 简化的优化报告生成
        return "简历优化报告：\n" +
               "1. 主要优化点：根据招聘要求调整了简历内容\n" +
               "2. 匹配度提升：从基础版本提升至" + (resume.getMatchScore() * 100) + "%\n" +
               "3. 建议进一步改进：增加更多与目标职位相关的技能和经验描述";
    }

    /**
     * 生成下一个版本号
     */
    private String generateNextVersion(String currentVersion) {
        try {
            double version = Double.parseDouble(currentVersion);
            return String.format("%.1f", version + 0.1);
        } catch (NumberFormatException e) {
            return "1.0";
        }
    }
}