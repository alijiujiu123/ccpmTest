package com.cvagent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 求职信实体类
 * 用于存储生成的求职信内容和相关元数据
 */
@Document(collection = "cover_letters")
public class CoverLetter {

    @Id
    private String id;

    // 关联信息
    private String userId;
    private String resumeId; // 关联的简历ID
    private String jobRequirementId; // 关联的招聘需求ID
    private String templateId; // 使用的模板ID

    // 基本信息
    private String title;
    private String recipientName; // 收件人姓名
    private String recipientTitle; // 收件人职位
    private String companyName; // 公司名称
    private String position; // 申请职位

    // 求职信内容
    private CoverLetterContent content;
    private Map<String, Object> customizations; // 用户自定义内容
    private List<String> keywords; // 关键词列表
    private Double matchScore; // 与职位的匹配度

    // 生成信息
    private String generatedBy; // ai_generated, manual_edited, template_based
    private LocalDateTime generatedAt;
    private LocalDateTime lastModifiedAt;
    private Integer version = 1;

    // AI优化信息
    private Boolean aiOptimized = false;
    private String optimizationStatus; // pending, processing, completed, failed
    private Map<String, Object> optimizationMetrics;
    private List<String> optimizationSuggestions;

    // 质量评估
    private Double qualityScore; // 内容质量评分
    private Double relevanceScore; // 相关性评分
    private Double completenessScore; // 完整性评分

    // 使用统计
    private Integer viewCount = 0;
    private Integer downloadCount = 0;
    private Double userRating = 0.0;

    // 状态管理
    private String status; // draft, ready, sent, archived
    private Boolean isPublic = false;
    private Boolean isTemplate = false;

    // 元数据
    private String language = "zh-CN";
    private String tone; // professional, casual, enthusiastic
    private Integer formalityLevel; // 1-10
    private Map<String, Object> metadata;

    /**
     * 求职信内容结构
     */
    public static class CoverLetterContent {
        private String salutation; // 称呼
        private String openingParagraph; // 开头段落
        private String bodyParagraphs; // 正文段落
        private String closingParagraph; // 结尾段落
        private String signature; // 署名
        private String contactInfo; // 联系信息
        private String postscript; // 附言

        // Getters and Setters
        public String getSalutation() { return salutation; }
        public void setSalutation(String salutation) { this.salutation = salutation; }

        public String getOpeningParagraph() { return openingParagraph; }
        public void setOpeningParagraph(String openingParagraph) { this.openingParagraph = openingParagraph; }

        public String getBodyParagraphs() { return bodyParagraphs; }
        public void setBodyParagraphs(String bodyParagraphs) { this.bodyParagraphs = bodyParagraphs; }

        public String getClosingParagraph() { return closingParagraph; }
        public void setClosingParagraph(String closingParagraph) { this.closingParagraph = closingParagraph; }

        public String getSignature() { return signature; }
        public void setSignature(String signature) { this.signature = signature; }

        public String getContactInfo() { return contactInfo; }
        public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

        public String getPostscript() { return postscript; }
        public void setPostscript(String postscript) { this.postscript = postscript; }
    }

    // Constructors
    public CoverLetter() {
        this.generatedAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
        this.status = "draft";
    }

    public CoverLetter(String userId, String title) {
        this();
        this.userId = userId;
        this.title = title;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }

    public String getJobRequirementId() { return jobRequirementId; }
    public void setJobRequirementId(String jobRequirementId) { this.jobRequirementId = jobRequirementId; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientTitle() { return recipientTitle; }
    public void setRecipientTitle(String recipientTitle) { this.recipientTitle = recipientTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public CoverLetterContent getContent() { return content; }
    public void setContent(CoverLetterContent content) { this.content = content; }

    public Map<String, Object> getCustomizations() { return customizations; }
    public void setCustomizations(Map<String, Object> customizations) { this.customizations = customizations; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Boolean getAiOptimized() { return aiOptimized; }
    public void setAiOptimized(Boolean aiOptimized) { this.aiOptimized = aiOptimized; }

    public String getOptimizationStatus() { return optimizationStatus; }
    public void setOptimizationStatus(String optimizationStatus) { this.optimizationStatus = optimizationStatus; }

    public Map<String, Object> getOptimizationMetrics() { return optimizationMetrics; }
    public void setOptimizationMetrics(Map<String, Object> optimizationMetrics) { this.optimizationMetrics = optimizationMetrics; }

    public List<String> getOptimizationSuggestions() { return optimizationSuggestions; }
    public void setOptimizationSuggestions(List<String> optimizationSuggestions) { this.optimizationSuggestions = optimizationSuggestions; }

    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }

    public Double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }

    public Double getCompletenessScore() { return completenessScore; }
    public void setCompletenessScore(Double completenessScore) { this.completenessScore = completenessScore; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public Double getUserRating() { return userRating; }
    public void setUserRating(Double userRating) { this.userRating = userRating; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public Boolean getIsTemplate() { return isTemplate; }
    public void setIsTemplate(Boolean isTemplate) { this.isTemplate = isTemplate; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }

    public Integer getFormalityLevel() { return formalityLevel; }
    public void setFormalityLevel(Integer formalityLevel) { this.formalityLevel = formalityLevel; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    // Business methods
    public void incrementViewCount() {
        this.viewCount++;
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void incrementDownloadCount() {
        this.downloadCount++;
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void updateContent() {
        this.lastModifiedAt = LocalDateTime.now();
        this.version++;
    }

    public void markAsOptimized() {
        this.aiOptimized = true;
        this.optimizationStatus = "completed";
        this.lastModifiedAt = LocalDateTime.now();
    }

    public Double getOverallScore() {
        if (qualityScore != null && relevanceScore != null && completenessScore != null) {
            return (qualityScore * 0.4 + relevanceScore * 0.4 + completenessScore * 0.2);
        }
        return 0.0;
    }

    public boolean isReadyForExport() {
        return content != null
                && content.getSalutation() != null
                && content.getOpeningParagraph() != null
                && content.getBodyParagraphs() != null
                && content.getClosingParagraph() != null
                && "ready".equals(status);
    }

    public boolean isEditable() {
        return "draft".equals(status) || "ready".equals(status);
    }

    @Override
    public String toString() {
        return "CoverLetter{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", companyName='" + companyName + '\'' +
                ", position='" + position + '\'' +
                ", status='" + status + '\'' +
                ", generatedBy='" + generatedBy + '\'' +
                '}';
    }
}