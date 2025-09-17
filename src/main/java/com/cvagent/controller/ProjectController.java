package com.cvagent.controller;

import com.cvagent.dto.ProjectCreateRequest;
import com.cvagent.dto.ProjectSearchRequest;
import com.cvagent.model.Project;
import com.cvagent.model.User;
import com.cvagent.repository.ResumeRepository;
import com.cvagent.security.UserPrincipal;
import com.cvagent.service.ProjectService;
import com.cvagent.service.MarkdownService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "项目管理", description = "项目的创建、查询、更新和删除相关接口")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private MarkdownService markdownService;

    /**
     * 创建项目
     */
    @PostMapping
    @Operation(summary = "创建项目", description = "创建新的项目记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Project> createProject(
            @Parameter(description = "项目创建请求参数", required = true)
            @Valid @RequestBody ProjectCreateRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 创建项目: {}", userPrincipal.getUsername(), request.getName());

        User user = new User();
        user.setId(userPrincipal.getId());

        // 创建项目对象
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOverview(request.getOverview());
        project.setTechnologyStack(request.getTechnologyStack());
        project.setResponsibilities(request.getResponsibilities());
        project.setAchievements(request.getAchievements());
        project.setMarkdownContent(request.getMarkdownContent());
        project.setTags(request.getTags());
        project.setProjectUrl(request.getProjectUrl());
        project.setRepositoryUrl(request.getRepositoryUrl());
        project.setDemoUrl(request.getDemoUrl());
        project.setTeamSize(request.getTeamSize());
        project.setTeamRole(request.getTeamRole());
        project.setStatus(request.getStatus());
        project.setPriority(request.getPriority());
        project.setIsPublic(request.getIsPublic());
        project.setVersion(request.getVersion());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setIsOngoing(request.getIsOngoing());

        // 设置关联的简历
        com.cvagent.model.Resume resume = new com.cvagent.model.Resume();
        resume.setId(request.getResumeId());
        project.setResume(resume);

        Project createdProject = projectService.createProject(project, user);
        return ResponseEntity.ok(createdProject);
    }

    /**
     * 获取用户的所有项目
     */
    @GetMapping
    @Operation(summary = "获取项目列表", description = "获取当前用户的所有项目记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<List<Project>> getUserProjects(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 查询所有项目", userPrincipal.getUsername());

        User user = new User();
        user.setId(userPrincipal.getId());

        List<Project> projects = projectService.getUserProjects(user);
        return ResponseEntity.ok(projects);
    }

    /**
     * 根据简历ID获取项目
     */
    @GetMapping("/resume/{resumeId}")
    @Operation(summary = "根据简历获取项目", description = "获取指定简历关联的所有项目")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "简历不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<List<Project>> getProjectsByResume(
            @Parameter(description = "简历ID", required = true, example = "resume123")
            @PathVariable String resumeId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 查询简历 {} 的项目", userPrincipal.getUsername(), resumeId);

        User user = new User();
        user.setId(userPrincipal.getId());

        List<Project> projects = projectService.getProjectsByResume(resumeId, user);
        return ResponseEntity.ok(projects);
    }

    /**
     * 根据ID获取项目
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取项目详情", description = "根据项目ID获取项目详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "项目不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Project> getProjectById(
            @Parameter(description = "项目ID", required = true, example = "project123")
            @PathVariable String id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 查询项目: {}", userPrincipal.getUsername(), id);

        User user = new User();
        user.setId(userPrincipal.getId());

        Project project = projectService.getProjectById(id, user);
        return ResponseEntity.ok(project);
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新项目", description = "更新指定项目的信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "项目不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Project> updateProject(
            @Parameter(description = "项目ID", required = true, example = "project123")
            @PathVariable String id,
            @Parameter(description = "项目更新请求参数", required = true)
            @Valid @RequestBody ProjectCreateRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 更新项目: {}", userPrincipal.getUsername(), id);

        User user = new User();
        user.setId(userPrincipal.getId());

        // 创建项目更新对象
        Project projectDetails = new Project();
        projectDetails.setName(request.getName());
        projectDetails.setDescription(request.getDescription());
        projectDetails.setOverview(request.getOverview());
        projectDetails.setTechnologyStack(request.getTechnologyStack());
        projectDetails.setResponsibilities(request.getResponsibilities());
        projectDetails.setAchievements(request.getAchievements());
        projectDetails.setMarkdownContent(request.getMarkdownContent());
        projectDetails.setTags(request.getTags());
        projectDetails.setProjectUrl(request.getProjectUrl());
        projectDetails.setRepositoryUrl(request.getRepositoryUrl());
        projectDetails.setDemoUrl(request.getDemoUrl());
        projectDetails.setTeamSize(request.getTeamSize());
        projectDetails.setTeamRole(request.getTeamRole());
        projectDetails.setStatus(request.getStatus());
        projectDetails.setPriority(request.getPriority());
        projectDetails.setIsPublic(request.getIsPublic());
        projectDetails.setVersion(request.getVersion());
        projectDetails.setStartDate(request.getStartDate());
        projectDetails.setEndDate(request.getEndDate());
        projectDetails.setIsOngoing(request.getIsOngoing());

        Project updatedProject = projectService.updateProject(id, projectDetails, user);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除项目", description = "删除指定的项目记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "删除成功"),
        @ApiResponse(responseCode = "404", description = "项目不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "项目ID", required = true, example = "project123")
            @PathVariable String id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 删除项目: {}", userPrincipal.getUsername(), id);

        User user = new User();
        user.setId(userPrincipal.getId());

        projectService.deleteProject(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * 搜索项目
     */
    @PostMapping("/search")
    @Operation(summary = "搜索项目", description = "根据条件搜索项目")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "搜索成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Page<Project>> searchProjects(
            @Parameter(description = "项目搜索请求参数", required = true)
            @Valid @RequestBody ProjectSearchRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 搜索项目", userPrincipal.getUsername());

        User user = new User();
        user.setId(userPrincipal.getId());

        List<Project> projects = projectService.searchProjects(
                user,
                request.getQuery(),
                request.getStatus(),
                request.getTags(),
                request.getSortBy(),
                request.getSortOrder(),
                request.getPage(),
                request.getSize()
        );

        // 计算总数
        int total = projectService.getUserProjects(user).size();
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<Project> page = new PageImpl<>(projects, pageable, total);

        return ResponseEntity.ok(page);
    }

    /**
     * 获取项目统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取项目统计", description = "获取项目相关的统计信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Map<String, Object>> getProjectStatistics(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 查询项目统计", userPrincipal.getUsername());

        User user = new User();
        user.setId(userPrincipal.getId());

        Map<String, Object> stats = projectService.getProjectStatistics(user);
        return ResponseEntity.ok(stats);
    }

    /**
     * 添加项目标签
     */
    @PostMapping("/{id}/tags")
    @Operation(summary = "添加项目标签", description = "为指定项目添加标签")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "添加成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "项目不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Project> addProjectTag(
            @Parameter(description = "项目ID", required = true, example = "project123")
            @PathVariable String id,
            @Parameter(description = "标签名称", required = true, example = "Java")
            @RequestParam String tag,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 为项目 {} 添加标签: {}", userPrincipal.getUsername(), id, tag);

        User user = new User();
        user.setId(userPrincipal.getId());

        Project project = projectService.addProjectTag(id, tag, user);
        return ResponseEntity.ok(project);
    }

    /**
     * 移除项目标签
     */
    @DeleteMapping("/{id}/tags/{tag}")
    @Operation(summary = "移除项目标签", description = "从指定项目中移除标签")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "移除成功"),
        @ApiResponse(responseCode = "404", description = "项目或标签不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Project> removeProjectTag(
            @Parameter(description = "项目ID", required = true, example = "project123")
            @PathVariable String id,
            @Parameter(description = "标签名称", required = true, example = "Java")
            @PathVariable String tag,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 为项目 {} 移除标签: {}", userPrincipal.getUsername(), id, tag);

        User user = new User();
        user.setId(userPrincipal.getId());

        Project project = projectService.removeProjectTag(id, tag, user);
        return ResponseEntity.ok(project);
    }

    /**
     * 更新项目状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新项目状态", description = "更新指定项目的状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "项目不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Project> updateProjectStatus(
            @Parameter(description = "项目ID", required = true, example = "project123")
            @PathVariable String id,
            @Parameter(description = "项目状态", required = true, example = "active")
            @RequestParam String status,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 更新项目 {} 状态为: {}", userPrincipal.getUsername(), id, status);

        User user = new User();
        user.setId(userPrincipal.getId());

        Project project = projectService.updateProjectStatus(id, status, user);
        return ResponseEntity.ok(project);
    }

    /**
     * 增加项目版本
     */
    @PostMapping("/{id}/version")
    @Operation(summary = "增加项目版本", description = "增加指定项目的版本号")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "404", description = "项目不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Project> incrementProjectVersion(
            @Parameter(description = "项目ID", required = true, example = "project123")
            @PathVariable String id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 增加项目 {} 版本", userPrincipal.getUsername(), id);

        User user = new User();
        user.setId(userPrincipal.getId());

        Project project = projectService.incrementProjectVersion(id, user);
        return ResponseEntity.ok(project);
    }

    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    @Operation(summary = "获取所有标签", description = "获取用户的所有项目标签")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<List<String>> getAllTags(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 查询所有标签", userPrincipal.getUsername());

        User user = new User();
        user.setId(userPrincipal.getId());

        List<String> tags = projectService.getAllTags(user);
        return ResponseEntity.ok(tags);
    }

    /**
     * 根据标签搜索项目
     */
    @GetMapping("/tag/{tag}")
    @Operation(summary = "根据标签搜索项目", description = "根据指定标签搜索项目")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "搜索成功"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<List<Project>> searchProjectsByTag(
            @Parameter(description = "标签名称", required = true, example = "Java")
            @PathVariable String tag,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 根据标签搜索项目: {}", userPrincipal.getUsername(), tag);

        User user = new User();
        user.setId(userPrincipal.getId());

        List<Project> projects = projectService.searchProjectsByTag(tag, user);
        return ResponseEntity.ok(projects);
    }

    /**
     * 处理项目Markdown内容
     */
    @GetMapping("/{id}/markdown")
    @Operation(summary = "处理Markdown内容", description = "处理项目的Markdown内容")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "处理成功"),
        @ApiResponse(responseCode = "404", description = "项目不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Map<String, Object>> processProjectMarkdown(
            @Parameter(description = "项目ID", required = true, example = "project123")
            @PathVariable String id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 处理项目 {} 的Markdown内容", userPrincipal.getUsername(), id);

        User user = new User();
        user.setId(userPrincipal.getId());

        Project project = projectService.getProjectById(id, user);
        Map<String, Object> processedContent = markdownService.processProjectMarkdown(project);
        return ResponseEntity.ok(processedContent);
    }

    /**
     * 批量更新项目标签
     */
    @PostMapping("/{id}/tags/batch")
    @Operation(summary = "批量更新标签", description = "批量更新项目的标签")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "项目不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Project> batchUpdateProjectTags(
            @Parameter(description = "项目ID", required = true, example = "project123")
            @PathVariable String id,
            @Parameter(description = "标签列表", required = true)
            @RequestBody List<String> tags,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 批量更新项目 {} 的标签", userPrincipal.getUsername(), id);

        User user = new User();
        user.setId(userPrincipal.getId());

        Project project = projectService.getProjectById(id, user);

        // 清除现有标签并添加新标签
        project.setTags(new java.util.ArrayList<>(tags));
        project.setUpdatedAt(java.time.LocalDateTime.now());

        Project updatedProject = projectService.updateProject(id, project, user);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * 获取标签云数据
     */
    @GetMapping("/tags/cloud")
    @Operation(summary = "获取标签云", description = "获取项目的标签云数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Map<String, Object>> getTagCloud(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 获取标签云数据", userPrincipal.getUsername());

        User user = new User();
        user.setId(userPrincipal.getId());

        List<String> allTags = projectService.getAllTags(user);
        List<Project> allProjects = projectService.getUserProjects(user);

        // 统计每个标签的使用次数
        Map<String, Integer> tagCounts = new java.util.HashMap<>();
        for (Project project : allProjects) {
            if (project.getTags() != null) {
                for (String tag : project.getTags()) {
                    tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
                }
            }
        }

        // 计算标签权重
        int maxCount = tagCounts.values().stream().max(Integer::compare).orElse(1);
        Map<String, Object> tagCloud = new java.util.HashMap<>();
        tagCloud.put("tags", allTags);
        tagCloud.put("tagCounts", tagCounts);
        tagCloud.put("maxCount", maxCount);

        return ResponseEntity.ok(tagCloud);
    }

    /**
     * 智能项目搜索（综合搜索）
     */
    @PostMapping("/smart-search")
    @Operation(summary = "智能搜索", description = "综合搜索项目，支持多种条件过滤")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "搜索成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Map<String, Object>> smartSearchProjects(
            @Parameter(description = "搜索参数", required = true)
            @RequestBody Map<String, Object> searchParams,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 执行智能项目搜索", userPrincipal.getUsername());

        User user = new User();
        user.setId(userPrincipal.getId());

        String query = (String) searchParams.get("query");
        String status = (String) searchParams.get("status");
        List<String> tags = (List<String>) searchParams.get("tags");
        String startDate = (String) searchParams.get("startDate");
        String endDate = (String) searchParams.get("endDate");
        Boolean isPublic = (Boolean) searchParams.get("isPublic");
        Integer minPriority = (Integer) searchParams.get("minPriority");

        List<Project> allProjects = projectService.getUserProjects(user);
        List<Project> filteredProjects = new java.util.ArrayList<>(allProjects);

        // 应用过滤条件
        if (query != null && !query.trim().isEmpty()) {
            String searchQuery = query.toLowerCase();
            filteredProjects = filteredProjects.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchQuery) ||
                            p.getDescription().toLowerCase().contains(searchQuery) ||
                            p.getTechnologyStack().toLowerCase().contains(searchQuery) ||
                            (p.getMarkdownContent() != null &&
                             p.getMarkdownContent().toLowerCase().contains(searchQuery)))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty()) {
            filteredProjects = filteredProjects.stream()
                    .filter(p -> status.equals(p.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (tags != null && !tags.isEmpty()) {
            filteredProjects = filteredProjects.stream()
                    .filter(p -> p.getTags() != null &&
                            p.getTags().stream().anyMatch(tags::contains))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDate + "T00:00:00");
            filteredProjects = filteredProjects.stream()
                    .filter(p -> p.getStartDate() != null && p.getStartDate().isAfter(start))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDate + "T23:59:59");
            filteredProjects = filteredProjects.stream()
                    .filter(p -> p.getEndDate() != null && p.getEndDate().isBefore(end))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (isPublic != null) {
            filteredProjects = filteredProjects.stream()
                    .filter(p -> isPublic.equals(p.getIsPublic()))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (minPriority != null) {
            filteredProjects = filteredProjects.stream()
                    .filter(p -> p.getPriority() != null && p.getPriority() >= minPriority)
                    .collect(java.util.stream.Collectors.toList());
        }

        // 生成搜索结果摘要
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("projects", filteredProjects);
        result.put("total", filteredProjects.size());
        result.put("query", searchParams);

        // 添加搜索建议
        List<String> suggestions = generateSearchSuggestions(allProjects, query);
        result.put("suggestions", suggestions);

        return ResponseEntity.ok(result);
    }

    /**
     * 生成搜索建议
     */
    private List<String> generateSearchSuggestions(List<Project> projects, String query) {
        List<String> suggestions = new java.util.ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return suggestions;
        }

        String lowerQuery = query.toLowerCase();

        // 从项目名称、描述和技术栈中提取建议
        for (Project project : projects) {
            if (project.getName().toLowerCase().contains(lowerQuery)) {
                suggestions.add(project.getName());
            }
            if (project.getDescription() != null &&
                project.getDescription().toLowerCase().contains(lowerQuery)) {
                suggestions.add(project.getDescription());
            }
            if (project.getTechnologyStack() != null &&
                project.getTechnologyStack().toLowerCase().contains(lowerQuery)) {
                suggestions.add(project.getTechnologyStack());
            }
        }

        // 从标签中提取建议
        for (Project project : projects) {
            if (project.getTags() != null) {
                for (String tag : project.getTags()) {
                    if (tag.toLowerCase().contains(lowerQuery)) {
                        suggestions.add(tag);
                    }
                }
            }
        }

        // 去重并限制数量
        return suggestions.stream()
                .distinct()
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
    }
}