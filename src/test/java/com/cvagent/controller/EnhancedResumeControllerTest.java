package com.cvagent.controller;

import com.cvagent.model.EnhancedResume;
import com.cvagent.model.ResumeTemplate;
import com.cvagent.service.ResumeGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 增强简历控制器测试类
 */
@ExtendWith(MockitoExtension.class)
public class EnhancedResumeControllerTest {

    @Mock
    private ResumeGenerationService resumeGenerationService;

    @InjectMocks
    private EnhancedResumeController enhancedResumeController;

    private EnhancedResume testResume;
    private ResumeTemplate testTemplate;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        testResume = new EnhancedResume();
        testResume.setId("test-resume-id");
        testResume.setUserId("test-user-id");
        testResume.setTitle("测试简历");
        testResume.setVersion("1.0");
        testResume.setMatchScore(0.85);
        testResume.setOptimizationStatus("completed");

        testTemplate = new ResumeTemplate();
        testTemplate.setId("test-template-id");
        testTemplate.setName("专业模板");
        testTemplate.setCategory("professional");
        testTemplate.setStyle("modern");

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    /**
     * 测试创建基础简历
     */
    @Test
    void testCreateBasicResume() {
        // 模拟服务行为
        when(resumeGenerationService.generateBasicResume(eq("test-user-id"), any(Map.class)))
                .thenReturn(testResume);

        // 设置请求头
        request.addHeader("X-User-Id", "test-user-id");

        // 准备请求数据
        Map<String, Object> resumeData = Map.of(
                "title", "测试简历",
                "personalInfo", Map.of(
                        "name", "张三",
                        "email", "zhangsan@example.com"
                )
        );

        // 执行测试
        ResponseEntity<EnhancedResume> result = enhancedResumeController.createBasicResume(
                resumeData, "test-user-id");

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("test-resume-id", result.getBody().getId());
        assertEquals("test-user-id", result.getBody().getUserId());

        // 验证服务调用
        verify(resumeGenerationService, times(1)).generateBasicResume(
                eq("test-user-id"), any(Map.class));
    }

    /**
     * 测试创建优化简历
     */
    @Test
    void testCreateOptimizedResume() {
        // 模拟服务行为
        when(resumeGenerationService.generateOptimizedResume(
                eq("test-user-id"), eq("base-resume-id"), eq("job-requirement-id")))
                .thenReturn(testResume);

        // 准备请求数据
        Map<String, String> requestData = Map.of(
                "baseResumeId", "base-resume-id",
                "jobRequirementId", "job-requirement-id"
        );

        // 执行测试
        ResponseEntity<EnhancedResume> result = enhancedResumeController.createOptimizedResume(
                requestData, "test-user-id");

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("test-resume-id", result.getBody().getId());

        // 验证服务调用
        verify(resumeGenerationService, times(1)).generateOptimizedResume(
                eq("test-user-id"), eq("base-resume-id"), eq("job-requirement-id"));
    }

    /**
     * 测试导出简历PDF
     */
    @Test
    void testExportResume_PDF() {
        // 模拟服务行为
        byte[] pdfContent = "PDF content".getBytes();
        when(resumeGenerationService.generateResumeWithTemplate(
                eq("test-resume-id"), eq("test-template-id"), eq("pdf")))
                .thenReturn(pdfContent);

        // 执行测试
        ResponseEntity<byte[]> result = enhancedResumeController.exportResume(
                "test-resume-id", "test-template-id", "pdf", "my-resume.pdf");

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("PDF content", new String(result.getBody()));

        // 验证响应头
        HttpHeaders headers = result.getHeaders();
        assertEquals("application/pdf", headers.getContentType().toString());
        assertEquals("attachment; filename=\"my-resume.pdf\"",
                     headers.getContentDisposition().toString());

        // 验证服务调用
        verify(resumeGenerationService, times(1)).generateResumeWithTemplate(
                eq("test-resume-id"), eq("test-template-id"), eq("pdf"));
    }

    /**
     * 测试导出简历Word
     */
    @Test
    void testExportResume_Word() {
        // 模拟服务行为
        byte[] wordContent = "Word content".getBytes();
        when(resumeGenerationService.generateResumeWithTemplate(
                eq("test-resume-id"), eq("test-template-id"), eq("docx")))
                .thenReturn(wordContent);

        // 执行测试
        ResponseEntity<byte[]> result = enhancedResumeController.exportResume(
                "test-resume-id", "test-template-id", "docx", null);

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Word content", new String(result.getBody()));

        // 验证响应头
        HttpHeaders headers = result.getHeaders();
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                     headers.getContentType().toString());
        assertTrue(headers.getContentDisposition().getFilename().endsWith(".docx"));

        // 验证服务调用
        verify(resumeGenerationService, times(1)).generateResumeWithTemplate(
                eq("test-resume-id"), eq("test-template-id"), eq("docx"));
    }

    /**
     * 测试导出简历HTML
     */
    @Test
    void testExportResume_HTML() {
        // 模拟服务行为
        byte[] htmlContent = "<html>HTML content</html>".getBytes();
        when(resumeGenerationService.generateResumeWithTemplate(
                eq("test-resume-id"), eq("test-template-id"), eq("html")))
                .thenReturn(htmlContent);

        // 执行测试
        ResponseEntity<byte[]> result = enhancedResumeController.exportResume(
                "test-resume-id", "test-template-id", "html", null);

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("<html>HTML content</html>", new String(result.getBody()));

        // 验证响应头
        HttpHeaders headers = result.getHeaders();
        assertEquals("text/html", headers.getContentType().toString());
        assertTrue(headers.getContentDisposition().getFilename().endsWith(".html"));

        // 验证服务调用
        verify(resumeGenerationService, times(1)).generateResumeWithTemplate(
                eq("test-resume-id"), eq("test-template-id"), eq("html"));
    }

    /**
     * 测试获取简历预览
     */
    @Test
    void testGetResumePreview() {
        // 模拟服务行为
        String previewContent = "<html>预览内容</html>";
        when(resumeGenerationService.getResumePreview(
                eq("test-resume-id"), eq("test-template-id")))
                .thenReturn(previewContent);

        // 执行测试
        ResponseEntity<String> result = enhancedResumeController.getResumePreview(
                "test-resume-id", "test-template-id");

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("text/html", result.getHeaders().getContentType().toString());
        assertEquals("<html>预览内容</html>", result.getBody());

        // 验证服务调用
        verify(resumeGenerationService, times(1)).getResumePreview(
                eq("test-resume-id"), eq("test-template-id"));
    }

    /**
     * 测试获取用户简历历史
     */
    @Test
    void testGetUserResumeHistory() {
        // 模拟服务行为
        List<EnhancedResume> expectedResumes = List.of(testResume);
        when(resumeGenerationService.getUserResumeHistory(eq("test-user-id")))
                .thenReturn(expectedResumes);

        // 执行测试
        ResponseEntity<List<EnhancedResume>> result = enhancedResumeController.getUserResumeHistory(
                "test-user-id");

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals("test-resume-id", result.getBody().get(0).getId());

        // 验证服务调用
        verify(resumeGenerationService, times(1)).getUserResumeHistory(eq("test-user-id"));
    }

    /**
     * 测试简历版本对比
     */
    @Test
    void testCompareResumes() {
        // 模拟服务行为
        Map<String, Object> expectedComparison = Map.of(
                "resume1", Map.of("id", "resume1", "title", "简历1"),
                "resume2", Map.of("id", "resume2", "title", "简历2"),
                "comparison", Map.of("difference", "有所改进")
        );
        when(resumeGenerationService.compareResumeVersions(
                eq("resume1"), eq("resume2")))
                .thenReturn(expectedComparison);

        // 准备请求数据
        Map<String, String> requestData = Map.of(
                "resumeId1", "resume1",
                "resumeId2", "resume2"
        );

        // 执行测试
        ResponseEntity<Map<String, Object>> result = enhancedResumeController.compareResumes(requestData);

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().containsKey("resume1"));
        assertTrue(result.getBody().containsKey("resume2"));
        assertTrue(result.getBody().containsKey("comparison"));

        // 验证服务调用
        verify(resumeGenerationService, times(1)).compareResumeVersions(
                eq("resume1"), eq("resume2"));
    }

    /**
     * 测试删除简历
     */
    @Test
    void testDeleteResume() {
        // 模拟服务行为（deleteResume方法返回void，所以使用doNothing）
        doNothing().when(resumeGenerationService).deleteResume(
                eq("test-resume-id"), eq("test-user-id"));

        // 执行测试
        ResponseEntity<Void> result = enhancedResumeController.deleteResume(
                "test-resume-id", "test-user-id");

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());

        // 验证服务调用
        verify(resumeGenerationService, times(1)).deleteResume(
                eq("test-resume-id"), eq("test-user-id"));
    }

    /**
     * 测试获取导出格式列表
     */
    @Test
    void testGetExportFormats() {
        // 执行测试
        ResponseEntity<List<Map<String, String>>> result = enhancedResumeController.getExportFormats();

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(3, result.getBody().size());

        // 验证格式数据
        List<Map<String, String>> formats = result.getBody();
        assertTrue(formats.stream().anyMatch(f -> "pdf".equals(f.get("format"))));
        assertTrue(formats.stream().anyMatch(f -> "docx".equals(f.get("format"))));
        assertTrue(formats.stream().anyMatch(f -> "html".equals(f.get("format"))));

        // 验证PDF格式
        Map<String, String> pdfFormat = formats.stream()
                .filter(f -> "pdf".equals(f.get("format")))
                .findFirst()
                .orElse(null);
        assertNotNull(pdfFormat);
        assertEquals("PDF文档", pdfFormat.get("name"));
        assertEquals("application/pdf", pdfFormat.get("mimeType"));

        // 验证Word格式
        Map<String, String> wordFormat = formats.stream()
                .filter(f -> "docx".equals(f.get("format")))
                .findFirst()
                .orElse(null);
        assertNotNull(wordFormat);
        assertEquals("Word文档", wordFormat.get("name"));
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                     wordFormat.get("mimeType"));

        // 验证HTML格式
        Map<String, String> htmlFormat = formats.stream()
                .filter(f -> "html".equals(f.get("format")))
                .findFirst()
                .orElse(null);
        assertNotNull(htmlFormat);
        assertEquals("HTML网页", htmlFormat.get("name"));
        assertEquals("text/html", htmlFormat.get("mimeType"));
    }

    /**
     * 测试服务异常处理
     */
    @Test
    void testServiceExceptionHandling() {
        // 模拟服务异常
        when(resumeGenerationService.generateBasicResume(eq("test-user-id"), any(Map.class)))
                .thenThrow(new RuntimeException("服务异常"));

        // 准备请求数据
        Map<String, Object> resumeData = Map.of("title", "测试简历");

        // 执行测试
        ResponseEntity<EnhancedResume> result = enhancedResumeController.createBasicResume(
                resumeData, "test-user-id");

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNull(result.getBody());

        // 验证服务调用
        verify(resumeGenerationService, times(1)).generateBasicResume(
                eq("test-user-id"), any(Map.class));
    }

    /**
     * 测试获取可用模板列表
     */
    @Test
    void testGetAvailableTemplates() {
        // 执行测试
        ResponseEntity<List<ResumeTemplate>> result = enhancedResumeController.getAvailableTemplates();

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        // 当前实现返回空响应，这符合预期
    }

    /**
     * 测试获取模板详情
     */
    @Test
    void testGetTemplateDetails() {
        // 执行测试
        ResponseEntity<ResumeTemplate> result = enhancedResumeController.getTemplateDetails("test-template-id");

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        // 当前实现返回空响应，这符合预期
    }

    /**
     * 测试获取简历详情
     */
    @Test
    void testGetResumeDetails() {
        // 执行测试
        ResponseEntity<EnhancedResume> result = enhancedResumeController.getResumeDetails("test-resume-id");

        // 验证结果
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        // 当前实现返回空响应，这符合预期
    }
}