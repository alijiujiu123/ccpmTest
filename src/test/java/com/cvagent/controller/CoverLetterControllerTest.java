package com.cvagent.controller;

import com.cvagent.model.CoverLetter;
import com.cvagent.model.CoverLetterTemplate;
import com.cvagent.service.CoverLetterExportService;
import com.cvagent.service.CoverLetterGenerationService;
import com.cvagent.repository.CoverLetterRepository;
import com.cvagent.repository.CoverLetterTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 求职信控制器测试
 */
@ExtendWith(MockitoExtension.class)
class CoverLetterControllerTest {

    @Mock
    private CoverLetterGenerationService coverLetterGenerationService;

    @Mock
    private CoverLetterExportService coverLetterExportService;

    @Mock
    private CoverLetterRepository coverLetterRepository;

    @Mock
    private CoverLetterTemplateRepository coverLetterTemplateRepository;

    @InjectMocks
    private CoverLetterController coverLetterController;

    private CoverLetter testCoverLetter;
    private CoverLetterTemplate testTemplate;
    private CoverLetterExportService.ExportResult testExportResult;

    @BeforeEach
    void setUp() {
        // 创建测试求职信
        testCoverLetter = new CoverLetter();
        testCoverLetter.setId("cl-1");
        testCoverLetter.setUserId("user-1");
        testCoverLetter.setTitle("高级软件工程师求职信");
        testCoverLetter.setCompanyName("科技有限公司");
        testCoverLetter.setPosition("高级软件工程师");
        testCoverLetter.setStatus("draft");
        testCoverLetter.setGeneratedBy("template_based");
        testCoverLetter.setGeneratedAt(LocalDateTime.now());

        // 设置求职信内容
        CoverLetter.CoverLetterContent content = new CoverLetter.CoverLetterContent();
        content.setSalutation("尊敬的招聘经理：");
        content.setOpeningParagraph("我对贵公司的高级软件工程师职位非常感兴趣。");
        content.setBodyParagraphs("我有5年Java开发经验，熟悉Spring Boot和MongoDB。");
        content.setClosingParagraph("期待您的回复。");
        testCoverLetter.setContent(content);

        // 创建测试模板
        testTemplate = new CoverLetterTemplate();
        testTemplate.setId("template-1");
        testTemplate.setName("专业求职信模板");
        testTemplate.setCategory("professional");
        testTemplate.setStyle("formal");
        testTemplate.setIsActive(true);

        // 创建测试导出结果
        testExportResult = new CoverLetterExportService.ExportResult(
            "<html><body>Test HTML Content</body></html>".getBytes(),
            "text/html",
            "test_cover_letter.html"
        );
    }

    @Test
    void getUserCoverLetters_WithUserIdOnly_ShouldReturnUserCoverLetters() {
        // 模拟依赖
        when(coverLetterRepository.findByUserIdOrderByGeneratedAtDesc("user-1"))
                .thenReturn(Arrays.asList(testCoverLetter));

        // 执行测试
        ResponseEntity<List<CoverLetter>> response = coverLetterController.getUserCoverLetters(
            "user-1", null, null);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("cl-1", response.getBody().get(0).getId());
        assertEquals("user-1", response.getBody().get(0).getUserId());

        // 验证调用
        verify(coverLetterRepository).findByUserIdOrderByGeneratedAtDesc("user-1");
    }

    @Test
    void getUserCoverLetters_WithStatusFilter_ShouldReturnFilteredCoverLetters() {
        // 模拟依赖
        when(coverLetterRepository.findByUserIdAndStatusOrderByGeneratedAtDesc("user-1", "draft"))
                .thenReturn(Arrays.asList(testCoverLetter));

        // 执行测试
        ResponseEntity<List<CoverLetter>> response = coverLetterController.getUserCoverLetters(
            "user-1", "draft", null);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("draft", response.getBody().get(0).getStatus());

        // 验证调用
        verify(coverLetterRepository).findByUserIdAndStatusOrderByGeneratedAtDesc("user-1", "draft");
    }

    @Test
    void getCoverLetterById_WithValidId_ShouldReturnCoverLetter() {
        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(Optional.of(testCoverLetter));

        // 执行测试
        ResponseEntity<CoverLetter> response = coverLetterController.getCoverLetterById("cl-1");

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("cl-1", response.getBody().getId());
        assertEquals("user-1", response.getBody().getUserId());

        // 验证查看次数增加
        verify(coverLetterRepository).incrementViewCount("cl-1");
    }

    @Test
    void getCoverLetterById_WithInvalidId_ShouldThrowException() {
        // 模拟依赖
        when(coverLetterRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // 验证异常
        assertThrows(RuntimeException.class, () -> {
            coverLetterController.getCoverLetterById("invalid-id");
        });

        // 验证没有查看次数增加
        verify(coverLetterRepository, never()).incrementViewCount(anyString());
    }

    @Test
    void createBasicCoverLetter_WithValidData_ShouldCreateCoverLetter() {
        // 准备请求数据
        Map<String, Object> request = Map.of(
            "userId", "user-1",
            "templateId", "template-1",
            "letterData", Map.of(
                "title", "测试求职信",
                "companyName", "测试公司",
                "position", "测试职位",
                "recipientName", "招聘经理"
            )
        );

        // 模拟服务返回
        when(coverLetterGenerationService.generateBasicCoverLetter(
            eq("user-1"), eq("template-1"), any(Map.class)))
                .thenReturn(testCoverLetter);

        // 执行测试
        ResponseEntity<CoverLetter> response = coverLetterController.createBasicCoverLetter(request);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("cl-1", response.getBody().getId());
        assertEquals("user-1", response.getBody().getUserId());

        // 验证服务调用
        verify(coverLetterGenerationService).generateBasicCoverLetter(
            eq("user-1"), eq("template-1"), any(Map.class));
    }

    @Test
    void createPersonalizedCoverLetter_WithValidData_ShouldCreateCoverLetter() {
        // 准备请求数据
        Map<String, Object> request = Map.of(
            "userId", "user-1",
            "resumeId", "resume-1",
            "jobRequirementId", "job-1",
            "templateId", "template-1",
            "customData", Map.of(
                "highlightSkills", Arrays.asList("Java", "Spring Boot"),
                "experienceLevel", "高级"
            )
        );

        // 模拟服务返回
        when(coverLetterGenerationService.generatePersonalizedCoverLetter(
            eq("user-1"), eq("resume-1"), eq("job-1"), eq("template-1"), any(Map.class)))
                .thenReturn(testCoverLetter);

        // 执行测试
        ResponseEntity<CoverLetter> response = coverLetterController.createPersonalizedCoverLetter(request);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("cl-1", response.getBody().getId());

        // 验证服务调用
        verify(coverLetterGenerationService).generatePersonalizedCoverLetter(
            eq("user-1"), eq("resume-1"), eq("job-1"), eq("template-1"), any(Map.class));
    }

    @Test
    void optimizeCoverLetter_WithValidData_ShouldOptimizeCoverLetter() {
        // 准备优化选项
        Map<String, Object> optimizationOptions = Map.of(
            "improveProfessionalism", true,
            "addKeywords", Arrays.asList("Java", "Spring Boot"),
            "enhanceStructure", true
        );

        // 创建优化后的求职信
        CoverLetter optimizedCoverLetter = new CoverLetter();
        optimizedCoverLetter.setId("cl-1");
        optimizedCoverLetter.setOptimizationStatus("processing");
        optimizedCoverLetter.setAiOptimized(true);

        // 模拟服务返回
        when(coverLetterGenerationService.optimizeCoverLetter("cl-1", optimizationOptions))
                .thenReturn(optimizedCoverLetter);

        // 执行测试
        ResponseEntity<CoverLetter> response = coverLetterController.optimizeCoverLetter("cl-1", optimizationOptions);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("cl-1", response.getBody().getId());
        assertEquals("processing", response.getBody().getOptimizationStatus());
        assertTrue(response.getBody().getAiOptimized());

        // 验证服务调用
        verify(coverLetterGenerationService).optimizeCoverLetter("cl-1", optimizationOptions);
    }

    @Test
    void customizeCoverLetter_WithValidData_ShouldCustomizeCoverLetter() {
        // 准备定制化数据
        Map<String, Object> customizations = Map.of(
            "salutation", "尊敬的HR经理：",
            "openingParagraph", "我对贵公司的技术职位非常感兴趣。",
            "tone", "friendly",
            "formalityLevel", 6
        );

        // 创建定制化后的求职信
        CoverLetter customizedCoverLetter = new CoverLetter();
        customizedCoverLetter.setId("cl-1");
        customizedCoverLetter.setTone("friendly");
        customizedCoverLetter.setFormalityLevel(6);

        // 模拟服务返回
        when(coverLetterGenerationService.customizeCoverLetter("cl-1", customizations))
                .thenReturn(customizedCoverLetter);

        // 执行测试
        ResponseEntity<CoverLetter> response = coverLetterController.customizeCoverLetter("cl-1", customizations);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("cl-1", response.getBody().getId());
        assertEquals("friendly", response.getBody().getTone());
        assertEquals(6, response.getBody().getFormalityLevel());

        // 验证服务调用
        verify(coverLetterGenerationService).customizeCoverLetter("cl-1", customizations);
    }

    @Test
    void copyCoverLetter_WithValidData_ShouldCopyCoverLetter() {
        // 准备请求数据
        Map<String, String> request = Map.of("newTitle", "求职信副本");

        // 创建复制的求职信
        CoverLetter copiedCoverLetter = new CoverLetter();
        copiedCoverLetter.setId("cl-2");
        copiedCoverLetter.setTitle("求职信副本");
        copiedCoverLetter.setGeneratedBy("copied");

        // 模拟服务返回
        when(coverLetterGenerationService.copyCoverLetter("cl-1", "求职信副本"))
                .thenReturn(copiedCoverLetter);

        // 执行测试
        ResponseEntity<CoverLetter> response = coverLetterController.copyCoverLetter("cl-1", request);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("cl-2", response.getBody().getId());
        assertEquals("求职信副本", response.getBody().getTitle());
        assertEquals("copied", response.getBody().getGeneratedBy());

        // 验证服务调用
        verify(coverLetterGenerationService).copyCoverLetter("cl-1", "求职信副本");
    }

    @Test
    void deleteCoverLetter_WithValidId_ShouldDeleteCoverLetter() {
        // 模拟依赖
        when(coverLetterRepository.existsById("cl-1")).thenReturn(true);

        // 执行测试
        ResponseEntity<Void> response = coverLetterController.deleteCoverLetter("cl-1");

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证删除调用
        verify(coverLetterRepository).deleteById("cl-1");
    }

    @Test
    void deleteCoverLetter_WithInvalidId_ShouldThrowException() {
        // 模拟依赖
        when(coverLetterRepository.existsById("invalid-id")).thenReturn(false);

        // 验证异常
        assertThrows(RuntimeException.class, () -> {
            coverLetterController.deleteCoverLetter("invalid-id");
        });

        // 验证没有删除调用
        verify(coverLetterRepository, never()).deleteById(anyString());
    }

    @Test
    void updateCoverLetterStatus_WithValidData_ShouldUpdateStatus() {
        // 准备请求数据
        Map<String, String> request = Map.of("status", "sent");

        // 执行测试
        ResponseEntity<Void> response = coverLetterController.updateCoverLetterStatus("cl-1", request);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证状态更新调用
        verify(coverLetterRepository).updateStatus("cl-1", "sent");
    }

    @Test
    void getCoverLetterSuggestions_WithValidId_ShouldReturnSuggestions() {
        // 准备建议数据
        Map<String, Object> suggestions = Map.of(
            "quality_improvement", "建议增加具体的工作成果和数据支撑",
            "relevance_improvement", "建议增加与职位要求相关的技能和经验描述",
            "ai_optimization", "建议使用AI优化功能提升求职信质量"
        );

        // 模拟服务返回
        when(coverLetterGenerationService.getCoverLetterSuggestions("cl-1")).thenReturn(suggestions);

        // 执行测试
        ResponseEntity<Map<String, Object>> response = coverLetterController.getCoverLetterSuggestions("cl-1");

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertTrue(response.getBody().containsKey("quality_improvement"));
        assertTrue(response.getBody().containsKey("relevance_improvement"));
        assertTrue(response.getBody().containsKey("ai_optimization"));

        // 验证服务调用
        verify(coverLetterGenerationService).getCoverLetterSuggestions("cl-1");
    }

    @Test
    void searchCoverLetters_WithValidKeyword_ShouldReturnSearchResults() {
        // 模拟依赖
        when(coverLetterRepository.searchByUserAndKeyword("user-1", "软件工程师"))
                .thenReturn(Arrays.asList(testCoverLetter));

        // 执行测试
        ResponseEntity<List<CoverLetter>> response = coverLetterController.searchCoverLetters(
            "user-1", "软件工程师");

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("cl-1", response.getBody().get(0).getId());

        // 验证搜索调用
        verify(coverLetterRepository).searchByUserAndKeyword("user-1", "软件工程师");
    }

    @Test
    void exportCoverLetter_WithValidFormat_ShouldExportCoverLetter() {
        // 模拟导出服务返回
        when(coverLetterExportService.exportCoverLetter("cl-1", "html")).thenReturn(testExportResult);

        // 执行测试
        ResponseEntity<byte[]> response = coverLetterController.exportCoverLetter("cl-1", "html");

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("<html><body>Test HTML Content</body></html>", new String(response.getBody()));

        // 验证头信息
        assertNotNull(response.getHeaders().getContentType());
        assertEquals("text/html", response.getHeaders().getContentType().toString());
        assertNotNull(response.getHeaders().getContentDisposition());
        assertTrue(response.getHeaders().getContentDisposition().getFilename().contains(".html"));

        // 验证导出服务调用
        verify(coverLetterExportService).exportCoverLetter("cl-1", "html");
    }

    @Test
    void exportCombinedPackage_WithValidData_ShouldExportCombinedPackage() {
        // 准备请求数据
        Map<String, Object> request = Map.of(
            "format", "html",
            "resumeContent", "<div>简历内容</div>"
        );

        // 模拟导出服务返回
        when(coverLetterExportService.exportCombinedPackage("cl-1", "<div>简历内容</div>", "html"))
                .thenReturn(testExportResult);

        // 执行测试
        ResponseEntity<byte[]> response = coverLetterController.exportCombinedPackage("cl-1", request);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // 验证导出服务调用
        verify(coverLetterExportService).exportCombinedPackage("cl-1", "<div>简历内容</div>", "html");
    }

    @Test
    void getCoverLetterTemplates_WithNoFilters_ShouldReturnAllTemplates() {
        // 模拟依赖
        when(coverLetterTemplateRepository.findByIsActiveTrueOrderByUsageCountDesc())
                .thenReturn(Arrays.asList(testTemplate));

        // 执行测试
        ResponseEntity<List<CoverLetterTemplate>> response = coverLetterController.getCoverLetterTemplates(
            null, null, null);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("template-1", response.getBody().get(0).getId());

        // 验证调用
        verify(coverLetterTemplateRepository).findByIsActiveTrueOrderByUsageCountDesc();
    }

    @Test
    void getCoverLetterTemplates_WithCategoryFilter_ShouldReturnFilteredTemplates() {
        // 模拟依赖
        when(coverLetterTemplateRepository.findByCategory("professional"))
                .thenReturn(Arrays.asList(testTemplate));

        // 执行测试
        ResponseEntity<List<CoverLetterTemplate>> response = coverLetterController.getCoverLetterTemplates(
            "professional", null, null);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        // 验证调用
        verify(coverLetterTemplateRepository).findByCategory("professional");
    }

    @Test
    void getCoverLetterTemplates_WithKeywordSearch_ShouldReturnSearchResults() {
        // 模拟依赖
        when(coverLetterTemplateRepository.searchByKeyword("专业"))
                .thenReturn(Arrays.asList(testTemplate));

        // 执行测试
        ResponseEntity<List<CoverLetterTemplate>> response = coverLetterController.getCoverLetterTemplates(
            null, null, "专业");

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        // 验证调用
        verify(coverLetterTemplateRepository).searchByKeyword("专业");
    }

    @Test
    void getUserCoverLetterStats_WithValidUserId_ShouldReturnStats() {
        // 模拟统计数据
        when(coverLetterRepository.countByUserId("user-1")).thenReturn(10L);
        when(coverLetterRepository.countByUserIdAndStatus("user-1", "draft")).thenReturn(3L);
        when(coverLetterRepository.countByUserIdAndStatus("user-1", "ready")).thenReturn(4L);
        when(coverLetterRepository.countByUserIdAndStatus("user-1", "sent")).thenReturn(3L);
        when(coverLetterRepository.findByUserIdAndAiOptimizedTrueOrderByGeneratedAtDesc("user-1"))
                .thenReturn(Arrays.asList(testCoverLetter));
        when(coverLetterRepository.findByUserIdAndQualityScoreGreaterThanOrderByQualityScoreDesc("user-1", 0.8))
                .thenReturn(Arrays.asList(testCoverLetter));

        // 执行测试
        ResponseEntity<Map<String, Object>> response = coverLetterController.getUserCoverLetterStats("user-1");

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> stats = response.getBody();
        assertEquals(10L, stats.get("total"));
        assertEquals(3L, stats.get("draft"));
        assertEquals(4L, stats.get("ready"));
        assertEquals(3L, stats.get("sent"));
        assertEquals(1, stats.get("aiOptimized"));
        assertEquals(1, stats.get("highQuality"));
    }

    @Test
    void getHighMatchCoverLetters_WithMinScore_ShouldReturnHighMatchLetters() {
        // 模拟依赖
        when(coverLetterRepository.findByUserIdAndMatchScoreGreaterThanOrderByMatchScoreDesc("user-1", 0.8))
                .thenReturn(Arrays.asList(testCoverLetter));

        // 执行测试
        ResponseEntity<List<CoverLetter>> response = coverLetterController.getHighMatchCoverLetters(
            "user-1", 0.8);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        // 验证调用
        verify(coverLetterRepository).findByUserIdAndMatchScoreGreaterThanOrderByMatchScoreDesc("user-1", 0.8);
    }

    @Test
    void getCoverLettersNeedingOptimization_ShouldReturnUnoptimizedLetters() {
        // 模拟依赖
        when(coverLetterRepository.findByUserIdAndAiOptimizedFalseOrderByGeneratedAtDesc("user-1"))
                .thenReturn(Arrays.asList(testCoverLetter));

        // 执行测试
        ResponseEntity<List<CoverLetter>> response = coverLetterController.getCoverLettersNeedingOptimization("user-1");

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        // 验证调用
        verify(coverLetterRepository).findByUserIdAndAiOptimizedFalseOrderByGeneratedAtDesc("user-1");
    }

    @Test
    void batchUpdateCoverLetterStatus_WithValidData_ShouldBatchUpdateStatus() {
        // 准备请求数据
        Map<String, Object> request = Map.of(
            "coverLetterIds", Arrays.asList("cl-1", "cl-2", "cl-3"),
            "status", "archived"
        );

        // 执行测试
        ResponseEntity<Void> response = coverLetterController.batchUpdateCoverLetterStatus(request);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证批量更新调用
        verify(coverLetterRepository).batchUpdateStatus(
            Arrays.asList("cl-1", "cl-2", "cl-3"), "archived");
    }

    @Test
    void rateCoverLetter_WithValidRating_ShouldUpdateRating() {
        // 准备评分数据
        Map<String, Object> request = Map.of("rating", 4.5);

        // 执行测试
        ResponseEntity<Void> response = coverLetterController.rateCoverLetter("cl-1", request);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证评分更新调用
        verify(coverLetterRepository).updateUserRating("cl-1", 4.5);
    }

    @Test
    void rateCoverLetter_WithInvalidRating_ShouldNotUpdateRating() {
        // 准备无效评分数据
        Map<String, Object> request = Map.of("rating", 6.0); // 超出范围

        // 执行测试
        ResponseEntity<Void> response = coverLetterController.rateCoverLetter("cl-1", request);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 验证没有评分更新调用
        verify(coverLetterRepository, never()).updateUserRating(anyString(), anyDouble());
    }

    @Test
    void getRecentCoverLetters_WithValidUserId_ShouldReturnRecentLetters() {
        // 模拟依赖
        when(coverLetterRepository.findTop10ByUserIdOrderByLastModifiedAtDesc("user-1"))
                .thenReturn(Arrays.asList(testCoverLetter));

        // 执行测试
        ResponseEntity<List<CoverLetter>> response = coverLetterController.getRecentCoverLetters("user-1");

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        // 验证调用
        verify(coverLetterRepository).findTop10ByUserIdOrderByLastModifiedAtDesc("user-1");
    }

    @Test
    void handleRuntimeException_ShouldReturnBadRequest() {
        // 模拟一个会抛出异常的方法调用
        when(coverLetterRepository.findById("invalid-id")).thenThrow(new RuntimeException("求职信不存在"));

        // 验证异常处理
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            coverLetterController.getCoverLetterById("invalid-id");
        });

        assertEquals("求职信不存在", exception.getMessage());
    }
}