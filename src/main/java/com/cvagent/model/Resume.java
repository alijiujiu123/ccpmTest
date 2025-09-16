package com.cvagent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "resumes")
public class Resume {

    @Id
    private String id;

    @NotBlank(message = "简历标题不能为空")
    private String title;

    @NotNull(message = "所属用户不能为空")
    @DBRef
    private User user;

    private String summary;
    private String skills;
    private String experience;
    private String education;
    private List<String> certifications;
    private List<String> languages;

    private Integer yearsOfExperience;
    private String targetIndustry;
    private String targetPosition;

    private LocalDateTime createdAt;
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