package com.cvagent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 简历模板实体类
 * 用于存储不同样式的简历模板
 */
@Document(collection = "resume_templates")
public class ResumeTemplate {

    @Id
    private String id;

    private String name;
    private String description;
    private String category; // 专业、创意、简洁、学术等
    private String style; // modern, classic, creative, academic
    private String previewImage;
    private Boolean isActive;
    private Boolean isPremium;
    private Integer usageCount;

    private TemplateConfig config;
    private TemplateSections sections;
    private TemplateStyling styling;
    private TemplateLayout layout;

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 模板配置内部类
     */
    public static class TemplateConfig {
        private String fontSize;
        private String fontFamily;
        private String lineHeight;
        private String margin;
        private String pageOrientation; // portrait, landscape
        private String pageSize; // A4, Letter
        private Boolean includePhoto;
        private Boolean includeObjective;
        private Boolean includeSummary;
        private Map<String, Object> customSettings;

        // Getters and Setters
        public String getFontSize() { return fontSize; }
        public void setFontSize(String fontSize) { this.fontSize = fontSize; }
        public String getFontFamily() { return fontFamily; }
        public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
        public String getLineHeight() { return lineHeight; }
        public void setLineHeight(String lineHeight) { this.lineHeight = lineHeight; }
        public String getMargin() { return margin; }
        public void setMargin(String margin) { this.margin = margin; }
        public String getPageOrientation() { return pageOrientation; }
        public void setPageOrientation(String pageOrientation) { this.pageOrientation = pageOrientation; }
        public String getPageSize() { return pageSize; }
        public void setPageSize(String pageSize) { this.pageSize = pageSize; }
        public Boolean getIncludePhoto() { return includePhoto; }
        public void setIncludePhoto(Boolean includePhoto) { this.includePhoto = includePhoto; }
        public Boolean getIncludeObjective() { return includeObjective; }
        public void setIncludeObjective(Boolean includeObjective) { this.includeObjective = includeObjective; }
        public Boolean getIncludeSummary() { return includeSummary; }
        public void setIncludeSummary(Boolean includeSummary) { this.includeSummary = includeSummary; }
        public Map<String, Object> getCustomSettings() { return customSettings; }
        public void setCustomSettings(Map<String, Object> customSettings) { this.customSettings = customSettings; }
    }

    /**
     * 模板章节配置内部类
     */
    public static class TemplateSections {
        private List<String> sectionOrder; // 章节顺序
        private Map<String, SectionConfig> sectionConfigs; // 各章节的配置

        /**
         * 章节配置内部类
         */
        public static class SectionConfig {
            private Boolean visible;
            private Boolean required;
            private String title;
            private Integer maxLength;
            private Integer minLength;
            private String placeholder;
            private List<String> hints;

            // Getters and Setters
            public Boolean getVisible() { return visible; }
            public void setVisible(Boolean visible) { this.visible = visible; }
            public Boolean getRequired() { return required; }
            public void setRequired(Boolean required) { this.required = required; }
            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }
            public Integer getMaxLength() { return maxLength; }
            public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
            public Integer getMinLength() { return minLength; }
            public void setMinLength(Integer minLength) { this.minLength = minLength; }
            public String getPlaceholder() { return placeholder; }
            public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
            public List<String> getHints() { return hints; }
            public void setHints(List<String> hints) { this.hints = hints; }
        }

        // Getters and Setters
        public List<String> getSectionOrder() { return sectionOrder; }
        public void setSectionOrder(List<String> sectionOrder) { this.sectionOrder = sectionOrder; }
        public Map<String, SectionConfig> getSectionConfigs() { return sectionConfigs; }
        public void setSectionConfigs(Map<String, SectionConfig> sectionConfigs) { this.sectionConfigs = sectionConfigs; }
    }

    /**
     * 模板样式内部类
     */
    public static class TemplateStyling {
        private String primaryColor;
        private String secondaryColor;
        private String accentColor;
        private String backgroundColor;
        private String textColor;
        private String headingFont;
        private String bodyFont;
        private Map<String, Object> customStyles;

        // Getters and Setters
        public String getPrimaryColor() { return primaryColor; }
        public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
        public String getSecondaryColor() { return secondaryColor; }
        public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }
        public String getAccentColor() { return accentColor; }
        public void setAccentColor(String accentColor) { this.accentColor = accentColor; }
        public String getBackgroundColor() { return backgroundColor; }
        public void setBackgroundColor(String backgroundColor) { this.backgroundColor = backgroundColor; }
        public String getTextColor() { return textColor; }
        public void setTextColor(String textColor) { this.textColor = textColor; }
        public String getHeadingFont() { return headingFont; }
        public void setHeadingFont(String headingFont) { this.headingFont = headingFont; }
        public String getBodyFont() { return bodyFont; }
        public void setBodyFont(String bodyFont) { this.bodyFont = bodyFont; }
        public Map<String, Object> getCustomStyles() { return customStyles; }
        public void setCustomStyles(Map<String, Object> customStyles) { this.customStyles = customStyles; }
    }

    /**
     * 模板布局内部类
     */
    public static class TemplateLayout {
        private String layoutType; // single-column, two-column, three-column
        private String headerStyle; // centered, left-aligned, right-aligned
        private String sectionSpacing;
        private Map<String, Object> sectionLayouts; // 各章节的具体布局配置

        // Getters and Setters
        public String getLayoutType() { return layoutType; }
        public void setLayoutType(String layoutType) { this.layoutType = layoutType; }
        public String getHeaderStyle() { return headerStyle; }
        public void setHeaderStyle(String headerStyle) { this.headerStyle = headerStyle; }
        public String getSectionSpacing() { return sectionSpacing; }
        public void setSectionSpacing(String sectionSpacing) { this.sectionSpacing = sectionSpacing; }
        public Map<String, Object> getSectionLayouts() { return sectionLayouts; }
        public void setSectionLayouts(Map<String, Object> sectionLayouts) { this.sectionLayouts = sectionLayouts; }
    }

    // 构造函数
    public ResumeTemplate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.isPremium = false;
        this.usageCount = 0;
        this.config = new TemplateConfig();
        this.sections = new TemplateSections();
        this.styling = new TemplateStyling();
        this.layout = new TemplateLayout();
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

    public String getPreviewImage() { return previewImage; }
    public void setPreviewImage(String previewImage) { this.previewImage = previewImage; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsPremium() { return isPremium; }
    public void setIsPremium(Boolean isPremium) { this.isPremium = isPremium; }

    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }

    public TemplateConfig getConfig() { return config; }
    public void setConfig(TemplateConfig config) { this.config = config; }

    public TemplateSections getSections() { return sections; }
    public void setSections(TemplateSections sections) { this.sections = sections; }

    public TemplateStyling getStyling() { return styling; }
    public void setStyling(TemplateStyling styling) { this.styling = styling; }

    public TemplateLayout getLayout() { return layout; }
    public void setLayout(TemplateLayout layout) { this.layout = layout; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void incrementUsageCount() {
        this.usageCount = this.usageCount + 1;
    }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}