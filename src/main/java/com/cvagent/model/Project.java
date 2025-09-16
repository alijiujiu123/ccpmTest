package com.cvagent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "projects")
public class Project {

    @Id
    private String id;

    @NotBlank(message = "项目名称不能为空")
    private String name;

    private String description;
    private String technologyStack;
    private String responsibilities;
    private List<String> achievements;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isOngoing;

    @NotNull(message = "所属简历不能为空")
    @DBRef
    private Resume resume;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public Project() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isOngoing = false;
    }

    public Project(String name, Resume resume) {
        this();
        this.name = name;
        this.resume = resume;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTechnologyStack() { return technologyStack; }
    public void setTechnologyStack(String technologyStack) { this.technologyStack = technologyStack; }

    public String getResponsibilities() { return responsibilities; }
    public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }

    public List<String> getAchievements() { return achievements; }
    public void setAchievements(List<String> achievements) { this.achievements = achievements; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Boolean getIsOngoing() { return isOngoing; }
    public void setIsOngoing(Boolean ongoing) { isOngoing = ongoing; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 保存前自动更新时间
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}