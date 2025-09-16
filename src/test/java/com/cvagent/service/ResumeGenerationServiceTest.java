package com.cvagent.service;

import com.cvagent.model.EnhancedResume;
import com.cvagent.model.JobRequirement;
import com.cvagent.model.ResumeTemplate;
import com.cvagent.repository.EnhancedResumeRepository;
import com.cvagent.repository.JobRequirementRepository;
import com.cvagent.repository.ResumeTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 简历生成服务测试类
 */
@ExtendWith(MockitoExtension.class)
public class ResumeGenerationServiceTest {

    @Mock
    private EnhancedResumeRepository enhancedResumeRepository;

    @Mock
    private JobRequirementRepository jobRequirementRepository;

    @Mock
    private ResumeTemplateRepository resumeTemplateRepository;

    @Mock
    private ResumeExportService resumeExportService;

    @InjectMocks
    private ResumeGenerationService resumeGenerationService;

    private EnhancedResume testResume;
    private JobRequirement testJobRequirement;
    private ResumeTemplate testTemplate;
    private Map<String, Object> testResumeData;

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

        // 初始化技能
        EnhancedResume.Skills skills = new EnhancedResume.Skills();
        skills.setTechnicalSkills(List.of("Java", "Spring", "MySQL"));
        testResume.setSkills(skills);

        // 确保其他内部类也被初始化
        testResume.setWorkExperience(new EnhancedResume.WorkExperience());
        testResume.setEducation(new EnhancedResume.Education());
        testResume.setProjects(new EnhancedResume.Projects());
        testResume.setCertifications(new EnhancedResume.Certifications());
        testResume.setLanguages(new EnhancedResume.Languages());
        testResume.setInterests(new EnhancedResume.Interests());

        testJobRequirement = new JobRequirement();
        testJobRequirement.setId("test-job-id");
        testJobRequirement.setTitle("Java开发工程师");
        testJobRequirement.setCompany("测试公司");
        testJobRequirement.setDescription("负责Java后端开发工作");
        testJobRequirement.setSkills(List.of("Java", "Spring", "MySQL"));

        testTemplate = new ResumeTemplate();
        testTemplate.setId("test-template-id");
        testTemplate.setName("专业模板");
        testTemplate.setCategory("professional");
        testTemplate.setStyle("modern");

        testResumeData = Map.of(
                "title", "测试简历",
                "personalInfo", Map.of(
                        "name", "张三",
                        "email", "zhangsan@example.com",
                        "phone", "13800138000",
                        "location", "北京",
                        "summary", "有5年Java开发经验的工程师"
                ),
                "skills", Map.of(
                        "technicalSkills", List.of("Java", "Spring", "MySQL")
                )
        );
    }

    /**
     * 测试基础简历生成
     */
    @Test
    void testGenerateBasicResume() {
        // 模拟repository保存行为
        when(enhancedResumeRepository.save(any(EnhancedResume.class))).thenReturn(testResume);

        // 执行测试
        EnhancedResume result = resumeGenerationService.generateBasicResume("test-user-id", testResumeData);

        // 验证结果
        assertNotNull(result);
        assertEquals("test-user-id", result.getUserId());
        assertEquals("测试简历", result.getTitle());
        assertEquals("system", result.getGeneratedBy());
        assertEquals("1.0", result.getVersion());
        assertEquals("pending", result.getOptimizationStatus());

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).save(any(EnhancedResume.class));
    }

    /**
     * 测试生成优化简历
     */
    @Test
    void testGenerateOptimizedResume() {
        // 模拟repository行为
        when(enhancedResumeRepository.findById("base-resume-id")).thenReturn(Optional.of(testResume));
        when(jobRequirementRepository.findById("test-job-id")).thenReturn(Optional.of(testJobRequirement));
        when(enhancedResumeRepository.save(any(EnhancedResume.class))).thenReturn(testResume);

        // 执行测试
        EnhancedResume result = resumeGenerationService.generateOptimizedResume(
                "test-user-id", "base-resume-id", "test-job-id");

        // 验证结果
        assertNotNull(result);
        assertEquals("test-user-id", result.getUserId());
        assertEquals("base-resume-id", result.getBaseResumeId());
        assertEquals("test-job-id", result.getJobRequirementId());
        assertEquals("ai_optimized", result.getGeneratedBy());

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).findById("base-resume-id");
        verify(jobRequirementRepository, times(1)).findById("test-job-id");
        verify(enhancedResumeRepository, times(1)).save(any(EnhancedResume.class));
    }

    /**
     * 测试生成优化简历 - 基础简历不存在
     */
    @Test
    void testGenerateOptimizedResume_BaseResumeNotFound() {
        // 模拟repository行为
        when(enhancedResumeRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            resumeGenerationService.generateOptimizedResume(
                    "test-user-id", "non-existent-id", "test-job-id");
        });

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).findById("non-existent-id");
        verify(jobRequirementRepository, never()).findById(anyString());
    }

    /**
     * 测试生成优化简历 - 招聘需求不存在
     */
    @Test
    void testGenerateOptimizedResume_JobRequirementNotFound() {
        // 模拟repository行为
        when(enhancedResumeRepository.findById("base-resume-id")).thenReturn(Optional.of(testResume));
        when(jobRequirementRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            resumeGenerationService.generateOptimizedResume(
                    "test-user-id", "base-resume-id", "non-existent-id");
        });

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).findById("base-resume-id");
        verify(jobRequirementRepository, times(1)).findById("non-existent-id");
    }

    /**
     * 测试简历导出 - PDF格式
     */
    @Test
    void testExportResume_PDF() {
        // 模拟repository行为
        when(enhancedResumeRepository.findById("test-resume-id")).thenReturn(Optional.of(testResume));
        when(resumeTemplateRepository.findById("test-template-id")).thenReturn(Optional.of(testTemplate));
        when(resumeExportService.exportToPDF(testResume, testTemplate)).thenReturn(new byte[]{1, 2, 3});

        // 执行测试
        byte[] result = resumeGenerationService.generateResumeWithTemplate(
                "test-resume-id", "test-template-id", "pdf");

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.length);

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).findById("test-resume-id");
        verify(resumeTemplateRepository, times(1)).findById("test-template-id");
        verify(resumeTemplateRepository, times(1)).incrementUsageCount("test-template-id");
        verify(resumeExportService, times(1)).exportToPDF(testResume, testTemplate);
    }

    /**
     * 测试简历导出 - Word格式
     */
    @Test
    void testExportResume_Word() {
        // 模拟repository行为
        when(enhancedResumeRepository.findById("test-resume-id")).thenReturn(Optional.of(testResume));
        when(resumeTemplateRepository.findById("test-template-id")).thenReturn(Optional.of(testTemplate));
        when(resumeExportService.exportToWord(testResume, testTemplate)).thenReturn(new byte[]{1, 2, 3});

        // 执行测试
        byte[] result = resumeGenerationService.generateResumeWithTemplate(
                "test-resume-id", "test-template-id", "docx");

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.length);

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).findById("test-resume-id");
        verify(resumeTemplateRepository, times(1)).findById("test-template-id");
        verify(resumeTemplateRepository, times(1)).incrementUsageCount("test-template-id");
        verify(resumeExportService, times(1)).exportToWord(testResume, testTemplate);
    }

    /**
     * 测试简历导出 - HTML格式
     */
    @Test
    void testExportResume_HTML() {
        // 模拟repository行为
        when(enhancedResumeRepository.findById("test-resume-id")).thenReturn(Optional.of(testResume));
        when(resumeTemplateRepository.findById("test-template-id")).thenReturn(Optional.of(testTemplate));
        when(resumeExportService.exportToHTML(testResume, testTemplate)).thenReturn("<html>简历内容</html>".getBytes());

        // 执行测试
        byte[] result = resumeGenerationService.generateResumeWithTemplate(
                "test-resume-id", "test-template-id", "html");

        // 验证结果
        assertNotNull(result);
        assertEquals("<html>简历内容</html>", new String(result));

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).findById("test-resume-id");
        verify(resumeTemplateRepository, times(1)).findById("test-template-id");
        verify(resumeTemplateRepository, times(1)).incrementUsageCount("test-template-id");
        verify(resumeExportService, times(1)).exportToHTML(testResume, testTemplate);
    }

    /**
     * 测试简历导出 - 简历不存在
     */
    @Test
    void testExportResume_ResumeNotFound() {
        // 模拟repository行为
        when(enhancedResumeRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            resumeGenerationService.generateResumeWithTemplate(
                    "non-existent-id", "test-template-id", "pdf");
        });

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).findById("non-existent-id");
        verify(resumeTemplateRepository, never()).findById(anyString());
    }

    /**
     * 测试简历预览
     */
    @Test
    void testGetResumePreview() {
        // 模拟repository行为
        when(enhancedResumeRepository.findById("test-resume-id")).thenReturn(Optional.of(testResume));
        when(resumeTemplateRepository.findById("test-template-id")).thenReturn(Optional.of(testTemplate));
        when(resumeExportService.generatePreview(testResume, testTemplate)).thenReturn("<html>预览内容</html>");

        // 执行测试
        String result = resumeGenerationService.getResumePreview("test-resume-id", "test-template-id");

        // 验证结果
        assertNotNull(result);
        assertEquals("<html>预览内容</html>", result);

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).findById("test-resume-id");
        verify(resumeTemplateRepository, times(1)).findById("test-template-id");
        verify(resumeExportService, times(1)).generatePreview(testResume, testTemplate);
    }

    /**
     * 测试获取用户简历历史
     */
    @Test
    void testGetUserResumeHistory() {
        // 模拟repository行为
        List<EnhancedResume> expectedResumes = List.of(testResume);
        when(enhancedResumeRepository.findByUserIdOrderByCreatedAtDesc("test-user-id")).thenReturn(expectedResumes);

        // 执行测试
        List<EnhancedResume> result = resumeGenerationService.getUserResumeHistory("test-user-id");

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-resume-id", result.get(0).getId());

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).findByUserIdOrderByCreatedAtDesc("test-user-id");
    }

    /**
     * 测试简历版本对比
     */
    @Test
    void testCompareResumeVersions() {
        // 创建第二个简历
        EnhancedResume testResume2 = new EnhancedResume();
        testResume2.setId("test-resume-id-2");
        testResume2.setTitle("优化简历");
        testResume2.setVersion("1.1");
        testResume2.setMatchScore(0.85);

        // 模拟repository行为
        when(enhancedResumeRepository.findById("test-resume-id")).thenReturn(Optional.of(testResume));
        when(enhancedResumeRepository.findById("test-resume-id-2")).thenReturn(Optional.of(testResume2));

        // 执行测试
        Map<String, Object> result = resumeGenerationService.compareResumeVersions(
                "test-resume-id", "test-resume-id-2");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("resume1"));
        assertTrue(result.containsKey("resume2"));
        assertTrue(result.containsKey("comparison"));

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).findById("test-resume-id");
        verify(enhancedResumeRepository, times(1)).findById("test-resume-id-2");
    }

    /**
     * 测试删除简历
     */
    @Test
    void testDeleteResume() {
        // 模拟repository行为
        when(enhancedResumeRepository.findById("test-resume-id")).thenReturn(Optional.of(testResume));

        // 执行测试
        assertDoesNotThrow(() -> {
            resumeGenerationService.deleteResume("test-resume-id", "test-user-id");
        });

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).findById("test-resume-id");
        verify(enhancedResumeRepository, times(1)).delete(testResume);
    }

    /**
     * 测试删除简历 - 用户不匹配
     */
    @Test
    void testDeleteResume_UserMismatch() {
        // 设置简历属于不同用户
        testResume.setUserId("different-user-id");

        // 模拟repository行为
        when(enhancedResumeRepository.findById("test-resume-id")).thenReturn(Optional.of(testResume));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            resumeGenerationService.deleteResume("test-resume-id", "test-user-id");
        });

        // 验证repository调用
        verify(enhancedResumeRepository, times(1)).findById("test-resume-id");
        verify(enhancedResumeRepository, never()).delete(any(EnhancedResume.class));
    }
}