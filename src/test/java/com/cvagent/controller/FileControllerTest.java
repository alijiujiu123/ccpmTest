package com.cvagent.controller;

import com.cvagent.CvAgentApplication;
import com.cvagent.dto.FileUploadResponse;
import com.cvagent.model.FileDocument;
import com.cvagent.model.User;
import com.cvagent.repository.UserRepository;
import com.cvagent.security.JwtTokenProvider;
import com.cvagent.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);

        // 创建UserPrincipal
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        // 生成认证token
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, null, userPrincipal.getAuthorities()
        );
        authToken = "Bearer " + jwtTokenProvider.generateToken(authentication);

        // 设置安全上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testUploadFile() throws Exception {
        // 创建测试文件
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-resume.pdf",
            "application/pdf",
            "PDF content for testing".getBytes()
        );

        // 执行文件上传
        MvcResult result = mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("description", "测试简历文件")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        // 验证响应
        String response = result.getResponse().getContentAsString();
        FileUploadResponse uploadResponse = objectMapper.readValue(response, FileUploadResponse.class);

        assertNotNull(uploadResponse.getId());
        assertEquals("test-resume.pdf", uploadResponse.getOriginalName());
        assertEquals("application/pdf", uploadResponse.getContentType());
        assertTrue(uploadResponse.isNewUpload());
    }

    @Test
    void testGetUserFiles() throws Exception {
        // 先上传一个文件
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-resume.pdf",
            "application/pdf",
            "PDF content for testing".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("description", "测试简历文件")
                .header("Authorization", authToken))
                .andExpect(status().isOk());

        // 获取用户文件列表
        MvcResult result = mockMvc.perform(get("/api/files")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        FileDocument[] files = objectMapper.readValue(response, FileDocument[].class);

        assertTrue(files.length > 0);
        assertEquals("test-resume.pdf", files[0].getOriginalName());
    }

    @Test
    void testGetFileInfo() throws Exception {
        // 先上传一个文件
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-resume.pdf",
            "application/pdf",
            "PDF content for testing".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("description", "测试简历文件")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        String uploadResponse = uploadResult.getResponse().getContentAsString();
        FileUploadResponse uploadData = objectMapper.readValue(uploadResponse, FileUploadResponse.class);

        // 获取文件信息
        MvcResult result = mockMvc.perform(get("/api/files/" + uploadData.getId())
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        FileDocument fileInfo = objectMapper.readValue(response, FileDocument.class);

        assertEquals(uploadData.getId(), fileInfo.getId());
        assertEquals("test-resume.pdf", fileInfo.getOriginalName());
    }

    @Test
    void testExtractTextContent() throws Exception {
        // 先上传一个文件
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-resume.pdf",
            "application/pdf",
            "PDF content for testing resume content".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("description", "测试简历文件")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        String uploadResponse = uploadResult.getResponse().getContentAsString();
        FileUploadResponse uploadData = objectMapper.readValue(uploadResponse, FileUploadResponse.class);

        // 提取文本内容
        MvcResult result = mockMvc.perform(get("/api/files/" + uploadData.getId() + "/text-content")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        Map<String, String> textContent = objectMapper.readValue(response, Map.class);

        assertNotNull(textContent.get("content"));
        assertEquals(uploadData.getId(), textContent.get("fileId"));
        assertEquals("test-resume.pdf", textContent.get("fileName"));
    }

    @Test
    void testGetFileMetadata() throws Exception {
        // 先上传一个文件
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-resume.pdf",
            "application/pdf",
            "PDF content for testing".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("description", "测试简历文件")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        String uploadResponse = uploadResult.getResponse().getContentAsString();
        FileUploadResponse uploadData = objectMapper.readValue(uploadResponse, FileUploadResponse.class);

        // 获取文件元数据
        MvcResult result = mockMvc.perform(get("/api/files/" + uploadData.getId() + "/metadata")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = objectMapper.readValue(response, Map.class);

        assertEquals(uploadData.getId(), metadata.get("fileId"));
        assertEquals("test-resume.pdf", metadata.get("originalName"));
        assertEquals("application/pdf", metadata.get("contentType"));
        assertNotNull(metadata.get("keywords"));
        assertTrue((Boolean) metadata.get("securityValid"));
    }

    @Test
    void testParseResume() throws Exception {
        // 创建简历文件内容
        String resumeContent = """
            张三 先生
            年龄：28岁

            教育背景
            北京大学 计算机科学 2015-2019

            工作经验
            阿里巴巴 高级工程师 2019-至今

            技能
            Java, Python, Spring, MySQL, Docker

            项目经验
            电商平台系统 2020-2022

            联系方式
            邮箱：zhangsan@example.com
            电话：13800138000
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "zhangsan-resume.pdf",
            "application/pdf",
            resumeContent.getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("description", "张三的简历")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        String uploadResponse = uploadResult.getResponse().getContentAsString();
        FileUploadResponse uploadData = objectMapper.readValue(uploadResponse, FileUploadResponse.class);

        // 解析简历
        MvcResult result = mockMvc.perform(post("/api/files/" + uploadData.getId() + "/parse-resume")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        Map<String, Object> resumeData = objectMapper.readValue(response, Map.class);

        assertNotNull(resumeData.get("personalInfo"));
        assertNotNull(resumeData.get("education"));
        assertNotNull(resumeData.get("workExperience"));
        assertNotNull(resumeData.get("skills"));
        assertNotNull(resumeData.get("projects"));
        assertNotNull(resumeData.get("contactInfo"));
    }

    @Test
    void testDeleteFile() throws Exception {
        // 先上传一个文件
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-resume.pdf",
            "application/pdf",
            "PDF content for testing".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("description", "测试简历文件")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        String uploadResponse = uploadResult.getResponse().getContentAsString();
        FileUploadResponse uploadData = objectMapper.readValue(uploadResponse, FileUploadResponse.class);

        // 删除文件
        mockMvc.perform(delete("/api/files/" + uploadData.getId())
                .header("Authorization", authToken))
                .andExpect(status().isNoContent());

        // 验证文件已删除
        mockMvc.perform(get("/api/files/" + uploadData.getId())
                .header("Authorization", authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUploadInvalidFileType() throws Exception {
        // 创建不支持的文件类型
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.exe",
            "application/x-executable",
            "Executable content".getBytes()
        );

        // 尝试上传不支持的文件类型
        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("description", "测试文件")
                .header("Authorization", authToken))
                .andExpect(status().isBadRequest());
    }
}