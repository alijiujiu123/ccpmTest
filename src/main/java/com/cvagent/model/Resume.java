package com.cvagent.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "resumes")
@Schema(description = "简历实体", title = "简历")
@JsonIgnoreProperties({"user"})
public class Resume {

    @Id
    @Schema(description = "简历ID", example = "507f1f77bcf86cd799439012")
    private String id;

    @NotBlank(message = "简历标题不能为空")
    @Schema(description = "简历标题", example = "软件工程师简历", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotNull(message = "所属用户不能为空")
    @DBRef
    @Schema(description = "所属用户", hidden = true)
    private User user;

    @Schema(description = "个人简介", example = "5年Java开发经验，精通Spring Boot和微服务架构")
    private String summary;

    @Schema(description = "技能列表", example = "Java, Spring Boot, MySQL, Redis")
    private String skills;

    @Schema(description = "工作经验", example = "2020-2025 高级软件工程师")
    private String experience;

    @Schema(description = "教育背景", example = "计算机科学与技术 本科")
    private String education;
    @Schema(description = "证书列表", example = "[\"AWS认证\", \"PMP\"]")
    private List<String> certifications;

    @Schema(description = "语言能力", example = "[\"中文(母语)\", \"英语(流利)\"]")
    private List<String> languages;

    @Schema(description = "工作年限", example = "5")
    private Integer yearsOfExperience;

    @Schema(description = "目标行业", example = "互联网")
    private String targetIndustry;

    @Schema(description = "目标职位", example = "高级软件工程师")
    private String targetPosition;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    // 构造函数
    public Resume() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Resume(String title, User user) {
        this();
        this.title = title;
        this.user = user;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }

    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getTargetIndustry() { return targetIndustry; }
    public void setTargetIndustry(String targetIndustry) { this.targetIndustry = targetIndustry; }

    public String getTargetPosition() { return targetPosition; }
    public void setTargetPosition(String targetPosition) { this.targetPosition = targetPosition; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 保存前自动更新时间
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}