package com.cvagent.service;

import com.cvagent.model.Project;
import com.cvagent.model.Resume;
import com.cvagent.model.User;
import com.cvagent.repository.ProjectRepository;
import com.cvagent.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    /**
     * 创建新项目
     */
    public Project createProject(Project project, User user) {
        logger.info("用户 {} 创建新项目: {}", user.getUsername(), project.getName());

        // 验证简历权限
        Resume resume = resumeRepository.findByIdAndUserId(project.getResume().getId(), user.getId())
                .orElseThrow(() -> new RuntimeException("简历不存在或无权限访问"));

        project.setResume(resume);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    /**
     * 获取用户的所有项目
     */
    public List<Project> getUserProjects(User user) {
        logger.info("用户 {} 查询所有项目", user.getUsername());

        // 获取用户的所有简历
        List<Resume> userResumes = resumeRepository.findByUserId(user.getId());

        // 获取这些简历的所有项目
        List<String> resumeIds = userResumes.stream()
                .map(Resume::getId)
                .collect(Collectors.toList());

        return projectRepository.findByResumeIdIn(resumeIds);
    }

    /**
     * 根据简历ID获取项目
     */
    public List<Project> getProjectsByResume(String resumeId, User user) {
        logger.info("用户 {} 查询简历 {} 的项目", user.getUsername(), resumeId);

        // 验证简历权限
        resumeRepository.findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new RuntimeException("简历不存在或无权限访问"));

        return projectRepository.findByResumeId(resumeId);
    }

    /**
     * 根据ID获取项目
     */
    public Project getProjectById(String id, User user) {
        logger.info("用户 {} 查询项目: {}", user.getUsername(), id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));

        // 验证简历权限
        resumeRepository.findByIdAndUserId(project.getResume().getId(), user.getId())
                .orElseThrow(() -> new RuntimeException("无权限访问此项目"));

        return project;
    }

    /**
     * 更新项目
     */
    public Project updateProject(String id, Project projectDetails, User user) {
        logger.info("用户 {} 更新项目: {}", user.getUsername(), id);

        Project existingProject = getProjectById(id, user);

        // 更新项目信息
        existingProject.setName(projectDetails.getName());
        existingProject.setDescription(projectDetails.getDescription());
        existingProject.setOverview(projectDetails.getOverview());
        existingProject.setTechnologyStack(projectDetails.getTechnologyStack());
        existingProject.setResponsibilities(projectDetails.getResponsibilities());
        existingProject.setAchievements(projectDetails.getAchievements());
        existingProject.setMarkdownContent(projectDetails.getMarkdownContent());
        existingProject.setTags(projectDetails.getTags());
        existingProject.setProjectUrl(projectDetails.getProjectUrl());
        existingProject.setRepositoryUrl(projectDetails.getRepositoryUrl());
        existingProject.setDemoUrl(projectDetails.getDemoUrl());
        existingProject.setTeamSize(projectDetails.getTeamSize());
        existingProject.setTeamRole(projectDetails.getTeamRole());
        existingProject.setStatus(projectDetails.getStatus());
        existingProject.setPriority(projectDetails.getPriority());
        existingProject.setIsPublic(projectDetails.getIsPublic());
        existingProject.setVersion(projectDetails.getVersion());
        existingProject.setStartDate(projectDetails.getStartDate());
        existingProject.setEndDate(projectDetails.getEndDate());
        existingProject.setIsOngoing(projectDetails.getIsOngoing());
        existingProject.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(existingProject);
    }

    /**
     * 删除项目
     */
    public void deleteProject(String id, User user) {
        logger.info("用户 {} 删除项目: {}", user.getUsername(), id);

        Project project = getProjectById(id, user);
        projectRepository.delete(project);
    }

    /**
     * 搜索项目
     */
    public List<Project> searchProjects(User user, String query, String status, List<String> tags,
                                     String sortBy, String sortOrder, int page, int size) {
        logger.info("用户 {} 搜索项目，查询条件: {}", user.getUsername(), query);

        // 获取用户的所有简历
        List<Resume> userResumes = resumeRepository.findByUserId(user.getId());
        List<String> resumeIds = userResumes.stream()
                .map(Resume::getId)
                .collect(Collectors.toList());

        List<Project> projects = projectRepository.findByResumeIdIn(resumeIds);

        // 应用过滤条件
        if (query != null && !query.trim().isEmpty()) {
            String searchQuery = query.toLowerCase();
            projects = projects.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchQuery) ||
                            p.getDescription().toLowerCase().contains(searchQuery) ||
                            p.getTechnologyStack().toLowerCase().contains(searchQuery))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty()) {
            projects = projects.stream()
                    .filter(p -> status.equals(p.getStatus()))
                    .collect(Collectors.toList());
        }

        if (tags != null && !tags.isEmpty()) {
            projects = projects.stream()
                    .filter(p -> p.getTags() != null &&
                            p.getTags().stream().anyMatch(tags::contains))
                    .collect(Collectors.toList());
        }

        // 排序
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ?
                    Sort.Direction.DESC : Sort.Direction.ASC;

            switch (sortBy) {
                case "name":
                    sort = Sort.by(direction, "name");
                    break;
                case "createdAt":
                    sort = Sort.by(direction, "createdAt");
                    break;
                case "updatedAt":
                    sort = Sort.by(direction, "updatedAt");
                    break;
                case "priority":
                    sort = Sort.by(direction, "priority");
                    break;
                case "startDate":
                    sort = Sort.by(direction, "startDate");
                    break;
            }
        }

        // 分页
        if (page > 0 && size > 0) {
            int start = (page - 1) * size;
            int end = Math.min(start + size, projects.size());

            if (start >= projects.size()) {
                return Collections.emptyList();
            }

            projects = projects.subList(start, end);
        }

        return projects;
    }

    /**
     * 获取项目统计信息
     */
    public Map<String, Object> getProjectStatistics(User user) {
        logger.info("用户 {} 查询项目统计", user.getUsername());

        List<Project> projects = getUserProjects(user);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProjects", projects.size());

        // 按状态统计
        Map<String, Long> statusStats = projects.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStatus() != null ? p.getStatus() : "unknown",
                        Collectors.counting()
                ));
        stats.put("statusStats", statusStats);

        // 按优先级统计
        Map<Integer, Long> priorityStats = projects.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPriority() != null ? p.getPriority() : 0,
                        Collectors.counting()
                ));
        stats.put("priorityStats", priorityStats);

        // 活跃项目数
        long activeProjects = projects.stream()
                .filter(Project::isActive)
                .count();
        stats.put("activeProjects", activeProjects);

        // 进行中项目数
        long ongoingProjects = projects.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsOngoing()))
                .count();
        stats.put("ongoingProjects", ongoingProjects);

        // 公开项目数
        long publicProjects = projects.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsPublic()))
                .count();
        stats.put("publicProjects", publicProjects);

        // 标签统计
        List<String> allTags = projects.stream()
                .flatMap(p -> p.getTags() != null ? p.getTags().stream() : Stream.empty())
                .collect(Collectors.toList());

        Map<String, Long> tagStats = allTags.stream()
                .collect(Collectors.groupingBy(
                        tag -> tag,
                        Collectors.counting()
                ));
        stats.put("tagStats", tagStats);

        return stats;
    }

    /**
     * 添加项目标签
     */
    public Project addProjectTag(String id, String tag, User user) {
        logger.info("用户 {} 为项目 {} 添加标签: {}", user.getUsername(), id, tag);

        Project project = getProjectById(id, user);
        project.addTag(tag);
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    /**
     * 移除项目标签
     */
    public Project removeProjectTag(String id, String tag, User user) {
        logger.info("用户 {} 为项目 {} 移除标签: {}", user.getUsername(), id, tag);

        Project project = getProjectById(id, user);
        project.removeTag(tag);
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    /**
     * 更新项目状态
     */
    public Project updateProjectStatus(String id, String status, User user) {
        logger.info("用户 {} 更新项目 {} 状态为: {}", user.getUsername(), id, status);

        Project project = getProjectById(id, user);
        project.setStatus(status);
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    /**
     * 增加项目版本
     */
    public Project incrementProjectVersion(String id, User user) {
        logger.info("用户 {} 增加项目 {} 版本", user.getUsername(), id);

        Project project = getProjectById(id, user);
        project.incrementVersion();
        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    /**
     * 获取所有标签
     */
    public List<String> getAllTags(User user) {
        logger.info("用户 {} 查询所有标签", user.getUsername());

        List<Project> projects = getUserProjects(user);

        return projects.stream()
                .flatMap(p -> p.getTags() != null ? p.getTags().stream() : Stream.empty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 根据标签搜索项目
     */
    public List<Project> searchProjectsByTag(String tag, User user) {
        logger.info("用户 {} 根据标签搜索项目: {}", user.getUsername(), tag);

        List<Project> projects = getUserProjects(user);

        return projects.stream()
                .filter(p -> p.getTags() != null && p.getTags().contains(tag))
                .collect(Collectors.toList());
    }
}