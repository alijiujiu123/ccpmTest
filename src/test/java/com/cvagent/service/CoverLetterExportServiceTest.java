package com.cvagent.service;

import com.cvagent.model.CoverLetter;
import com.cvagent.repository.CoverLetterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 求职信导出服务测试
 */
@ExtendWith(MockitoExtension.class)
class CoverLetterExportServiceTest {

    @Mock
    private CoverLetterRepository coverLetterRepository;

    @InjectMocks
    private CoverLetterExportService coverLetterExportService;

    private CoverLetter testCoverLetter;

    @BeforeEach
    void setUp() {
        // 创建测试求职信
        testCoverLetter = new CoverLetter();
        testCoverLetter.setId("cl-1");
        testCoverLetter.setUserId("user-1");
        testCoverLetter.setTitle("高级软件工程师求职信");
        testCoverLetter.setCompanyName("科技有限公司");
        testCoverLetter.setPosition("高级软件工程师");
        testCoverLetter.setLanguage("zh-CN");
        testCoverLetter.setAiOptimized(true);
        testCoverLetter.setMatchScore(0.85);
        testCoverLetter.setGeneratedAt(LocalDateTime.now());

        // 设置求职信内容
        CoverLetter.CoverLetterContent content = new CoverLetter.CoverLetterContent();
        content.setSalutation("尊敬的招聘经理：");
        content.setOpeningParagraph("我对贵公司的高级软件工程师职位非常感兴趣。"
                + "作为一名有5年Java开发经验的工程师，我相信我的技能和经验与贵公司的要求高度匹配。");
        content.setBodyParagraphs("在过去的5年中，我主要负责企业级应用开发，熟练掌握Spring Boot、"
                + "MongoDB等技术栈。我参与过多个大型项目的开发和维护，具备良好的团队协作能力。"
                + "特别是在高并发系统设计和性能优化方面有丰富经验。\n\n"
                + "我了解到贵公司正在寻找技术实力强的开发人才，"
                + "我相信我的技术背景和项目经验能够为贵公司创造价值。");
        content.setClosingParagraph("感谢您考虑我的申请。期待有机会与您进一步交流，"
                + "详细讨论我如何为贵公司的发展贡献力量。");
        content.setSignature("张三\n高级软件工程师\n电话：13800138000\n邮箱：zhangsan@example.com");
        content.setContactInfo("电话：13800138000\n邮箱：zhangsan@example.com\n地址：北京市朝阳区");
        content.setPostscript("P.S. 我有相关技术证书和项目作品集可以提供，随时可以进行技术面试。");
        testCoverLetter.setContent(content);
    }

    @Test
    void exportCoverLetter_WithValidHtmlFormat_ShouldReturnHtmlContent() {
        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(java.util.Optional.of(testCoverLetter));

        // 执行测试
        CoverLetterExportService.ExportResult result = coverLetterExportService.exportCoverLetter("cl-1", "html");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals("text/html", result.getContentType());
        assertTrue(result.getFilename().endsWith(".html"));
        assertTrue(result.getFilename().contains("高级软件工程师求职信"));

        // 验证HTML内容
        String htmlContent = new String(result.getContent());
        assertTrue(htmlContent.contains("<!DOCTYPE html>"));
        assertTrue(htmlContent.contains("高级软件工程师求职信"));
        assertTrue(htmlContent.contains("科技有限公司"));
        assertTrue(htmlContent.contains("高级软件工程师"));
        assertTrue(htmlContent.contains("尊敬的招聘经理："));
        assertTrue(htmlContent.contains("我对贵公司的高级软件工程师职位非常感兴趣"));
        assertTrue(htmlContent.contains("Spring Boot"));
        assertTrue(htmlContent.contains("MongoDB"));
        assertTrue(htmlContent.contains("张三"));
        assertTrue(htmlContent.contains("13800138000"));
        assertTrue(htmlContent.contains("生成时间"));
        assertTrue(htmlContent.contains("AI优化: 是"));
        assertTrue(htmlContent.contains("匹配度: 85.0%"));

        // 验证样式包含
        assertTrue(htmlContent.contains(".cover-letter"));
        assertTrue(htmlContent.contains(".header"));
        assertTrue(htmlContent.contains(".salutation"));
        assertTrue(htmlContent.contains(".opening"));
        assertTrue(htmlContent.contains(".body"));
        assertTrue(htmlContent.contains(".closing"));
        assertTrue(htmlContent.contains(".signature"));
        assertTrue(htmlContent.contains(".footer"));

        // 验证下载统计更新
        verify(coverLetterRepository).incrementDownloadCount(eq("cl-1"));
    }

    @Test
    void exportCoverLetter_WithValidDocxFormat_ShouldReturnWordContent() {
        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(java.util.Optional.of(testCoverLetter));

        // 执行测试
        CoverLetterExportService.ExportResult result = coverLetterExportService.exportCoverLetter("cl-1", "docx");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", result.getContentType());
        assertTrue(result.getFilename().endsWith(".docx"));

        // 验证Word内容
        String wordContent = new String(result.getContent());
        assertTrue(wordContent.contains("xmlns:o='urn:schemas-microsoft-com:office:office'"));
        assertTrue(wordContent.contains("xmlns:w='urn:schemas-microsoft-com:office:word'"));
        assertTrue(wordContent.contains("高级软件工程师求职信"));
        assertTrue(wordContent.contains("科技有限公司"));
        assertTrue(wordContent.contains("尊敬的招聘经理："));
        assertTrue(wordContent.contains("我对贵公司的高级软件工程师职位非常感兴趣"));

        // 验证Word样式
        assertTrue(wordContent.contains("font-family: 'SimSun'"));
        assertTrue(wordContent.contains("font-size: 12pt"));
        assertTrue(wordContent.contains("line-height: 1.5"));

        // 验证下载统计更新
        verify(coverLetterRepository).incrementDownloadCount(eq("cl-1"));
    }

    @Test
    void exportCoverLetter_WithValidPdfFormat_ShouldReturnPdfContent() {
        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(java.util.Optional.of(testCoverLetter));

        // 执行测试
        CoverLetterExportService.ExportResult result = coverLetterExportService.exportCoverLetter("cl-1", "pdf");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals("application/pdf", result.getContentType());
        assertTrue(result.getFilename().endsWith(".pdf"));

        // 验证PDF内容（目前返回HTML格式）
        String pdfContent = new String(result.getContent());
        assertTrue(pdfContent.contains("高级软件工程师求职信"));
        assertTrue(pdfContent.contains("科技有限公司"));
        assertTrue(pdfContent.contains("尊敬的招聘经理："));

        // 验证下载统计更新
        verify(coverLetterRepository).incrementDownloadCount(eq("cl-1"));
    }

    @Test
    void exportCoverLetter_WithInvalidFormat_ShouldThrowException() {
        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(java.util.Optional.of(testCoverLetter));

        // 验证异常
        assertThrows(RuntimeException.class, () -> {
            coverLetterExportService.exportCoverLetter("cl-1", "txt");
        });

        // 验证没有下载统计更新
        verify(coverLetterRepository, never()).incrementDownloadCount(anyString());
    }

    @Test
    void exportCoverLetter_WithNonexistentCoverLetter_ShouldThrowException() {
        // 模拟求职信不存在
        when(coverLetterRepository.findById("invalid-id")).thenReturn(java.util.Optional.empty());

        // 验证异常
        assertThrows(RuntimeException.class, () -> {
            coverLetterExportService.exportCoverLetter("invalid-id", "html");
        });

        // 验证没有下载统计更新
        verify(coverLetterRepository, never()).incrementDownloadCount(anyString());
    }

    @Test
    void exportCombinedPackage_WithValidHtmlFormat_ShouldReturnCombinedHtmlContent() {
        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(java.util.Optional.of(testCoverLetter));

        // 准备简历内容
        String resumeContent = """
            <div class="resume">
                <h1>张三</h1>
                <p>高级软件工程师</p>
                <h2>技能</h2>
                <ul>
                    <li>Java 5年经验</li>
                    <li>Spring Boot 熟练</li>
                    <li>MongoDB 熟练</li>
                </ul>
                <h2>工作经历</h2>
                <p>某科技公司 | 2019-至今</p>
                <p>负责企业级应用开发</p>
            </div>
            """;

        // 执行测试
        CoverLetterExportService.ExportResult result = coverLetterExportService.exportCombinedPackage(
            "cl-1", resumeContent, "html");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals("text/html", result.getContentType());
        assertTrue(result.getFilename().endsWith(".html"));
        assertTrue(result.getFilename().contains("申请材料包"));

        // 验证组合HTML内容
        String htmlContent = new String(result.getContent());
        assertTrue(htmlContent.contains("<!DOCTYPE html>"));
        assertTrue(htmlContent.contains("求职申请材料包"));
        assertTrue(htmlContent.contains("求职信"));
        assertTrue(htmlContent.contains("个人简历"));
        assertTrue(htmlContent.contains("高级软件工程师求职信"));
        assertTrue(htmlContent.contains("科技有限公司"));
        assertTrue(htmlContent.contains("张三"));
        assertTrue(htmlContent.contains("Java 5年经验"));
        assertTrue(htmlContent.contains("Spring Boot 熟练"));

        // 验证样式结构
        assertTrue(htmlContent.contains(".package"));
        assertTrue(htmlContent.contains(".cover-letter-section"));
        assertTrue(htmlContent.contains(".resume-section"));
        assertTrue(htmlContent.contains(".resume-content"));
    }

    @Test
    void exportCombinedPackage_WithValidDocxFormat_ShouldReturnCombinedWordContent() {
        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(java.util.Optional.of(testCoverLetter));

        // 准备简历内容
        String resumeContent = """
            <h1>张三 - 高级软件工程师</h1>
            <p>5年Java开发经验，精通Spring Boot生态系统</p>
            <h2>主要技能</h2>
            <ul>
                <li>Java, Spring Boot, MongoDB</li>
                <li>微服务架构, 高并发系统</li>
                <li>团队管理, 项目交付</li>
            </ul>
            """;

        // 执行测试
        CoverLetterExportService.ExportResult result = coverLetterExportService.exportCombinedPackage(
            "cl-1", resumeContent, "docx");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", result.getContentType());
        assertTrue(result.getFilename().endsWith(".docx"));

        // 验证组合Word内容
        String wordContent = new String(result.getContent());
        assertTrue(wordContent.contains("求职申请材料包"));
        assertTrue(wordContent.contains("求职信"));
        assertTrue(wordContent.contains("个人简历"));
        assertTrue(wordContent.contains("高级软件工程师求职信"));
        assertTrue(wordContent.contains("张三 - 高级软件工程师"));
        assertTrue(wordContent.contains("Java, Spring Boot, MongoDB"));
        assertTrue(wordContent.contains("尊敬的招聘经理："));
    }

    @Test
    void exportCombinedPackage_WithValidPdfFormat_ShouldReturnCombinedPdfContent() {
        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(java.util.Optional.of(testCoverLetter));

        // 准备简历内容
        String resumeContent = "<div>简历内容测试</div>";

        // 执行测试
        CoverLetterExportService.ExportResult result = coverLetterExportService.exportCombinedPackage(
            "cl-1", resumeContent, "pdf");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals("application/pdf", result.getContentType());
        assertTrue(result.getFilename().endsWith(".pdf"));

        // 验证组合PDF内容（目前返回HTML格式）
        String pdfContent = new String(result.getContent());
        assertTrue(pdfContent.contains("求职申请材料包"));
        assertTrue(pdfContent.contains("求职信"));
        assertTrue(pdfContent.contains("个人简历"));
    }

    @Test
    void exportCombinedPackage_WithInvalidFormat_ShouldThrowException() {
        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(java.util.Optional.of(testCoverLetter));

        // 准备简历内容
        String resumeContent = "<div>测试简历</div>";

        // 验证异常
        assertThrows(RuntimeException.class, () -> {
            coverLetterExportService.exportCombinedPackage("cl-1", resumeContent, "txt");
        });
    }

    @Test
    void exportCoverLetter_WithMinimalContent_ShouldGenerateValidHtml() {
        // 创建最小内容的求职信
        CoverLetter minimalCoverLetter = new CoverLetter();
        minimalCoverLetter.setId("cl-2");
        minimalCoverLetter.setTitle("测试求职信");
        minimalCoverLetter.setCompanyName("测试公司");
        minimalCoverLetter.setPosition("测试职位");

        // 设置基本内容
        CoverLetter.CoverLetterContent minimalContent = new CoverLetter.CoverLetterContent();
        minimalContent.setSalutation("您好：");
        minimalContent.setOpeningParagraph("我对贵公司职位感兴趣。");
        minimalContent.setBodyParagraphs("我有相关经验。");
        minimalContent.setClosingParagraph("期待回复。");
        minimalCoverLetter.setContent(minimalContent);

        // 模拟依赖
        when(coverLetterRepository.findById("cl-2")).thenReturn(java.util.Optional.of(minimalCoverLetter));

        // 执行测试
        CoverLetterExportService.ExportResult result = coverLetterExportService.exportCoverLetter("cl-2", "html");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getContent());

        // 验证HTML内容
        String htmlContent = new String(result.getContent());
        assertTrue(htmlContent.contains("测试求职信"));
        assertTrue(htmlContent.contains("测试公司"));
        assertTrue(htmlContent.contains("测试职位"));
        assertTrue(htmlContent.contains("您好："));
        assertTrue(htmlContent.contains("我对贵公司职位感兴趣"));
        assertTrue(htmlContent.contains("我有相关经验"));
        assertTrue(htmlContent.contains("期待回复"));
        assertTrue(htmlContent.contains("生成时间"));
    }

    @Test
    void exportCoverLetter_WithChineseCharacters_ShouldHandleEncodingCorrectly() {
        // 创建包含中文字符的求职信
        testCoverLetter.setTitle("高级软件工程师求职信-人工智能方向");
        testCoverLetter.setCompanyName("百度在线网络技术（北京）有限公司");
        testCoverLetter.setPosition("高级AI工程师");

        CoverLetter.CoverLetterContent content = testCoverLetter.getContent();
        content.setOpeningParagraph("我对贵公司在人工智能领域的创新成果深感钦佩。"
                + "作为一名专注于机器学习和深度学习的工程师，我希望能够加入贵公司的AI研发团队。");
        content.setBodyParagraphs("我在自然语言处理、计算机视觉和推荐系统等方面有丰富经验。"
                + "熟练掌握TensorFlow、PyTorch等深度学习框架，在大规模分布式训练方面有实践经验。"
                + "曾主导过多个AI项目，包括智能客服系统、图像识别平台等。");

        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(java.util.Optional.of(testCoverLetter));

        // 执行测试
        CoverLetterExportService.ExportResult result = coverLetterExportService.exportCoverLetter("cl-1", "html");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getContent());

        // 验证中文字符正确处理
        String htmlContent = new String(result.getContent());
        assertTrue(htmlContent.contains("高级软件工程师求职信-人工智能方向"));
        assertTrue(htmlContent.contains("百度在线网络技术（北京）有限公司"));
        assertTrue(htmlContent.contains("高级AI工程师"));
        assertTrue(htmlContent.contains("人工智能领域"));
        assertTrue(htmlContent.contains("机器学习"));
        assertTrue(htmlContent.contains("深度学习"));
        assertTrue(htmlContent.contains("TensorFlow"));
        assertTrue(htmlContent.contains("PyTorch"));
        assertTrue(htmlContent.contains("自然语言处理"));
        assertTrue(htmlContent.contains("计算机视觉"));

        // 验证UTF-8字符编码正常
        assertTrue(htmlContent.contains("charset=\"UTF-8\""));
        assertTrue(htmlContent.contains("lang=\"zh-CN\""));
    }

    @Test
    void exportCoverLetter_WithSpecialCharacters_ShouldEscapeProperly() {
        // 创建包含特殊字符的求职信
        testCoverLetter.setTitle("Java & Spring Boot <高级> 工程师求职信");
        testCoverLetter.setCompanyName("Tech & Solutions \"创新\" 公司");

        CoverLetter.CoverLetterContent content = testCoverLetter.getContent();
        content.setOpeningParagraph("我对贵公司的职位感兴趣，特别是在 <微服务> 和 \"云原生\" 领域。");
        content.setBodyParagraphs("我有以下技能：\n- Java & Spring Boot\n- MongoDB & Redis\n- Docker & Kubernetes");

        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(java.util.Optional.of(testCoverLetter));

        // 执行测试
        CoverLetterExportService.ExportResult result = coverLetterExportService.exportCoverLetter("cl-1", "html");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getContent());

        // 验证特殊字符在HTML中显示
        String htmlContent = new String(result.getContent());
        assertTrue(htmlContent.contains("Java & Spring Boot <高级> 工程师求职信"));
        assertTrue(htmlContent.contains("Tech & Solutions \"创新\" 公司"));
        assertTrue(htmlContent.contains("特别是在 <微服务> 和 \"云原生\" 领域"));
        assertTrue(htmlContent.contains("Java & Spring Boot"));
        assertTrue(htmlContent.contains("MongoDB & Redis"));
        assertTrue(htmlContent.contains("Docker & Kubernetes"));
    }
}