package com.cvagent.controller;

import com.cvagent.CvAgentApplication;
import com.cvagent.config.TestConfig;
import com.cvagent.dto.AuthRequest;
import com.cvagent.dto.AuthResponse;
import com.cvagent.model.User;
import com.cvagent.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = CvAgentApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
//    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        // 创建测试用户
//        testUser = new User();
//        testUser.setUsername("testuser");
//        testUser.setEmail("test@example.com");
//        testUser.setPassword("password123");
//        testUser = userRepository.save(testUser);

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
    void testChat() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("message", "你好，请介绍一下自己");

        mockMvc.perform(post("/api/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists())
                .andExpect(jsonPath("$.type").value("chat"));
    }

    @Test
    void testChatWithEmptyMessage() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("message", "");

        mockMvc.perform(post("/api/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("消息内容不能为空"));
    }

    @Test
    void testOptimizeResume() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("resumeContent", "张三，软件工程师，3年经验，精通Java和Spring Boot");
        request.put("jobDescription", "招聘Java开发工程师，要求3年以上经验，熟悉Spring Boot");

        mockMvc.perform(post("/api/ai/optimize-resume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optimizedResume").exists())
                .andExpect(jsonPath("$.type").value("resume_optimization"));
    }

    @Test
    void testOptimizeResumeWithEmptyContent() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("resumeContent", "");
        request.put("jobDescription", "招聘Java开发工程师");

        mockMvc.perform(post("/api/ai/optimize-resume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("简历内容不能为空"));
    }

    @Test
    void testGenerateCoverLetter() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("resumeContent", "张三，软件工程师，3年经验，精通Java和Spring Boot");
        request.put("jobDescription", "招聘Java开发工程师，要求3年以上经验");
        request.put("companyInfo", "ABC科技公司，专注于软件开发");

        mockMvc.perform(post("/api/ai/generate-cover-letter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverLetter").exists())
                .andExpect(jsonPath("$.type").value("cover_letter_generation"));
    }

    @Test
    void testGenerateCoverLetterWithMissingInfo() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("resumeContent", "张三，软件工程师");
        request.put("jobDescription", "");

        mockMvc.perform(post("/api/ai/generate-cover-letter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("职位描述不能为空"));
    }

    @Test
    void testImproveResumeSection() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("sectionContent", "负责Java开发工作");
        request.put("sectionType", "工作经验");

        mockMvc.perform(post("/api/ai/improve-resume-section")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.improvedSection").exists())
                .andExpect(jsonPath("$.sectionType").value("工作经验"))
                .andExpect(jsonPath("$.type").value("resume_section_improvement"));
    }

    @Test
    void testGenerateProjectDescription() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("projectInfo", "电商平台项目，使用Spring Boot和Vue.js开发，负责后端API开发");

        mockMvc.perform(post("/api/ai/generate-project-description")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectDescription").exists())
                .andExpect(jsonPath("$.type").value("project_description_generation"));
    }

    @Test
    void testBatchProcess() throws Exception {
        Map<String, Object> batchRequest = new HashMap<>();
        Map<String, Object> requests = new HashMap<>();

        // 聊天请求
        Map<String, String> chatRequest = new HashMap<>();
        chatRequest.put("type", "chat");
        chatRequest.put("message", "你好");
        requests.put("chat1", chatRequest);

        // 简历优化请求
        Map<String, String> resumeRequest = new HashMap<>();
        resumeRequest.put("type", "optimize_resume");
        resumeRequest.put("resumeContent", "软件工程师");
        resumeRequest.put("jobDescription", "招聘Java工程师");
        requests.put("resume1", resumeRequest);

        batchRequest.put("requests", requests);

        mockMvc.perform(post("/api/ai/batch-process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").exists())
                .andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void testGetServiceStatus() throws Exception {
        mockMvc.perform(get("/api/ai/status")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatAvailable").exists())
                .andExpect(jsonPath("$.resumeOptimizationAvailable").exists())
                .andExpect(jsonPath("$.creativeWritingAvailable").exists())
                .andExpect(jsonPath("$.activeThreads").exists());
    }

    @Test
    void testGetStatistics() throws Exception {
        mockMvc.perform(get("/api/ai/statistics")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").exists())
                .andExpect(jsonPath("$.totalErrors").exists())
                .andExpect(jsonPath("$.serviceStats").exists());
    }

    @Test
    void testGetHealth() throws Exception {
        mockMvc.perform(get("/api/ai/health")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.successRate").exists())
                .andExpect(jsonPath("$.checkedAt").exists());
    }

    @Test
    void testGetPerformanceReport() throws Exception {
        mockMvc.perform(get("/api/ai/performance-report")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").exists())
                .andExpect(jsonPath("$.averageResponseTime").exists())
                .andExpect(jsonPath("$.successRate").exists());
    }

    @Test
    void testGetRecentLogs() throws Exception {
        mockMvc.perform(get("/api/ai/recent-logs")
                .param("limit", "5")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetErrorLogs() throws Exception {
        mockMvc.perform(get("/api/ai/error-logs")
                .param("limit", "5")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetServiceRanking() throws Exception {
        mockMvc.perform(get("/api/ai/service-ranking")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetPromptTemplates() throws Exception {
        mockMvc.perform(get("/api/ai/prompt-templates")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetPromptTemplateDetails() throws Exception {
        mockMvc.perform(get("/api/ai/prompt-templates/resume-optimization")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("resume-optimization"))
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.variables").exists());
    }

    @Test
    void testUsePromptTemplate() throws Exception {
        Map<String, Object> variables = new HashMap<>();
        variables.put("resumeContent", "软件工程师");
        variables.put("jobDescription", "招聘Java工程师");

        mockMvc.perform(post("/api/ai/prompt-templates/resume-optimization/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(variables))
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prompt").exists())
                .andExpect(jsonPath("$.templateName").value("resume-optimization"));
    }

    @Test
    void testResetStatistics() throws Exception {
        mockMvc.perform(post("/api/ai/reset-statistics")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("统计数据已重置"));
    }

    @Test
    void testCleanupLogs() throws Exception {
        mockMvc.perform(post("/api/ai/cleanup-logs")
                .param("daysToKeep", "7")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("清理了 7 天前的日志"));
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("message", "你好");

        mockMvc.perform(post("/api/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testInvalidBatchRequest() throws Exception {
        Map<String, Object> batchRequest = new HashMap<>();
        batchRequest.put("requests", new HashMap<>()); // 空请求

        mockMvc.perform(post("/api/ai/batch-process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest))
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("处理请求不能为空"));
    }

    @Test
    void testNonExistentPromptTemplate() throws Exception {
        mockMvc.perform(get("/api/ai/prompt-templates/non-existent")
                .header("Authorization", authToken))
                .andExpect(status().isNotFound());
    }
}