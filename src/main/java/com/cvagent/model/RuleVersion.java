package com.cvagent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 规则版本实体类
 * 记录规则的历史版本信息
 */
@Document(collection = "rule_versions")
public class RuleVersion {

    @Id
    private String id;

    private String ruleId;
    private String ruleName;
    private Integer version;
    private String pattern;
    private String suggestion;
    private String description;
    private String category;
    private Integer priority;
    private String targetSection;
    private Boolean isActive;
    private String changeReason;
    private String changedBy;
    private LocalDateTime createdAt;
    private LocalDateTime effectiveAt;
    private LocalDateTime expiresAt;

    // 构造函数
    public RuleVersion() {
        this.createdAt = LocalDateTime.now();
    }

    public RuleVersion(OptimizationRule rule, Integer version, String changeReason, String changedBy) {
        this();
        this.ruleId = rule.getId();
        this.ruleName = rule.getName();
        this.version = version;
        this.pattern = rule.getPattern();
        this.suggestion = rule.getSuggestion();
        this.description = rule.getDescription();
        this.category = rule.getCategory();
        this.priority = rule.getPriority();
        this.targetSection = rule.getTargetSection();
        this.isActive = rule.getIsActive();
        this.changeReason = changeReason;
        this.changedBy = changedBy;
        this.effectiveAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getTargetSection() { return targetSection; }
    public void setTargetSection(String targetSection) { this.targetSection = targetSection; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getEffectiveAt() { return effectiveAt; }
    public void setEffectiveAt(LocalDateTime effectiveAt) { this.effectiveAt = effectiveAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}