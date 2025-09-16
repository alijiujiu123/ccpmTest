package com.cvagent.controller;

import com.cvagent.config.TestSecurityConfig;
import com.cvagent.service.AiServiceManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AI控制器简化测试
 * 使用@WebMvcTest进行单元测试，避免复杂的依赖问题
 */
@WebMvcTest(AiController.class)
@Import(TestSecurityConfig.class)
class AiControllerSimpleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiServiceManager aiServiceManager;

    @MockBean
    private com.cvagent.service.PromptTemplateService promptTemplateService;

    @MockBean
    private com.cvagent.service.AiMonitoringService aiMonitoringService;

    @MockBean
    private com.cvagent.repository.UserRepository userRepository;

    @Test
    void testChat() throws Exception {
        // 模拟AI服务返回
        when(aiServiceManager.chat(anyString())).thenReturn("你好！我是一个AI助手，很高兴为你服务。");

        Map<String, String> request = new HashMap<>();
        request.put("message", "你好，请介绍一下自己");

        mockMvc.perform(post("/api/ai/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
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
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("消息内容不能为空"));
    }

    @Test
    void testOptimizeResume() throws Exception {
        // 模拟简历优化服务返回
        when(aiServiceManager.optimizeResume(anyString(), anyString()))
                .thenReturn("优化后的简历内容...");

        Map<String, String> request = new HashMap<>();
        request.put("resumeContent", "张三，软件工程师，3年经验，精通Java和Spring Boot");
        request.put("jobDescription", "招聘Java开发工程师，要求3年以上经验，熟悉Spring Boot");

        mockMvc.perform(post("/api/ai/optimize-resume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optimizedResume").exists())
                .andExpect(jsonPath("$.type").value("resume_optimization"));
    }

    @Test
    void testGenerateCoverLetter() throws Exception {
        // 模拟求职信生成服务返回
        when(aiServiceManager.generateCoverLetter(anyString(), anyString(), anyString()))
                .thenReturn("尊敬的招聘经理：\n\n我非常贵公司的Java开发工程师职位...");

        Map<String, String> request = new HashMap<>();
        request.put("resumeContent", "张三，软件工程师，3年经验，精通Java和Spring Boot");
        request.put("jobDescription", "招聘Java开发工程师，要求3年以上经验");
        request.put("companyInfo", "ABC科技公司，专注于软件开发");

        mockMvc.perform(post("/api/ai/generate-cover-letter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverLetter").exists())
                .andExpect(jsonPath("$.type").value("cover_letter_generation"));
    }

    @Test
    void testImproveResumeSection() throws Exception {
        // 模拟简历章节改进服务返回
        when(aiServiceManager.improveResumeSection(anyString(), anyString()))
                .thenReturn("改进后的章节内容...");

        Map<String, String> request = new HashMap<>();
        request.put("sectionContent", "负责Java开发工作");
        request.put("sectionType", "工作经验");

        mockMvc.perform(post("/api/ai/improve-resume-section")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.improvedSection").exists())
                .andExpect(jsonPath("$.sectionType").value("工作经验"))
                .andExpect(jsonPath("$.type").value("resume_section_improvement"));
    }

    @Test
    void testGenerateProjectDescription() throws Exception {
        // 模拟项目描述生成服务返回
        when(aiServiceManager.generateProjectDescription(anyString()))
                .thenReturn("这是一个基于Spring Boot和Vue.js的电商平台项目...");

        Map<String, String> request = new HashMap<>();
        request.put("projectInfo", "电商平台项目，使用Spring Boot和Vue.js开发，负责后端API开发");

        mockMvc.perform(post("/api/ai/generate-project-description")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectDescription").exists())
                .andExpect(jsonPath("$.type").value("project_description_generation"));
    }

    @Test
    void testGetServiceStatus() throws Exception {
        // 模拟服务状态返回
        when(aiServiceManager.getServiceStatus()).thenReturn(new HashMap<String, Object>() {{
            put("chatAvailable", true);
            put("resumeOptimizationAvailable", true);
            put("activeThreads", 0);
        }});

        mockMvc.perform(get("/api/ai/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatAvailable").exists())
                .andExpect(jsonPath("$.resumeOptimizationAvailable").exists())
                .andExpect(jsonPath("$.activeThreads").exists());
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/ai/health"))
                .andExpect(status().isOk());
    }
}