package com.cvagent.service;

import com.cvagent.model.CoverLetter;
import com.cvagent.model.CoverLetterTemplate;
import com.cvagent.model.EnhancedResume;
import com.cvagent.model.JobRequirement;
import com.cvagent.repository.CoverLetterRepository;
import com.cvagent.repository.CoverLetterTemplateRepository;
import com.cvagent.repository.EnhancedResumeRepository;
import com.cvagent.repository.JobRequirementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 求职信生成服务
 * 负责生成个性化求职信内容
 */
@Service
@Transactional
public class CoverLetterGenerationService {

    @Autowired
    private CoverLetterRepository coverLetterRepository;

    @Autowired
    private CoverLetterTemplateRepository coverLetterTemplateRepository;

    @Autowired
    private EnhancedResumeRepository enhancedResumeRepository;

    @Autowired
    private JobRequirementRepository jobRequirementRepository;

    @Autowired
    private AiServiceManager aiServiceManager;

    /**
     * 基于模板生成基础求职信
     */
    public CoverLetter generateBasicCoverLetter(String userId, String templateId, Map<String, Object> letterData) {
        // 获取模板
        CoverLetterTemplate template = coverLetterTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在"));

        // 创建求职信
        CoverLetter coverLetter = new CoverLetter();
        coverLetter.setUserId(userId);
        coverLetter.setTemplateId(templateId);
        coverLetter.setTitle((String) letterData.getOrDefault("title", "求职信"));
        coverLetter.setGeneratedBy("template_based");
        coverLetter.setStatus("draft");

        // 设置基本信息
        coverLetter.setCompanyName((String) letterData.get("companyName"));
        coverLetter.setPosition((String) letterData.get("position"));
        coverLetter.setRecipientName((String) letterData.get("recipientName"));
        coverLetter.setRecipientTitle((String) letterData.get("recipientTitle"));

        // 设置语言和风格
        if (template.getLanguageConfig() != null) {
            coverLetter.setLanguage(template.getLanguageConfig().getLanguage());
            coverLetter.setTone(template.getLanguageConfig().getTone());
            coverLetter.setFormalityLevel(template.getLanguageConfig().getFormalityLevel());
        }

        // 生成基础内容
        CoverLetter.CoverLetterContent content = generateContentFromTemplate(template, letterData);
        coverLetter.setContent(content);

        // 计算初始分数
        calculateInitialScores(coverLetter);

        return coverLetterRepository.save(coverLetter);
    }

    /**
     * 基于简历和招聘需求生成个性化求职信
     */
    public CoverLetter generatePersonalizedCoverLetter(String userId, String resumeId, String jobRequirementId,
                                                       String templateId, Map<String, Object> customData) {
        // 获取基础数据
        EnhancedResume resume = enhancedResumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("简历不存在"));

        JobRequirement jobRequirement = jobRequirementRepository.findById(jobRequirementId)
                .orElseThrow(() -> new RuntimeException("招聘需求不存在"));

        CoverLetterTemplate template = coverLetterTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在"));

        // 创建求职信
        CoverLetter coverLetter = new CoverLetter();
        coverLetter.setUserId(userId);
        coverLetter.setResumeId(resumeId);
        coverLetter.setJobRequirementId(jobRequirementId);
        coverLetter.setTemplateId(templateId);
        coverLetter.setTitle(resume.getTitle() + " - " + jobRequirement.getTitle() + "求职信");
        coverLetter.setGeneratedBy("ai_generated");
        coverLetter.setStatus("processing");
        coverLetter.setOptimizationStatus("processing");

        // 设置基本信息
        coverLetter.setCompanyName(jobRequirement.getCompany());
        coverLetter.setPosition(jobRequirement.getTitle());
        coverLetter.setAiOptimized(true);

        // 设置语言和风格
        if (template.getLanguageConfig() != null) {
            coverLetter.setLanguage(template.getLanguageConfig().getLanguage());
            coverLetter.setTone(template.getLanguageConfig().getTone());
            coverLetter.setFormalityLevel(template.getLanguageConfig().getFormalityLevel());
        }

        // 保存到数据库
        coverLetter = coverLetterRepository.save(coverLetter);

        // 异步生成个性化内容
        generatePersonalizedContentAsync(coverLetter, resume, jobRequirement, template, customData);

        return coverLetter;
    }

    /**
     * 优化现有求职信
     */
    public CoverLetter optimizeCoverLetter(String coverLetterId, Map<String, Object> optimizationOptions) {
        CoverLetter coverLetter = coverLetterRepository.findById(coverLetterId)
                .orElseThrow(() -> new RuntimeException("求职信不存在"));

        if (!coverLetter.isEditable()) {
            throw new RuntimeException("求职信不可编辑");
        }

        coverLetter.setOptimizationStatus("processing");
        coverLetter = coverLetterRepository.save(coverLetter);

        // 异步优化内容
        optimizeCoverLetterContentAsync(coverLetter, optimizationOptions);

        return coverLetter;
    }

    /**
     * 定制化求职信内容
     */
    public CoverLetter customizeCoverLetter(String coverLetterId, Map<String, Object> customizations) {
        CoverLetter coverLetter = coverLetterRepository.findById(coverLetterId)
                .orElseThrow(() -> new RuntimeException("求职信不存在"));

        if (!coverLetter.isEditable()) {
            throw new RuntimeException("求职信不可编辑");
        }

        // 应用定制化
        applyCustomizations(coverLetter, customizations);

        // 更新内容
        coverLetter.updateContent();
        coverLetter.setCustomizations(customizations);

        return coverLetterRepository.save(coverLetter);
    }

    /**
     * 复制求职信
     */
    public CoverLetter copyCoverLetter(String coverLetterId, String newTitle) {
        CoverLetter original = coverLetterRepository.findById(coverLetterId)
                .orElseThrow(() -> new RuntimeException("求职信不存在"));

        CoverLetter copy = new CoverLetter();
        copy.setUserId(original.getUserId());
        copy.setTitle(newTitle != null ? newTitle : original.getTitle() + " - 副本");
        copy.setResumeId(original.getResumeId());
        copy.setJobRequirementId(original.getJobRequirementId());
        copy.setTemplateId(original.getTemplateId());
        copy.setCompanyName(original.getCompanyName());
        copy.setPosition(original.getPosition());
        copy.setRecipientName(original.getRecipientName());
        copy.setRecipientTitle(original.getRecipientTitle());
        copy.setGeneratedBy("copied");
        copy.setStatus("draft");

        // 复制内容
        if (original.getContent() != null) {
            CoverLetter.CoverLetterContent content = new CoverLetter.CoverLetterContent();
            content.setSalutation(original.getContent().getSalutation());
            content.setOpeningParagraph(original.getContent().getOpeningParagraph());
            content.setBodyParagraphs(original.getContent().getBodyParagraphs());
            content.setClosingParagraph(original.getContent().getClosingParagraph());
            content.setSignature(original.getContent().getSignature());
            content.setContactInfo(original.getContent().getContactInfo());
            content.setPostscript(original.getContent().getPostscript());
            copy.setContent(content);
        }

        return coverLetterRepository.save(copy);
    }

    /**
     * 获取求职信建议
     */
    public Map<String, Object> getCoverLetterSuggestions(String coverLetterId) {
        CoverLetter coverLetter = coverLetterRepository.findById(coverLetterId)
                .orElseThrow(() -> new RuntimeException("求职信不存在"));

        Map<String, Object> suggestions = new HashMap<>();

        // 内容质量建议
        if (coverLetter.getQualityScore() != null && coverLetter.getQualityScore() < 0.7) {
            suggestions.put("quality_improvement", "建议增加具体的工作成果和数据支撑");
        }

        // 相关性建议
        if (coverLetter.getRelevanceScore() != null && coverLetter.getRelevanceScore() < 0.7) {
            suggestions.put("relevance_improvement", "建议增加与职位要求相关的技能和经验描述");
        }

        // 完整性建议
        if (coverLetter.getCompletenessScore() != null && coverLetter.getCompletenessScore() < 0.7) {
            suggestions.put("completeness_improvement", "建议补充缺失的部分，如具体的项目经验或技能证书");
        }

        // AI优化建议
        if (!coverLetter.getAiOptimized()) {
            suggestions.put("ai_optimization", "建议使用AI优化功能提升求职信质量");
        }

        return suggestions;
    }

    // 私有方法

    /**
     * 基于模板生成内容
     */
    private CoverLetter.CoverLetterContent generateContentFromTemplate(CoverLetterTemplate template, Map<String, Object> letterData) {
        CoverLetter.CoverLetterContent content = new CoverLetter.CoverLetterContent();

        // 使用模板结构生成内容
        if (template.getStructure() != null && template.getStructure().getSections() != null) {
            for (CoverLetterTemplate.Section section : template.getStructure().getSections()) {
                switch (section.getType()) {
                    case "header":
                        content.setSalutation(generateSalutation(letterData));
                        break;
                    case "opening":
                        content.setOpeningParagraph(generateOpeningParagraph(letterData));
                        break;
                    case "body":
                        content.setBodyParagraphs(generateBodyParagraphs(letterData));
                        break;
                    case "closing":
                        content.setClosingParagraph(generateClosingParagraph(letterData));
                        break;
                    case "signature":
                        content.setSignature(generateSignature(letterData));
                        break;
                }
            }
        }

        // 确保所有必要字段都有值
        if (content.getSalutation() == null) {
            content.setSalutation(generateSalutation(letterData));
        }
        if (content.getOpeningParagraph() == null) {
            content.setOpeningParagraph(generateOpeningParagraph(letterData));
        }
        if (content.getBodyParagraphs() == null) {
            content.setBodyParagraphs(generateBodyParagraphs(letterData));
        }
        if (content.getClosingParagraph() == null) {
            content.setClosingParagraph(generateClosingParagraph(letterData));
        }
        if (content.getSignature() == null) {
            content.setSignature(generateSignature(letterData));
        }

        return content;
    }

    /**
     * 异步生成个性化内容
     */
    private void generatePersonalizedContentAsync(CoverLetter coverLetter, EnhancedResume resume,
                                                  JobRequirement jobRequirement, CoverLetterTemplate template,
                                                  Map<String, Object> customData) {
        try {
            // 构建AI提示
            String prompt = buildPersonalizationPrompt(resume, jobRequirement, template, customData);

            // 调用AI服务生成内容
            String aiContent = aiServiceManager.generateCoverLetter(
                extractResumeContent(resume),
                jobRequirement.getDescription(),
                jobRequirement.getCompany()
            );

            // 解析AI生成的内容
            CoverLetter.CoverLetterContent content = parseAIGeneratedContent(aiContent);
            coverLetter.setContent(content);

            // 计算匹配度和质量分数
            double matchScore = calculateMatchScore(resume, jobRequirement);
            coverLetter.setMatchScore(matchScore);

            calculateQualityScores(coverLetter, resume, jobRequirement);

            // 更新状态
            coverLetter.setOptimizationStatus("completed");
            coverLetter.setStatus("ready");
            coverLetter.markAsOptimized();

            coverLetterRepository.save(coverLetter);

        } catch (Exception e) {
            coverLetter.setOptimizationStatus("failed");
            coverLetterRepository.save(coverLetter);
        }
    }

    /**
     * 异步优化求职信内容
     */
    private void optimizeCoverLetterContentAsync(CoverLetter coverLetter, Map<String, Object> options) {
        try {
            // 获取当前内容
            String currentContent = buildContentString(coverLetter.getContent());

            // 构建优化提示
            String optimizationPrompt = buildOptimizationPrompt(currentContent, options);

            // 调用AI服务优化内容
            String optimizedContent = aiServiceManager.improveResumeSection(currentContent, "cover_letter");

            // 解析优化后的内容
            CoverLetter.CoverLetterContent content = parseAIGeneratedContent(optimizedContent);
            coverLetter.setContent(content);

            // 更新分数和状态
            calculateQualityScores(coverLetter, null, null);
            coverLetter.setOptimizationStatus("completed");
            coverLetter.setStatus("ready");
            coverLetter.markAsOptimized();

            coverLetterRepository.save(coverLetter);

        } catch (Exception e) {
            coverLetter.setOptimizationStatus("failed");
            coverLetterRepository.save(coverLetter);
        }
    }

    /**
     * 应用定制化修改
     */
    private void applyCustomizations(CoverLetter coverLetter, Map<String, Object> customizations) {
        if (coverLetter.getContent() == null) {
            coverLetter.setContent(new CoverLetter.CoverLetterContent());
        }

        CoverLetter.CoverLetterContent content = coverLetter.getContent();

        if (customizations.containsKey("salutation")) {
            content.setSalutation((String) customizations.get("salutation"));
        }
        if (customizations.containsKey("openingParagraph")) {
            content.setOpeningParagraph((String) customizations.get("openingParagraph"));
        }
        if (customizations.containsKey("bodyParagraphs")) {
            content.setBodyParagraphs((String) customizations.get("bodyParagraphs"));
        }
        if (customizations.containsKey("closingParagraph")) {
            content.setClosingParagraph((String) customizations.get("closingParagraph"));
        }
        if (customizations.containsKey("signature")) {
            content.setSignature((String) customizations.get("signature"));
        }
        if (customizations.containsKey("contactInfo")) {
            content.setContactInfo((String) customizations.get("contactInfo"));
        }
        if (customizations.containsKey("postscript")) {
            content.setPostscript((String) customizations.get("postscript"));
        }

        // 更新语言和风格
        if (customizations.containsKey("tone")) {
            coverLetter.setTone((String) customizations.get("tone"));
        }
        if (customizations.containsKey("formalityLevel")) {
            coverLetter.setFormalityLevel((Integer) customizations.get("formalityLevel"));
        }
    }

    /**
     * 计算初始分数
     */
    private void calculateInitialScores(CoverLetter coverLetter) {
        // 简化的初始分数计算
        coverLetter.setQualityScore(0.6);
        coverLetter.setRelevanceScore(0.5);
        coverLetter.setCompletenessScore(0.7);
    }

    /**
     * 计算质量分数
     */
    private void calculateQualityScores(CoverLetter coverLetter, EnhancedResume resume, JobRequirement jobRequirement) {
        double qualityScore = calculateContentQuality(coverLetter.getContent());
        double relevanceScore = calculateRelevance(coverLetter, jobRequirement);
        double completenessScore = calculateCompleteness(coverLetter.getContent());

        coverLetter.setQualityScore(qualityScore);
        coverLetter.setRelevanceScore(relevanceScore);
        coverLetter.setCompletenessScore(completenessScore);
    }

    /**
     * 计算匹配度
     */
    private double calculateMatchScore(EnhancedResume resume, JobRequirement jobRequirement) {
        // 简化的匹配度计算
        double score = 0.0;

        if (resume.getSkills() != null && jobRequirement.getSkills() != null) {
            long matchingSkills = resume.getSkills().getTechnicalSkills().stream()
                    .filter(skill -> jobRequirement.getSkills().contains(skill))
                    .count();
            score += (double) matchingSkills / jobRequirement.getSkills().size() * 0.5;
        }

        // 其他匹配因素
        score += 0.3; // 经验匹配
        score += 0.2; // 教育匹配

        return Math.min(score, 1.0);
    }

    // 辅助方法（简化实现）
    private String generateSalutation(Map<String, Object> data) {
        String recipientName = (String) data.getOrDefault("recipientName", "招聘经理");
        return "尊敬的" + recipientName + "：";
    }

    private String generateOpeningParagraph(Map<String, Object> data) {
        String position = (String) data.getOrDefault("position", "该职位");
        String companyName = (String) data.getOrDefault("companyName", "贵公司");
        return "我对贵公司发布的" + position + "职位非常感兴趣，特此申请。";
    }

    private String generateBodyParagraphs(Map<String, Object> data) {
        return "我的技能和经验与该职位的要求非常匹配。我有相关的工作经验，能够快速适应工作并为团队创造价值。";
    }

    private String generateClosingParagraph(Map<String, Object> data) {
        return "感谢您考虑我的申请。期待有机会与您进一步沟通。";
    }

    private String generateSignature(Map<String, Object> data) {
        return "此致\n敬礼";
    }

    private String buildPersonalizationPrompt(EnhancedResume resume, JobRequirement jobRequirement,
                                           CoverLetterTemplate template, Map<String, Object> customData) {
        return "基于简历和招聘要求生成个性化求职信";
    }

    private String extractResumeContent(EnhancedResume resume) {
        return "简历内容摘要";
    }

    private CoverLetter.CoverLetterContent parseAIGeneratedContent(String aiContent) {
        CoverLetter.CoverLetterContent content = new CoverLetter.CoverLetterContent();
        // 简化的内容解析
        content.setSalutation("尊敬的招聘经理：");
        content.setOpeningParagraph(aiContent);
        content.setBodyParagraphs(aiContent);
        content.setClosingParagraph("感谢您的考虑。");
        content.setSignature("此致\n敬礼");
        return content;
    }

    private String buildContentString(CoverLetter.CoverLetterContent content) {
        if (content == null) return "";
        StringBuilder sb = new StringBuilder();
        if (content.getOpeningParagraph() != null) sb.append(content.getOpeningParagraph());
        if (content.getBodyParagraphs() != null) sb.append(content.getBodyParagraphs());
        if (content.getClosingParagraph() != null) sb.append(content.getClosingParagraph());
        return sb.toString();
    }

    private String buildOptimizationPrompt(String content, Map<String, Object> options) {
        return "优化求职信内容";
    }

    private double calculateContentQuality(CoverLetter.CoverLetterContent content) {
        // 简化的质量计算
        return content != null && content.getOpeningParagraph() != null ? 0.8 : 0.5;
    }

    private double calculateRelevance(CoverLetter coverLetter, JobRequirement jobRequirement) {
        // 简化的相关性计算
        return 0.7;
    }

    private double calculateCompleteness(CoverLetter.CoverLetterContent content) {
        // 简化的完整性计算
        if (content == null) return 0.0;
        int completeSections = 0;
        if (content.getSalutation() != null) completeSections++;
        if (content.getOpeningParagraph() != null) completeSections++;
        if (content.getBodyParagraphs() != null) completeSections++;
        if (content.getClosingParagraph() != null) completeSections++;
        return (double) completeSections / 4.0;
    }
}