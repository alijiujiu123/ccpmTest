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
    private String overview;
    private String technologyStack;
    private String responsibilities;
    private List<String> achievements;

    // Markdown格式的详细描述
    private String markdownContent;

    // 项目标签
    private List<String> tags;

    // 项目链接和资源
    private String projectUrl;
    private String repositoryUrl;
    private String demoUrl;

    // 团队信息
    private String teamSize;
    private String teamRole;

    // 项目状态和优先级
    private String status; // planning, development, testing, completed, on-hold
    private Integer priority; // 1-5, 5为最高

    // 项目可见性
    private Boolean isPublic = false;

    // 版本信息
    private String version;
    private Integer versionNumber = 1;

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
        this.status = "planning";
        this.priority = 3;
        this.version = "1.0";
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

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getTechnologyStack() { return technologyStack; }
    public void setTechnologyStack(String technologyStack) { this.technologyStack = technologyStack; }

    public String getResponsibilities() { return responsibilities; }
    public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }

    public List<String> getAchievements() { return achievements; }
    public void setAchievements(List<String> achievements) { this.achievements = achievements; }

    public String getMarkdownContent() { return markdownContent; }
    public void setMarkdownContent(String markdownContent) { this.markdownContent = markdownContent; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getProjectUrl() { return projectUrl; }
    public void setProjectUrl(String projectUrl) { this.projectUrl = projectUrl; }

    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }

    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }

    public String getTeamSize() { return teamSize; }
    public void setTeamSize(String teamSize) { this.teamSize = teamSize; }

    public String getTeamRole() { return teamRole; }
    public void setTeamRole(String teamRole) { this.teamRole = teamRole; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Boolean getIsOngoing() { return isOngoing; }
    public void setIsOngoing(Boolean isOngoing) { this.isOngoing = isOngoing; }

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

    // 增加版本号
    public void incrementVersion() {
        this.versionNumber++;
        this.version = versionNumber + ".0";
    }

    // 检查项目是否活跃
    public boolean isActive() {
        return "development".equals(status) || "testing".equals(status);
    }

    // 获取技术栈列表
    public List<String> getTechnologyList() {
        if (technologyStack == null || technologyStack.trim().isEmpty()) {
            return List.of();
        }
        return List.of(technologyStack.split(","));
    }

    // 添加标签
    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new java.util.ArrayList<>();
        }
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    // 移除标签
    public void removeTag(String tag) {
        if (this.tags != null) {
            this.tags.remove(tag);
        }
    }
}