package com.cvagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "项目创建请求", title = "项目创建")
public class ProjectCreateRequest {

    @NotBlank(message = "项目名称不能为空")
    @Schema(description = "项目名称", example = "电商网站开发", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "项目描述", example = "一个现代化的电商平台")
    private String description;

    @Schema(description = "项目概述", example = "基于Spring Boot和Vue的电商系统")
    private String overview;

    @Schema(description = "技术栈", example = "Java, Spring Boot, Vue, MySQL")
    private String technologyStack;

    @Schema(description = "职责描述", example = "负责后端API开发")
    private String responsibilities;

    @Schema(description = "成就列表")
    private List<String> achievements;

    @Schema(description = "Markdown格式的详细描述", example = "# 项目详情\n## 功能特点\n- 用户管理\n- 商品管理\n- 订单处理")
    private String markdownContent;

    @Schema(description = "项目标签")
    private List<String> tags;

    @Schema(description = "项目链接", example = "https://example.com/project")
    private String projectUrl;

    @Schema(description = "代码仓库链接", example = "https://github.com/user/project")
    private String repositoryUrl;

    @Schema(description = "演示链接", example = "https://demo.example.com")
    private String demoUrl;

    @Schema(description = "团队规模", example = "5人")
    private String teamSize;

    @Schema(description = "团队角色", example = "技术负责人")
    private String teamRole;

    @Schema(description = "项目状态", example = "planning", defaultValue = "planning")
    private String status = "planning";

    @Schema(description = "项目优先级", example = "3", defaultValue = "3")
    private Integer priority = 3;

    @Schema(description = "是否公开", example = "false", defaultValue = "false")
    private Boolean isPublic = false;

    @Schema(description = "版本信息", example = "1.0", defaultValue = "1.0")
    private String version = "1.0";

    @Schema(description = "开始时间", example = "2025-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "结束时间", example = "2025-06-01T00:00:00")
    private LocalDateTime endDate;

    @Schema(description = "是否进行中", example = "false", defaultValue = "false")
    private Boolean isOngoing = false;

    @NotNull(message = "所属简历ID不能为空")
    @Schema(description = "所属简历ID", example = "resume_123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resumeId;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getTechnologyStack() {
        return technologyStack;
    }

    public void setTechnologyStack(String technologyStack) {
        this.technologyStack = technologyStack;
    }

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    public List<String> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<String> achievements) {
        this.achievements = achievements;
    }

    public String getMarkdownContent() {
        return markdownContent;
    }

    public void setMarkdownContent(String markdownContent) {
        this.markdownContent = markdownContent;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getDemoUrl() {
        return demoUrl;
    }

    public void setDemoUrl(String demoUrl) {
        this.demoUrl = demoUrl;
    }

    public String getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(String teamSize) {
        this.teamSize = teamSize;
    }

    public String getTeamRole() {
        return teamRole;
    }

    public void setTeamRole(String teamRole) {
        this.teamRole = teamRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsOngoing() {
        return isOngoing;
    }

    public void setIsOngoing(Boolean isOngoing) {
        this.isOngoing = isOngoing;
    }

    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }
}