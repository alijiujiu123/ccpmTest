package com.cvagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "简历数据传输对象", title = "简历信息")
public class ResumeDto {

    @Schema(description = "简历ID", example = "resume_123456")
    private String id;

    @NotBlank(message = "简历标题不能为空")
    @Size(max = 100, message = "简历标题不能超过100个字符")
    @Schema(description = "简历标题", example = "软件工程师简历", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "个人简介", example = "5年Java开发经验，精通Spring Boot和微服务架构")
    private String summary;

    @Schema(description = "技能列表", example = "Java, Spring Boot, MySQL, Redis")
    private String skills;

    @Schema(description = "工作经验", example = "5年Java开发经验，参与过多个大型项目")
    private String experience;

    @Schema(description = "教育背景", example = "本科-计算机科学与技术")
    private String education;

    @Schema(description = "证书列表")
    private List<String> certifications;

    @Schema(description = "语言能力")
    private List<String> languages;

    @Schema(description = "工作年限", example = "5")
    private Integer yearsOfExperience;

    @Schema(description = "目标行业", example = "互联网")
    private String targetIndustry;

    @Schema(description = "目标职位", example = "Java开发工程师")
    private String targetPosition;

    @Schema(description = "用户ID", example = "user_123456")
    private String userId;

    @Schema(description = "创建时间", example = "2025-09-17T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2025-09-17T12:00:00")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

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

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}