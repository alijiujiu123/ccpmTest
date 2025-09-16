package com.cvagent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 求职信模板实体类
 * 用于存储和管理求职信模板
 */
@Document(collection = "cover_letter_templates")
public class CoverLetterTemplate {

    @Id
    private String id;

    // 基本信息
    private String name;
    private String description;
    private String category; // professional, creative, academic, etc.
    private String style; // formal, casual, modern, etc.

    // 模板内容结构
    private TemplateStructure structure;

    // 样式配置
    private TemplateStyling styling;

    // 语言和风格配置
    private LanguageConfig languageConfig;

    // 使用统计
    private Integer usageCount = 0;
    private Double averageRating = 0.0;

    // 元数据
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive = true;
    private Integer version = 1;

    /**
     * 模板结构配置
     */
    public static class TemplateStructure {
        private List<Section> sections;
        private Map<String, Object> variables;
        private String layoutTemplate;

        // Getters and Setters
        public List<Section> getSections() { return sections; }
        public void setSections(List<Section> sections) { this.sections = sections; }

        public Map<String, Object> getVariables() { return variables; }
        public void setVariables(Map<String, Object> variables) { this.variables = variables; }

        public String getLayoutTemplate() { return layoutTemplate; }
        public void setLayoutTemplate(String layoutTemplate) { this.layoutTemplate = layoutTemplate; }
    }

    /**
     * 模板段落配置
     */
    public static class Section {
        private String id;
        private String title;
        private String type; // header, body, closing, signature
        private Boolean required = true;
        private Integer order;
        private String defaultContent;
        private List<String> placeholders;
        private Map<String, Object> rules;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Boolean getRequired() { return required; }
        public void setRequired(Boolean required) { this.required = required; }

        public Integer getOrder() { return order; }
        public void setOrder(Integer order) { this.order = order; }

        public String getDefaultContent() { return defaultContent; }
        public void setDefaultContent(String defaultContent) { this.defaultContent = defaultContent; }

        public List<String> getPlaceholders() { return placeholders; }
        public void setPlaceholders(List<String> placeholders) { this.placeholders = placeholders; }

        public Map<String, Object> getRules() { return rules; }
        public void setRules(Map<String, Object> rules) { this.rules = rules; }
    }

    /**
     * 样式配置
     */
    public static class TemplateStyling {
        private String fontFamily = "Microsoft YaHei";
        private Integer fontSize = 12;
        private String lineHeight = "1.5";
        private String primaryColor = "#333333";
        private String backgroundColor = "#ffffff";
        private String accentColor = "#007bff";
        private Map<String, Object> customStyles;

        // Getters and Setters
        public String getFontFamily() { return fontFamily; }
        public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }

        public Integer getFontSize() { return fontSize; }
        public void setFontSize(Integer fontSize) { this.fontSize = fontSize; }

        public String getLineHeight() { return lineHeight; }
        public void setLineHeight(String lineHeight) { this.lineHeight = lineHeight; }

        public String getPrimaryColor() { return primaryColor; }
        public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }

        public String getBackgroundColor() { return backgroundColor; }
        public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }

        public String getAccentColor() { return accentColor; }
        public void setAccentColor(String accentColor) { this.accentColor = accentColor; }

        public Map<String, Object> getCustomStyles() { return customStyles; }
        public void setCustomStyles(Map<String, Object> customStyles) { this.customStyles = customStyles; }
    }

    /**
     * 语言配置
     */
    public static class LanguageConfig {
        private String language = "zh-CN";
        private String tone = "professional"; // professional, friendly, enthusiastic
        private Integer formalityLevel = 7; // 1-10
        private List<String> keywords;
        private Map<String, String> phrases;
        private Boolean aiOptimized = true;

        // Getters and Setters
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public String getTone() { return tone; }
        public void setTone(String tone) { this.tone = tone; }

        public Integer getFormalityLevel() { return formalityLevel; }
        public void setFormalityLevel(Integer formalityLevel) { this.formalityLevel = formalityLevel; }

        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }

        public Map<String, String> getPhrases() { return phrases; }
        public void setPhrases(Map<String, String> phrases) { this.phrases = phrases; }

        public Boolean getAiOptimized() { return aiOptimized; }
        public void setAiOptimized(Boolean aiOptimized) { this.aiOptimized = aiOptimized; }
    }

    // Constructors
    public CoverLetterTemplate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public TemplateStructure getStructure() { return structure; }
    public void setStructure(TemplateStructure structure) { this.structure = structure; }

    public TemplateStyling getStyling() { return styling; }
    public void setStyling(TemplateStyling styling) { this.styling = styling; }

    public LanguageConfig getLanguageConfig() { return languageConfig; }
    public void setLanguageConfig(LanguageConfig languageConfig) { this.languageConfig = languageConfig; }

    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    // Business methods
    public void incrementUsageCount() {
        this.usageCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRating(Double newRating) {
        this.averageRating = (this.averageRating * (this.usageCount - 1) + newRating) / this.usageCount;
        this.updatedAt = LocalDateTime.now();
    }
}