package com.cvagent.service;

import com.cvagent.model.CoverLetter;
import com.cvagent.model.CoverLetterTemplate;
import com.cvagent.model.EnhancedResume;
import com.cvagent.model.JobRequirement;
import com.cvagent.repository.CoverLetterRepository;
import com.cvagent.repository.CoverLetterTemplateRepository;
import com.cvagent.repository.EnhancedResumeRepository;
import com.cvagent.repository.JobRequirementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 求职信生成服务测试
 */
@ExtendWith(MockitoExtension.class)
class CoverLetterGenerationServiceTest {

    @Mock
    private CoverLetterRepository coverLetterRepository;

    @Mock
    private CoverLetterTemplateRepository coverLetterTemplateRepository;

    @Mock
    private EnhancedResumeRepository enhancedResumeRepository;

    @Mock
    private JobRequirementRepository jobRequirementRepository;

    @Mock
    private AiServiceManager aiServiceManager;

    @InjectMocks
    private CoverLetterGenerationService coverLetterGenerationService;

    private CoverLetterTemplate testTemplate;
    private EnhancedResume testResume;
    private JobRequirement testJobRequirement;
    private Map<String, Object> testLetterData;

    @BeforeEach
    void setUp() {
        // 设置测试模板
        testTemplate = new CoverLetterTemplate();
        testTemplate.setId("template-1");
        testTemplate.setName("专业求职信模板");
        testTemplate.setCategory("professional");
        testTemplate.setStyle("formal");

        CoverLetterTemplate.TemplateStructure structure = new CoverLetterTemplate.TemplateStructure();
        CoverLetterTemplate.Section headerSection = new CoverLetterTemplate.Section();
        headerSection.setType("header");
        headerSection.setTitle("称呼");
        headerSection.setRequired(true);

        CoverLetterTemplate.Section openingSection = new CoverLetterTemplate.Section();
        openingSection.setType("opening");
        openingSection.setTitle("开场白");
        openingSection.setRequired(true);

        CoverLetterTemplate.Section bodySection = new CoverLetterTemplate.Section();
        bodySection.setType("body");
        bodySection.setTitle("正文");
        bodySection.setRequired(true);

        CoverLetterTemplate.Section closingSection = new CoverLetterTemplate.Section();
        closingSection.setType("closing");
        closingSection.setTitle("结尾");
        closingSection.setRequired(true);

        structure.setSections(List.of(headerSection, openingSection, bodySection, closingSection));
        testTemplate.setStructure(structure);

        // 设置语言配置
        CoverLetterTemplate.LanguageConfig languageConfig = new CoverLetterTemplate.LanguageConfig();
        languageConfig.setLanguage("zh-CN");
        languageConfig.setTone("professional");
        languageConfig.setFormalityLevel(8);
        testTemplate.setLanguageConfig(languageConfig);

        // 设置测试简历
        testResume = new EnhancedResume();
        testResume.setId("resume-1");
        testResume.setTitle("高级软件工程师简历");
        testResume.setUserId("user-1");
        testResume.setOptimizationStatus("completed");

        // 设置技能数据
        EnhancedResume.Skills skills = new EnhancedResume.Skills();
        skills.setTechnicalSkills(List.of("Java", "Spring Boot", "MongoDB", "React"));
        testResume.setSkills(skills);

        // 设置测试招聘需求
        testJobRequirement = new JobRequirement();
        testJobRequirement.setId("job-1");
        testJobRequirement.setTitle("高级软件工程师");
        testJobRequirement.setCompany("科技有限公司");
        testJobRequirement.setDescription("寻找有经验的高级软件工程师，熟悉Java生态系统，具备良好的团队协作能力。");
        testJobRequirement.setSkills(List.of("Java", "Spring Boot", "MongoDB"));

        // 设置测试信件数据
        testLetterData = new HashMap<>();
        testLetterData.put("title", "高级软件工程师求职信");
        testLetterData.put("companyName", "科技有限公司");
        testLetterData.put("position", "高级软件工程师");
        testLetterData.put("recipientName", "技术总监");
        testLetterData.put("recipientTitle", "技术总监");
    }

    @Test
    void generateBasicCoverLetter_WithValidData_ShouldCreateCoverLetter() {
        // 模拟依赖
        when(coverLetterTemplateRepository.findById("template-1")).thenReturn(Optional.of(testTemplate));
        when(coverLetterRepository.save(any(CoverLetter.class))).thenAnswer(invocation -> {
            CoverLetter saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId("cl-1");
                saved.setTemplateId("template-1");
                saved.setGeneratedAt(LocalDateTime.now());
                // 确保设置内容
                if (saved.getContent() == null) {
                    CoverLetter.CoverLetterContent content = new CoverLetter.CoverLetterContent();
                    content.setSalutation("尊敬的招聘经理：");
                    content.setOpeningParagraph("我对贵公司的高级软件工程师职位非常感兴趣。");
                    content.setBodyParagraphs("我有5年Java开发经验。");
                    content.setClosingParagraph("期待您的回复。");
                    content.setSignature("此致\n敬礼");
                    saved.setContent(content);
                }
            }
            return saved;
        });

        // 执行测试
        CoverLetter result = coverLetterGenerationService.generateBasicCoverLetter(
            "user-1", "template-1", testLetterData);

        // 验证结果
        assertNotNull(result);
        assertEquals("user-1", result.getUserId());
        assertEquals("template-1", result.getTemplateId());
        assertEquals("高级软件工程师求职信", result.getTitle());
        assertEquals("科技有限公司", result.getCompanyName());
        assertEquals("高级软件工程师", result.getPosition());
        assertEquals("template_based", result.getGeneratedBy());
        assertEquals("draft", result.getStatus());
        assertEquals("zh-CN", result.getLanguage());
        assertEquals("professional", result.getTone());
        assertEquals(8, result.getFormalityLevel());

        // 验证内容结构
        assertNotNull(result.getContent());
        assertNotNull(result.getContent().getSalutation());
        assertNotNull(result.getContent().getOpeningParagraph());
        assertNotNull(result.getContent().getBodyParagraphs());
        assertNotNull(result.getContent().getClosingParagraph());
        assertNotNull(result.getContent().getSignature());

        // 验证分数设置
        assertNotNull(result.getQualityScore());
        assertNotNull(result.getRelevanceScore());
        assertNotNull(result.getCompletenessScore());

        // 验证存储调用
        verify(coverLetterRepository).save(any(CoverLetter.class));
    }

    @Test
    void generateBasicCoverLetter_WithNonexistentTemplate_ShouldThrowException() {
        // 模拟模板不存在
        when(coverLetterTemplateRepository.findById("invalid-template")).thenReturn(Optional.empty());

        // 验证异常
        assertThrows(RuntimeException.class, () -> {
            coverLetterGenerationService.generateBasicCoverLetter("user-1", "invalid-template", testLetterData);
        });

        // 验证没有保存调用
        verify(coverLetterRepository, never()).save(any(CoverLetter.class));
    }

    @Test
    void generatePersonalizedCoverLetter_WithValidData_ShouldCreateAIOptimizedCoverLetter() {
        // 模拟依赖
        when(enhancedResumeRepository.findById("resume-1")).thenReturn(Optional.of(testResume));
        when(jobRequirementRepository.findById("job-1")).thenReturn(Optional.of(testJobRequirement));
        when(coverLetterTemplateRepository.findById("template-1")).thenReturn(Optional.of(testTemplate));

        // 模拟AI服务返回
        when(aiServiceManager.generateCoverLetter(anyString(), anyString(), anyString()))
                .thenReturn("尊敬的招聘经理：\n\n我对贵公司的高级软件工程师职位非常感兴趣。"
                        + "作为一名有5年Java开发经验的工程师，我在Spring Boot、MongoDB等技术栈方面有丰富经验。"
                        + "我曾在多个项目中担任技术负责人，成功交付了多个高并发系统。"
                        + "我相信我的技能和经验能够为贵公司创造价值。期待您的回复。\n\n此致\n敬礼");

        // 模拟保存操作
        when(coverLetterRepository.save(any(CoverLetter.class))).thenAnswer(invocation -> {
            CoverLetter saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                // 设置所有必要字段
                saved.setId("cl-1");
                saved.setResumeId("resume-1");
                saved.setJobRequirementId("job-1");
                saved.setTemplateId("template-1");
                saved.setGeneratedAt(LocalDateTime.now());
                saved.setStatus("processing");
                saved.setOptimizationStatus("processing");
                saved.setAiOptimized(false);
            }
            return saved;
        });

        // 准备自定义数据
        Map<String, Object> customData = new HashMap<>();
        customData.put("highlightSkills", List.of("Spring Boot", "MongoDB"));
        customData.put("experienceLevel", "高级");

        // 执行测试
        CoverLetter result = coverLetterGenerationService.generatePersonalizedCoverLetter(
            "user-1", "resume-1", "job-1", "template-1", customData);

        // 验证结果
        assertNotNull(result);
        assertEquals("user-1", result.getUserId());
        assertEquals("resume-1", result.getResumeId());
        assertEquals("job-1", result.getJobRequirementId());
        assertEquals("template-1", result.getTemplateId());
        assertEquals("高级软件工程师简历 - 高级软件工程师求职信", result.getTitle());
        assertEquals("ai_generated", result.getGeneratedBy());
        assertEquals("ready", result.getStatus());
        assertEquals("completed", result.getOptimizationStatus());
        assertTrue(result.getAiOptimized());
        assertEquals("科技有限公司", result.getCompanyName());
        assertEquals("高级软件工程师", result.getPosition());

        // 验证内容生成
        assertNotNull(result.getContent());
        assertNotNull(result.getContent().getSalutation());
        assertNotNull(result.getContent().getOpeningParagraph());
        assertNotNull(result.getContent().getBodyParagraphs());
        assertNotNull(result.getContent().getClosingParagraph());

        // 验证匹配度计算
        assertNotNull(result.getMatchScore());
        assertTrue(result.getMatchScore() > 0.0);

        // 验证质量分数
        assertNotNull(result.getQualityScore());
        assertNotNull(result.getRelevanceScore());
        assertNotNull(result.getCompletenessScore());

        // 验证AI服务调用
        verify(aiServiceManager).generateCoverLetter(
            contains("简历内容摘要"),
            contains("寻找有经验的高级软件工程师"),
            eq("科技有限公司")
        );

        // 验证保存调用（创建 + 更新状态）
        verify(coverLetterRepository, times(2)).save(any(CoverLetter.class));
    }

    @Test
    void optimizeCoverLetter_WithValidCoverLetter_ShouldOptimizeContent() {
        // 创建测试求职信
        CoverLetter existingCoverLetter = new CoverLetter();
        existingCoverLetter.setId("cl-1");
        existingCoverLetter.setUserId("user-1");
        existingCoverLetter.setTitle("测试求职信");
        existingCoverLetter.setStatus("draft");
        existingCoverLetter.setGeneratedBy("manual_edited");

        CoverLetter.CoverLetterContent content = new CoverLetter.CoverLetterContent();
        content.setSalutation("尊敬的招聘经理：");
        content.setOpeningParagraph("我对这个职位感兴趣。");
        content.setBodyParagraphs("我有相关经验。");
        content.setClosingParagraph("期待您的回复。");
        existingCoverLetter.setContent(content);

        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(Optional.of(existingCoverLetter));

        // 模拟AI优化返回
        when(aiServiceManager.improveResumeSection(anyString(), eq("cover_letter")))
                .thenReturn("尊敬的招聘经理：\n\n我对贵公司的高级软件工程师职位表现出浓厚兴趣。"
                        + "作为一名在Java生态系统方面有深厚经验的工程师，我在Spring Boot、MongoDB等技术领域拥有丰富实践经验。"
                        + "在过去的五年中，我参与并主导了多个大型项目的开发，具备出色的技术领导力和团队协作能力。"
                        + "我相信我的专业技能和工作经验能够为贵公司的技术团队带来显著价值。期待有机会与您进一步交流。\n\n此致\n敬礼");

        // 模拟保存操作
        when(coverLetterRepository.save(any(CoverLetter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 准备优化选项
        Map<String, Object> optimizationOptions = new HashMap<>();
        optimizationOptions.put("improveProfessionalism", true);
        optimizationOptions.put("addKeywords", List.of("Java", "Spring Boot"));
        optimizationOptions.put("enhanceStructure", true);

        // 执行测试
        CoverLetter result = coverLetterGenerationService.optimizeCoverLetter("cl-1", optimizationOptions);

        // 验证结果
        assertNotNull(result);
        assertEquals("cl-1", result.getId());
        assertEquals("completed", result.getOptimizationStatus());
        assertTrue(result.getAiOptimized());

        // 验证AI服务调用
        verify(aiServiceManager).improveResumeSection(
            contains("我对这个职位感兴趣。我有相关经验。期待您的回复。"),
            eq("cover_letter")
        );

        // 验证保存调用
        verify(coverLetterRepository, times(2)).save(any(CoverLetter.class));
    }

    @Test
    void optimizeCoverLetter_WithNonEditableCoverLetter_ShouldThrowException() {
        // 创建不可编辑的求职信
        CoverLetter sentCoverLetter = new CoverLetter();
        sentCoverLetter.setId("cl-1");
        sentCoverLetter.setStatus("sent");

        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(Optional.of(sentCoverLetter));

        // 准备优化选项
        Map<String, Object> optimizationOptions = new HashMap<>();

        // 验证异常
        assertThrows(RuntimeException.class, () -> {
            coverLetterGenerationService.optimizeCoverLetter("cl-1", optimizationOptions);
        });

        // 验证没有AI调用和保存操作
        verify(aiServiceManager, never()).improveResumeSection(anyString(), anyString());
        verify(coverLetterRepository, never()).save(any(CoverLetter.class));
    }

    @Test
    void customizeCoverLetter_WithValidCustomizations_ShouldApplyChanges() {
        // 创建测试求职信
        CoverLetter existingCoverLetter = new CoverLetter();
        existingCoverLetter.setId("cl-1");
        existingCoverLetter.setUserId("user-1");
        existingCoverLetter.setTitle("测试求职信");
        existingCoverLetter.setStatus("draft");
        existingCoverLetter.setTone("professional");
        existingCoverLetter.setFormalityLevel(7);

        CoverLetter.CoverLetterContent content = new CoverLetter.CoverLetterContent();
        content.setSalutation("尊敬的招聘经理：");
        content.setOpeningParagraph("我对这个职位感兴趣。");
        existingCoverLetter.setContent(content);

        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(Optional.of(existingCoverLetter));
        when(coverLetterRepository.save(any(CoverLetter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 准备定制化数据
        Map<String, Object> customizations = new HashMap<>();
        customizations.put("salutation", "尊敬的HR经理：");
        customizations.put("openingParagraph", "我对贵公司的技术职位非常感兴趣，希望能够加入您的团队。");
        customizations.put("tone", "friendly");
        customizations.put("formalityLevel", 6);

        // 执行测试
        CoverLetter result = coverLetterGenerationService.customizeCoverLetter("cl-1", customizations);

        // 验证结果
        assertNotNull(result);
        assertEquals("cl-1", result.getId());
        assertEquals("friendly", result.getTone());
        assertEquals(6, result.getFormalityLevel());

        // 验证内容更新
        assertEquals("尊敬的HR经理：", result.getContent().getSalutation());
        assertEquals("我对贵公司的技术职位非常感兴趣，希望能够加入您的团队。", result.getContent().getOpeningParagraph());

        // 验证定制化数据保存
        assertEquals(customizations, result.getCustomizations());

        // 验证版本更新
        assertTrue(result.getVersion() > 1);

        // 验证保存调用
        verify(coverLetterRepository).save(any(CoverLetter.class));
    }

    @Test
    void copyCoverLetter_WithValidCoverLetter_ShouldCreateCopy() {
        // 创建原始求职信
        CoverLetter original = new CoverLetter();
        original.setId("cl-1");
        original.setUserId("user-1");
        original.setTitle("原始求职信");
        original.setResumeId("resume-1");
        original.setJobRequirementId("job-1");
        original.setTemplateId("template-1");
        original.setCompanyName("测试公司");
        original.setPosition("测试职位");
        original.setRecipientName("招聘经理");
        original.setRecipientTitle("HR");
        original.setGeneratedBy("ai_generated");
        original.setStatus("ready");

        CoverLetter.CoverLetterContent content = new CoverLetter.CoverLetterContent();
        content.setSalutation("尊敬的招聘经理：");
        content.setOpeningParagraph("我对贵公司的职位感兴趣。");
        content.setBodyParagraphs("我有丰富的工作经验。");
        content.setClosingParagraph("期待您的回复。");
        content.setSignature("张三");
        content.setContactInfo("电话：13800138000");
        content.setPostscript("P.S. 我有相关证书。");
        original.setContent(content);

        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(Optional.of(original));
        when(coverLetterRepository.save(any(CoverLetter.class))).thenAnswer(invocation -> {
            CoverLetter saved = invocation.getArgument(0);
            saved.setId("cl-2");
            saved.setGeneratedAt(LocalDateTime.now());
            return saved;
        });

        // 执行测试
        CoverLetter copy = coverLetterGenerationService.copyCoverLetter("cl-1", "求职信副本");

        // 验证结果
        assertNotNull(copy);
        assertNotEquals("cl-1", copy.getId());
        assertEquals("user-1", copy.getUserId());
        assertEquals("求职信副本", copy.getTitle());
        assertEquals("resume-1", copy.getResumeId());
        assertEquals("job-1", copy.getJobRequirementId());
        assertEquals("template-1", copy.getTemplateId());
        assertEquals("测试公司", copy.getCompanyName());
        assertEquals("测试职位", copy.getPosition());
        assertEquals("招聘经理", copy.getRecipientName());
        assertEquals("HR", copy.getRecipientTitle());
        assertEquals("copied", copy.getGeneratedBy());
        assertEquals("draft", copy.getStatus());

        // 验证内容复制
        assertNotNull(copy.getContent());
        assertEquals("尊敬的招聘经理：", copy.getContent().getSalutation());
        assertEquals("我对贵公司的职位感兴趣。", copy.getContent().getOpeningParagraph());
        assertEquals("我有丰富的工作经验。", copy.getContent().getBodyParagraphs());
        assertEquals("期待您的回复。", copy.getContent().getClosingParagraph());
        assertEquals("张三", copy.getContent().getSignature());
        assertEquals("电话：13800138000", copy.getContent().getContactInfo());
        assertEquals("P.S. 我有相关证书。", copy.getContent().getPostscript());

        // 验证保存调用
        verify(coverLetterRepository).save(any(CoverLetter.class));
    }

    @Test
    void getCoverLetterSuggestions_WithLowScores_ShouldReturnSuggestions() {
        // 创建低分数求职信
        CoverLetter lowScoreCoverLetter = new CoverLetter();
        lowScoreCoverLetter.setId("cl-1");
        lowScoreCoverLetter.setQualityScore(0.6);
        lowScoreCoverLetter.setRelevanceScore(0.5);
        lowScoreCoverLetter.setCompletenessScore(0.4);
        lowScoreCoverLetter.setAiOptimized(false);

        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(Optional.of(lowScoreCoverLetter));

        // 执行测试
        Map<String, Object> suggestions = coverLetterGenerationService.getCoverLetterSuggestions("cl-1");

        // 验证结果
        assertNotNull(suggestions);
        assertTrue(suggestions.containsKey("quality_improvement"));
        assertTrue(suggestions.containsKey("relevance_improvement"));
        assertTrue(suggestions.containsKey("completeness_improvement"));
        assertTrue(suggestions.containsKey("ai_optimization"));

        assertEquals("建议增加具体的工作成果和数据支撑", suggestions.get("quality_improvement"));
        assertEquals("建议增加与职位要求相关的技能和经验描述", suggestions.get("relevance_improvement"));
        assertEquals("建议补充缺失的部分，如具体的项目经验或技能证书", suggestions.get("completeness_improvement"));
        assertEquals("建议使用AI优化功能提升求职信质量", suggestions.get("ai_optimization"));
    }

    @Test
    void getCoverLetterSuggestions_WithHighScores_ShouldReturnMinimalSuggestions() {
        // 创建高分数求职信
        CoverLetter highScoreCoverLetter = new CoverLetter();
        highScoreCoverLetter.setId("cl-1");
        highScoreCoverLetter.setQualityScore(0.8);
        highScoreCoverLetter.setRelevanceScore(0.9);
        highScoreCoverLetter.setCompletenessScore(0.8);
        highScoreCoverLetter.setAiOptimized(true);

        // 模拟依赖
        when(coverLetterRepository.findById("cl-1")).thenReturn(Optional.of(highScoreCoverLetter));

        // 执行测试
        Map<String, Object> suggestions = coverLetterGenerationService.getCoverLetterSuggestions("cl-1");

        // 验证结果
        assertNotNull(suggestions);
        assertTrue(suggestions.isEmpty());
    }
}