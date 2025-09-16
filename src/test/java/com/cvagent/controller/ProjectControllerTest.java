package com.cvagent.controller;

import com.cvagent.CvAgentApplication;
import com.cvagent.dto.AuthRequest;
import com.cvagent.dto.AuthResponse;
import com.cvagent.dto.ProjectCreateRequest;
import com.cvagent.dto.ProjectSearchRequest;
import com.cvagent.model.Project;
import com.cvagent.model.Resume;
import com.cvagent.model.User;
import com.cvagent.repository.ResumeRepository;
import com.cvagent.repository.UserRepository;
import com.cvagent.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CvAgentApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private User testUser;
    private Resume testResume;

    @BeforeEach
    void setUp() throws Exception {
        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);

        // 创建测试简历
        testResume = new Resume();
        testResume.setTitle("测试简历");
        testResume.setUser(testUser);
        testResume.setSummary("这是一个测试简历");
        testResume.setCreatedAt(LocalDateTime.now());
        testResume.setUpdatedAt(LocalDateTime.now());
        testResume = resumeRepository.save(testResume);

        // 使用真实的login方法获取token
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
        authToken = authResponse.getTokenType() + " " + authResponse.getToken();
    }

    @Test
    void testCreateProject() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("测试项目");
        request.setDescription("这是一个测试项目");
        request.setOverview("项目概述");
        request.setTechnologyStack("Java, Spring Boot, MongoDB");
        request.setResponsibilities("负责后端开发");
        request.setAchievements(Arrays.asList("完成了核心功能开发", "优化了系统性能"));
        request.setMarkdownContent("# 项目介绍\n\n这是一个使用 **Java** 和 **Spring Boot** 开发的项目。\n\n## 主要功能\n\n- 用户管理\n- 数据处理\n- API接口\n\n```java\npublic class Test {\n    public static void main(String[] args) {\n        System.out.println(\"Hello World\");\n    }\n}\n```");
        request.setTags(Arrays.asList("Java", "Spring Boot", "MongoDB", "后端开发"));
        request.setProjectUrl("https://github.com/test/project");
        request.setRepositoryUrl("https://github.com/test/project.git");
        request.setDemoUrl("https://demo.example.com");
        request.setTeamSize("5人");
        request.setTeamRole("后端开发工程师");
        request.setStatus("development");
        request.setPriority(4);
        request.setIsPublic(true);
        request.setVersion("1.0");
        request.setStartDate(LocalDateTime.now());
        request.setIsOngoing(true);
        request.setResumeId(testResume.getId());

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("测试项目"))
                .andExpect(jsonPath("$.description").value("这是一个测试项目"))
                .andExpect(jsonPath("$.technologyStack").value("Java, Spring Boot, MongoDB"))
                .andExpect(jsonPath("$.status").value("development"))
                .andExpect(jsonPath("$.priority").value(4))
                .andExpect(jsonPath("$.isPublic").value(true))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(4));
    }

    @Test
    void testGetUserProjects() throws Exception {
        // 先创建一个项目
        createTestProject();

        // 获取用户项目列表
        mockMvc.perform(get("/api/projects")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void testGetProjectsByResume() throws Exception {
        // 先创建一个项目
        createTestProject();

        // 根据简历ID获取项目
        mockMvc.perform(get("/api/projects/resume/" + testResume.getId())
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void testGetProjectById() throws Exception {
        // 先创建一个项目
        MvcResult createResult = createTestProject();
        String createResponse = createResult.getResponse().getContentAsString();
        Project createdProject = objectMapper.readValue(createResponse, Project.class);

        // 根据ID获取项目
        mockMvc.perform(get("/api/projects/" + createdProject.getId())
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdProject.getId()))
                .andExpect(jsonPath("$.name").value("测试项目"));
    }

    @Test
    void testUpdateProject() throws Exception {
        // 先创建一个项目
        MvcResult createResult = createTestProject();
        String createResponse = createResult.getResponse().getContentAsString();
        Project createdProject = objectMapper.readValue(createResponse, Project.class);

        // 更新项目
        ProjectCreateRequest updateRequest = new ProjectCreateRequest();
        updateRequest.setName("更新后的项目");
        updateRequest.setDescription("更新后的描述");
        updateRequest.setTechnologyStack("Java, Spring Boot, MongoDB, React");
        updateRequest.setTags(Arrays.asList("Java", "Spring Boot", "MongoDB", "React", "全栈开发"));
        updateRequest.setStatus("testing");
        updateRequest.setPriority(5);
        updateRequest.setResumeId(testResume.getId());

        mockMvc.perform(put("/api/projects/" + createdProject.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("更新后的项目"))
                .andExpect(jsonPath("$.description").value("更新后的描述"))
                .andExpect(jsonPath("$.status").value("testing"))
                .andExpect(jsonPath("$.priority").value(5))
                .andExpect(jsonPath("$.tags.length()").value(5));
    }

    @Test
    void testDeleteProject() throws Exception {
        // 先创建一个项目
        MvcResult createResult = createTestProject();
        String createResponse = createResult.getResponse().getContentAsString();
        Project createdProject = objectMapper.readValue(createResponse, Project.class);

        // 删除项目
        mockMvc.perform(delete("/api/projects/" + createdProject.getId())
                .header("Authorization", authToken))
                .andExpect(status().isNoContent());

        // 验证项目已删除
        mockMvc.perform(get("/api/projects/" + createdProject.getId())
                .header("Authorization", authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchProjects() throws Exception {
        // 创建几个测试项目
        createTestProject();

        ProjectCreateRequest request2 = new ProjectCreateRequest();
        request2.setName("前端项目");
        request2.setDescription("React前端项目");
        request2.setTechnologyStack("React, TypeScript, Node.js");
        request2.setTags(Arrays.asList("React", "TypeScript", "前端开发"));
        request2.setStatus("completed");
        request2.setResumeId(testResume.getId());
        createProject(request2);

        ProjectSearchRequest searchRequest = new ProjectSearchRequest();
        searchRequest.setQuery("Java");
        searchRequest.setStatus("development");
        searchRequest.setTags(Arrays.asList("Java"));
        searchRequest.setPage(1);
        searchRequest.setSize(10);

        mockMvc.perform(post("/api/projects/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void testGetProjectStatistics() throws Exception {
        // 创建测试项目
        createTestProject();

        // 获取项目统计
        mockMvc.perform(get("/api/projects/statistics")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.statusStats").exists())
                .andExpect(jsonPath("$.priorityStats").exists())
                .andExpect(jsonPath("$.activeProjects").exists())
                .andExpect(jsonPath("$.ongoingProjects").exists())
                .andExpect(jsonPath("$.publicProjects").exists())
                .andExpect(jsonPath("$.tagStats").exists());
    }

    @Test
    void testAddProjectTag() throws Exception {
        // 创建测试项目
        MvcResult createResult = createTestProject();
        String createResponse = createResult.getResponse().getContentAsString();
        Project createdProject = objectMapper.readValue(createResponse, Project.class);

        // 添加标签
        mockMvc.perform(post("/api/projects/" + createdProject.getId() + "/tags")
                .param("tag", "新标签")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags[?(@ == '新标签')]").exists());
    }

    @Test
    void testRemoveProjectTag() throws Exception {
        // 创建测试项目
        MvcResult createResult = createTestProject();
        String createResponse = createResult.getResponse().getContentAsString();
        Project createdProject = objectMapper.readValue(createResponse, Project.class);

        // 移除标签
        mockMvc.perform(delete("/api/projects/" + createdProject.getId() + "/tags/Java")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[?(@ == 'Java')]").doesNotExist());
    }

    @Test
    void testUpdateProjectStatus() throws Exception {
        // 创建测试项目
        MvcResult createResult = createTestProject();
        String createResponse = createResult.getResponse().getContentAsString();
        Project createdProject = objectMapper.readValue(createResponse, Project.class);

        // 更新状态
        mockMvc.perform(put("/api/projects/" + createdProject.getId() + "/status")
                .param("status", "testing")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("testing"));
    }

    @Test
    void testIncrementProjectVersion() throws Exception {
        // 创建测试项目
        MvcResult createResult = createTestProject();
        String createResponse = createResult.getResponse().getContentAsString();
        Project createdProject = objectMapper.readValue(createResponse, Project.class);

        // 增加版本
        mockMvc.perform(post("/api/projects/" + createdProject.getId() + "/version")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("2.0"))
                .andExpect(jsonPath("$.versionNumber").value(2));
    }

    @Test
    void testGetAllTags() throws Exception {
        // 创建测试项目
        createTestProject();

        // 获取所有标签
        mockMvc.perform(get("/api/projects/tags")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void testSearchProjectsByTag() throws Exception {
        // 创建测试项目
        createTestProject();

        // 根据标签搜索
        mockMvc.perform(get("/api/projects/tag/Java")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void testProcessProjectMarkdown() throws Exception {
        // 创建包含Markdown内容的项目
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Markdown测试项目");
        request.setDescription("包含Markdown内容的项目");
        request.setMarkdownContent("# 项目介绍\n\n这是一个 **Markdown** 测试项目。\n\n## 功能特点\n\n- 支持Markdown解析\n- 生成HTML内容\n- 提取关键词\n\n```java\npublic class Test {\n    public static void main(String[] args) {\n        System.out.println(\"Hello Markdown\");\n    }\n}\n```\n\n更多请访问 [项目主页](https://github.com/test/markdown)");
        request.setTags(Arrays.asList("Markdown", "文档"));
        request.setResumeId(testResume.getId());

        MvcResult createResult = mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Project createdProject = objectMapper.readValue(createResponse, Project.class);

        // 处理Markdown内容
        mockMvc.perform(get("/api/projects/" + createdProject.getId() + "/markdown")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headings").isArray())
                .andExpect(jsonPath("$.codeBlocks").isArray())
                .andExpect(jsonPath("$.links").isArray())
                .andExpect(jsonPath("$.keywords").isArray())
                .andExpect(jsonPath("$.readingTime").exists())
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.htmlContent").exists())
                .andExpect(jsonPath("$.isValid").value(true));
    }

    @Test
    void testBatchUpdateProjectTags() throws Exception {
        // 创建测试项目
        MvcResult createResult = createTestProject();
        String createResponse = createResult.getResponse().getContentAsString();
        Project createdProject = objectMapper.readValue(createResponse, Project.class);

        // 批量更新标签
        List<String> newTags = Arrays.asList("新标签1", "新标签2", "新标签3");
        mockMvc.perform(post("/api/projects/" + createdProject.getId() + "/tags/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTags))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(3))
                .andExpect(jsonPath("$.tags[0]").value("新标签1"));
    }

    @Test
    void testGetTagCloud() throws Exception {
        // 创建测试项目
        createTestProject();

        // 获取标签云数据
        mockMvc.perform(get("/api/projects/tags/cloud")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tagCounts").exists())
                .andExpect(jsonPath("$.maxCount").exists());
    }

    @Test
    void testSmartSearchProjects() throws Exception {
        // 创建测试项目
        createTestProject();

        // 智能搜索
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("query", "Java");
        searchParams.put("status", "development");
        searchParams.put("tags", Arrays.asList("Java"));
        searchParams.put("minPriority", 3);
        searchParams.put("isPublic", true);

        mockMvc.perform(post("/api/projects/smart-search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchParams))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects").isArray())
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.query").exists())
                .andExpect(jsonPath("$.suggestions").isArray());
    }

    @Test
    void testCreateProjectWithInvalidData() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest();
        // 故意不设置必需的name字段

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAccessProjectWithoutPermission() throws Exception {
        // 创建另一个用户和简历
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password123");
        otherUser = userRepository.save(otherUser);

        Resume otherResume = new Resume();
        otherResume.setTitle("其他用户的简历");
        otherResume.setUser(otherUser);
        otherResume.setSummary("其他用户的简历摘要");
        otherResume.setCreatedAt(LocalDateTime.now());
        otherResume.setUpdatedAt(LocalDateTime.now());
        otherResume = resumeRepository.save(otherResume);

        // 创建项目并关联到其他用户的简历
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("其他用户的项目");
        request.setDescription("属于其他用户的项目");
        request.setResumeId(otherResume.getId());

        // 尝试访问无权限的项目
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andExpect(status().isInternalServerError()); // 因为简历权限检查会抛出异常
    }

    // 辅助方法：创建测试项目
    private MvcResult createTestProject() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("测试项目");
        request.setDescription("这是一个测试项目");
        request.setTechnologyStack("Java, Spring Boot, MongoDB");
        request.setTags(Arrays.asList("Java", "Spring Boot", "MongoDB"));
        request.setStatus("development");
        request.setPriority(4);
        request.setResumeId(testResume.getId());

        return mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andReturn();
    }

    // 辅助方法：创建项目
    private void createProject(ProjectCreateRequest request) throws Exception {
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andReturn();
    }
}