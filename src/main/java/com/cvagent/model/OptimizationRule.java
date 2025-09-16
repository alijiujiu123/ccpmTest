package com.cvagent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Document(collection = "optimization_rules")
public class OptimizationRule {

    @Id
    private String id;

    @NotBlank(message = "规则名称不能为空")
    private String name;

    private String description;
    private String category; // KEYWORD, FORMAT, CONTENT, STRUCTURE
    private String pattern; // 正则表达式模式
    private String suggestion; // 优化建议

    private Integer priority; // 优先级，1-5，5最高
    private Boolean isActive;
    private String targetSection; // SUMMARY, SKILLS, EXPERIENCE, EDUCATION, ALL

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public OptimizationRule() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.priority = 3;
    }

    public OptimizationRule(String name, String category) {
        this();
        this.name = name;
        this.category = category;
    }

    // 检查规则是否匹配
    public boolean matches(String text) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return false;
        }
        return text.matches(pattern);
    }

    // 应用规则
    public String applyOptimization(String text) {
        if (!matches(text)) {
            return text;
        }

        // 这里可以实现具体的优化逻辑
        // 简单的实现：返回建议的修改
        return suggestion;
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

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public String getTargetSection() { return targetSection; }
    public void setTargetSection(String targetSection) { this.targetSection = targetSection; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 保存前自动更新时间
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}