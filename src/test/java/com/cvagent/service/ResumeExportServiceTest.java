package com.cvagent.service;

import com.cvagent.model.EnhancedResume;
import com.cvagent.model.ResumeTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简历导出服务测试类
 */
@ExtendWith(MockitoExtension.class)
public class ResumeExportServiceTest {

    @InjectMocks
    private ResumeExportService resumeExportService;

    private EnhancedResume testResume;
    private ResumeTemplate testTemplate;

    @BeforeEach
    void setUp() {
        // 设置测试简历数据
        testResume = new EnhancedResume();
        testResume.setId("test-resume-id");
        testResume.setTitle("测试简历");
        testResume.setName("张三");
        testResume.setEmail("zhangsan@example.com");
        testResume.setPhone("13800138000");
        testResume.setLocation("北京");
        testResume.setSummary("有5年Java开发经验的工程师，精通Spring框架和MySQL数据库。");
        testResume.setMatchScore(0.85);
        testResume.setOptimizationStatus("completed");

        // 设置个人信息
        EnhancedResume.PersonalInfo personalInfo = new EnhancedResume.PersonalInfo();
        personalInfo.setFullName("张三");
        personalInfo.setEmail("zhangsan@example.com");
        personalInfo.setPhone("13800138000");
        personalInfo.setLocation("北京");
        personalInfo.setAddress("北京市朝阳区");
        testResume.setPersonalInfo(personalInfo);

        // 设置技能
        EnhancedResume.Skills skills = new EnhancedResume.Skills();
        skills.setTechnicalSkills(List.of("Java", "Spring", "MySQL", "Redis", "Docker"));
        testResume.setSkills(skills);

        // 设置测试模板数据
        testTemplate = new ResumeTemplate();
        testTemplate.setId("test-template-id");
        testTemplate.setName("专业模板");
        testTemplate.setCategory("professional");
        testTemplate.setStyle("modern");

        // 设置模板样式
        ResumeTemplate.TemplateStyling styling = new ResumeTemplate.TemplateStyling();
        styling.setPrimaryColor("#3498db");
        styling.setBackgroundColor("#ffffff");
        styling.setTextColor("#333333");
        testTemplate.setStyling(styling);
    }

    /**
     * 测试生成HTML内容
     */
    @Test
    void testGenerateHTMLContent() {
        // 使用反射调用私有方法进行测试
        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "generateHTMLContent", EnhancedResume.class, ResumeTemplate.class);
            method.setAccessible(true);

            String htmlContent = (String) method.invoke(resumeExportService, testResume, testTemplate);

            // 验证HTML内容
            assertNotNull(htmlContent);
            assertTrue(htmlContent.contains("<!DOCTYPE html>"));
            assertTrue(htmlContent.contains("<html"));
            assertTrue(htmlContent.contains("测试简历"));
            assertTrue(htmlContent.contains("张三"));
            assertTrue(htmlContent.contains("zhangsan@example.com"));
            assertTrue(htmlContent.contains("13800138000"));
            assertTrue(htmlContent.contains("北京"));
            assertTrue(htmlContent.contains("Java"));
            assertTrue(htmlContent.contains("Spring"));
            assertTrue(htmlContent.contains("85.0%"));
            assertTrue(htmlContent.contains("优化完成"));
            assertTrue(htmlContent.contains("#3498db")); // 模板主色调

        } catch (Exception e) {
            fail("测试生成HTML内容失败: " + e.getMessage());
        }
    }

    /**
     * 测试生成CSS样式
     */
    @Test
    void testGenerateCSSStyles() {
        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "generateCSSStyles", ResumeTemplate.class);
            method.setAccessible(true);

            String css = (String) method.invoke(resumeExportService, testTemplate);

            // 验证CSS内容
            assertNotNull(css);
            assertTrue(css.contains("body {"));
            assertTrue(css.contains(".resume-container {"));
            assertTrue(css.contains(".resume-title {"));
            assertTrue(css.contains(".section {"));
            assertTrue(css.contains(".personal-info {"));
            assertTrue(css.contains(".skills-list {"));
            assertTrue(css.contains(".skill-tag {"));
            assertTrue(css.contains(".match-score {"));
            assertTrue(css.contains("#3498db")); // 模板主色调
            assertTrue(css.contains("#ffffff")); // 模板背景色
            assertTrue(css.contains("#333333")); // 模板文字颜色

        } catch (Exception e) {
            fail("测试生成CSS样式失败: " + e.getMessage());
        }
    }

    /**
     * 测试应用模板样式
     */
    @Test
    void testApplyTemplateStyles() {
        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "applyTemplateStyles", ResumeTemplate.TemplateStyling.class);
            method.setAccessible(true);

            String styles = (String) method.invoke(resumeExportService, testTemplate.getStyling());

            // 验证样式应用
            assertNotNull(styles);
            assertTrue(styles.contains("#3498db")); // 主色调
            assertTrue(styles.contains("#ffffff")); // 背景色
            assertTrue(styles.contains("#333333")); // 文字颜色

        } catch (Exception e) {
            fail("测试应用模板样式失败: " + e.getMessage());
        }
    }

    /**
     * 测试生成个人信息HTML
     */
    @Test
    void testGeneratePersonalInfoHTML() {
        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "generatePersonalInfoHTML", EnhancedResume.PersonalInfo.class);
            method.setAccessible(true);

            String html = (String) method.invoke(resumeExportService, testResume.getPersonalInfo());

            // 验证个人信息HTML
            assertNotNull(html);
            assertTrue(html.contains("personal-info"));
            assertTrue(html.contains("张三"));
            assertTrue(html.contains("zhangsan@example.com"));
            assertTrue(html.contains("13800138000"));
            assertTrue(html.contains("北京"));

        } catch (Exception e) {
            fail("测试生成个人信息HTML失败: " + e.getMessage());
        }
    }

    /**
     * 测试生成技能HTML
     */
    @Test
    void testGenerateSkillsHTML() {
        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "generateSkillsHTML", EnhancedResume.Skills.class);
            method.setAccessible(true);

            String html = (String) method.invoke(resumeExportService, testResume.getSkills());

            // 验证技能HTML
            assertNotNull(html);
            assertTrue(html.contains("skills-list"));
            assertTrue(html.contains("skill-tag"));
            assertTrue(html.contains("Java"));
            assertTrue(html.contains("Spring"));
            assertTrue(html.contains("MySQL"));
            assertTrue(html.contains("Redis"));
            assertTrue(html.contains("Docker"));

        } catch (Exception e) {
            fail("测试生成技能HTML失败: " + e.getMessage());
        }
    }

    /**
     * 测试获取优化状态文本
     */
    @Test
    void testGetOptimizationStatusText() {
        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "getOptimizationStatusText", String.class);
            method.setAccessible(true);

            // 测试各种状态
            assertEquals("待优化", method.invoke(resumeExportService, "pending"));
            assertEquals("优化中", method.invoke(resumeExportService, "processing"));
            assertEquals("优化完成", method.invoke(resumeExportService, "completed"));
            assertEquals("优化失败", method.invoke(resumeExportService, "failed"));
            assertEquals("未知状态", method.invoke(resumeExportService, "unknown"));

        } catch (Exception e) {
            fail("测试获取优化状态文本失败: " + e.getMessage());
        }
    }

    /**
     * 测试获取文件内容类型
     */
    @Test
    void testGetContentType() {
        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "getContentType", String.class);
            method.setAccessible(true);

            // 测试各种格式
            assertEquals("application/pdf", method.invoke(resumeExportService, "pdf"));
            assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                         method.invoke(resumeExportService, "docx"));
            assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                         method.invoke(resumeExportService, "doc"));
            assertEquals("text/html", method.invoke(resumeExportService, "html"));
            assertEquals("application/octet-stream", method.invoke(resumeExportService, "unknown"));

        } catch (Exception e) {
            fail("测试获取文件内容类型失败: " + e.getMessage());
        }
    }

    /**
     * 测试获取文件扩展名
     */
    @Test
    void testGetFileExtension() {
        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "getFileExtension", String.class);
            method.setAccessible(true);

            // 测试各种格式
            assertEquals(".pdf", method.invoke(resumeExportService, "pdf"));
            assertEquals(".docx", method.invoke(resumeExportService, "docx"));
            assertEquals(".doc", method.invoke(resumeExportService, "doc"));
            assertEquals(".html", method.invoke(resumeExportService, "html"));
            assertEquals("", method.invoke(resumeExportService, "unknown"));

        } catch (Exception e) {
            fail("测试获取文件扩展名失败: " + e.getMessage());
        }
    }

    /**
     * 测试生成工作经历HTML
     */
    @Test
    void testGenerateWorkExperienceHTML() {
        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "generateWorkExperienceHTML", EnhancedResume.WorkExperience.class);
            method.setAccessible(true);

            EnhancedResume.WorkExperience workExperience = new EnhancedResume.WorkExperience();
            String html = (String) method.invoke(resumeExportService, workExperience);

            // 验证工作经历HTML结构
            assertNotNull(html);
            assertTrue(html.contains("section"));
            assertTrue(html.contains("工作经历"));
            assertTrue(html.contains("work-experience-item"));

        } catch (Exception e) {
            fail("测试生成工作经历HTML失败: " + e.getMessage());
        }
    }

    /**
     * 测试生成教育背景HTML
     */
    @Test
    void testGenerateEducationHTML() {
        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "generateEducationHTML", EnhancedResume.Education.class);
            method.setAccessible(true);

            EnhancedResume.Education education = new EnhancedResume.Education();
            String html = (String) method.invoke(resumeExportService, education);

            // 验证教育背景HTML结构
            assertNotNull(html);
            assertTrue(html.contains("section"));
            assertTrue(html.contains("教育背景"));
            assertTrue(html.contains("education-item"));

        } catch (Exception e) {
            fail("测试生成教育背景HTML失败: " + e.getMessage());
        }
    }

    /**
     * 测试生成项目经验HTML
     */
    @Test
    void testGenerateProjectsHTML() {
        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "generateProjectsHTML", EnhancedResume.Projects.class);
            method.setAccessible(true);

            EnhancedResume.Projects projects = new EnhancedResume.Projects();
            String html = (String) method.invoke(resumeExportService, projects);

            // 验证项目经验HTML结构
            assertNotNull(html);
            assertTrue(html.contains("section"));
            assertTrue(html.contains("项目经验"));
            assertTrue(html.contains("project-item"));

        } catch (Exception e) {
            fail("测试生成项目经验HTML失败: " + e.getMessage());
        }
    }

    /**
     * 测试空数据处理
     */
    @Test
    void testHandleNullData() {
        // 测试空简历数据
        EnhancedResume emptyResume = new EnhancedResume();
        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "generateHTMLContent", EnhancedResume.class, ResumeTemplate.class);
            method.setAccessible(true);

            String htmlContent = (String) method.invoke(resumeExportService, emptyResume, testTemplate);

            // 验证空数据的HTML生成不会出错
            assertNotNull(htmlContent);
            assertTrue(htmlContent.contains("<!DOCTYPE html>"));
            assertTrue(htmlContent.contains("<html"));

        } catch (Exception e) {
            fail("测试空数据处理失败: " + e.getMessage());
        }
    }

    /**
     * 测试模板样式为空的情况
     */
    @Test
    void testHandleNullTemplateStyling() {
        // 设置空样式的模板
        testTemplate.setStyling(null);

        try {
            java.lang.reflect.Method method = ResumeExportService.class.getDeclaredMethod(
                    "generateCSSStyles", ResumeTemplate.class);
            method.setAccessible(true);

            String css = (String) method.invoke(resumeExportService, testTemplate);

            // 验证空样式不会导致错误
            assertNotNull(css);
            assertTrue(css.contains("body {"));

        } catch (Exception e) {
            fail("测试空模板样式失败: " + e.getMessage());
        }
    }
}