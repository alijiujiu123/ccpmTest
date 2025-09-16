package com.cvagent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提示词模板服务
 * 管理和提供各种AI功能的提示词模板
 */
@Service
public class PromptTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateService.class);

    @Autowired
    private AiMonitoringService aiMonitoringService;

    // 内置提示词模板
    private final Map<String, String> builtinTemplates = new ConcurrentHashMap<>();

    public PromptTemplateService() {
        // 初始化内置提示词模板
        initializeBuiltinTemplates();
    }

    /**
     * 初始化内置提示词模板
     */
    private void initializeBuiltinTemplates() {
        // 简历优化模板
        builtinTemplates.put("resume-optimization",
                """
                你是一个专业的简历优化专家。请根据以下简历内容和职位描述，对简历进行优化：

                【简历内容】：
                {resumeContent}

                【职位描述】：
                {jobDescription}

                请从以下几个方面进行优化：
                1. 关键词匹配：确保简历中包含职位描述中的关键词
                2. 经历优化：突出与目标职位最相关的经验和技能
                3. 成果量化：尽可能用数字和成果来展示能力
                4. 格式优化：确保简历格式清晰、专业
                5. 语言表达：使用更专业、更有力的表达方式

                请提供优化后的简历内容，并说明主要的改进点。
                """);

        // 求职信生成模板
        builtinTemplates.put("cover-letter-generation",
                """
                你是一个专业的求职信写作专家。请根据以下信息生成一封专业的求职信：

                【简历内容】：
                {resumeContent}

                【职位描述】：
                {jobDescription}

                【公司信息】：
                {companyInfo}

                请生成一封包含以下要素的求职信：
                1. 专业的称呼和开场白
                2. 对公司的了解和兴趣表达
                3. 个人技能和经验与职位的匹配
                4. 对公司价值的阐述
                5. 专业的结尾和联系方式

                求职信应该简洁、专业、有针对性，长度控制在300-500字。
                """);

        // 项目描述优化模板
        builtinTemplates.put("project-description-optimization",
                """
                你是一个专业的技术文档写作专家。请根据以下项目信息，生成一个专业的项目描述：

                【项目信息】：
                {projectInfo}

                请生成包含以下要素的项目描述：
                1. 项目背景和目标
                2. 技术栈和架构
                3. 主要功能和特性
                4. 个人职责和贡献
                5. 遇到的挑战和解决方案
                6. 项目成果和价值

                描述应该专业、清晰、有条理，突出技术亮点和个人贡献。
                """);

        // 自我介绍生成模板
        builtinTemplates.put("self-introduction",
                """
                你是一个专业的自我介绍写作专家。请根据以下简历内容和场合，生成一个专业的自我介绍：

                【简历内容】：
                {resumeContent}

                【场合】：
                {context}

                请生成一个包含以下要素的自我介绍：
                1. 姓名和基本背景
                2. 核心技能和专长
                3. 主要经验和成就
                4. 个人特点和优势
                5. 未来的目标和期望

                自我介绍应该简洁、自信、有重点，长度控制在200-300字。
                """);

        // 简历章节改进模板
        builtinTemplates.put("resume-section-improvement",
                """
                你是一个专业的简历优化专家。请改进以下简历章节：

                【章节类型】：{sectionType}
                【章节内容】：{sectionContent}

                请从以下几个方面进行改进：
                1. 内容优化：确保内容完整、准确
                2. 语言优化：使用更专业、更有力的表达
                3. 格式优化：确保格式清晰、易读
                4. 重点突出：突出最相关的信息
                5. 量化成果：尽可能用数字来展示成就

                请提供改进后的章节内容。
                """);

        // 技能匹配分析模板
        builtinTemplates.put("skill-matching-analysis",
                """
                你是一个专业的职业规划师。请分析以下简历和职位描述的技能匹配度：

                【简历内容】：
                {resumeContent}

                【职位描述】：
                {jobDescription}

                请提供以下分析：
                1. 匹配的技能清单
                2. 缺失的技能清单
                3. 技能匹配度百分比
                4. 技能提升建议
                5. 简历优化建议
                """);

        // 面试问题预测模板
        builtinTemplates.put("interview-questions-prediction",
                """
                你是一个专业的面试准备专家。根据以下简历和职位描述，预测可能的面试问题：

                【简历内容】：
                {resumeContent}

                【职位描述】：
                {jobDescription}

                请提供以下内容：
                1. 技术面试问题及答案
                2. 行为面试问题及答案
                3. 项目相关问题及答案
                4. 个人素质相关问题及答案
                5. 面试准备建议
                """);
    }

    /**
     * 获取提示词模板
     */
    @Cacheable(value = "promptTemplates", key = "#templateName")
    public String getPrompt(String templateName, Map<String, Object> variables) {
        long startTime = System.currentTimeMillis();
        try {
            String template = builtinTemplates.get(templateName);
            if (template == null) {
                logger.warn("未找到提示词模板: {}", templateName);
                return "未找到指定的提示词模板";
            }

            // 替换模板变量
            String prompt = template;
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                prompt = prompt.replace(placeholder, value);
            }

            long duration = System.currentTimeMillis() - startTime;
            aiMonitoringService.recordPromptRequest(templateName, duration, true);

            logger.info("获取提示词模板完成: {}, 耗时: {}ms", templateName, duration);
            return prompt;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            aiMonitoringService.recordPromptRequest(templateName, duration, false);

            logger.error("获取提示词模板失败: {}", templateName, e);
            throw new RuntimeException("获取提示词模板失败", e);
        }
    }

    /**
     * 添加自定义提示词模板
     */
    public void addCustomTemplate(String templateName, String templateContent) {
        builtinTemplates.put(templateName, templateContent);
        logger.info("添加自定义提示词模板: {}", templateName);
    }

    /**
     * 删除提示词模板
     */
    public void removeTemplate(String templateName) {
        builtinTemplates.remove(templateName);
        logger.info("删除提示词模板: {}", templateName);
    }

    /**
     * 获取所有模板名称
     */
    public java.util.List<String> getAllTemplateNames() {
        return new java.util.ArrayList<>(builtinTemplates.keySet());
    }

    /**
     * 获取模板详情
     */
    public Map<String, Object> getTemplateDetails(String templateName) {
        Map<String, Object> details = new HashMap<>();
        details.put("name", templateName);
        details.put("content", builtinTemplates.get(templateName));
        details.put("variables", extractVariables(builtinTemplates.get(templateName)));
        return details;
    }

    /**
     * 提取模板中的变量
     */
    private java.util.List<String> extractVariables(String template) {
        java.util.List<String> variables = new java.util.ArrayList<>();
        if (template != null) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
            java.util.regex.Matcher matcher = pattern.matcher(template);
            while (matcher.find()) {
                variables.add(matcher.group(1));
            }
        }
        return variables;
    }

    /**
     * 验证模板变量
     */
    public boolean validateTemplateVariables(String templateName, Map<String, Object> variables) {
        String template = builtinTemplates.get(templateName);
        if (template == null) {
            return false;
        }

        java.util.List<String> requiredVariables = extractVariables(template);
        return variables.keySet().containsAll(requiredVariables);
    }

    /**
     * 从文件加载模板
     */
    public void loadTemplateFromFile(String templateName, String filePath) {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            Path path = resource.getFile().toPath();
            String content = Files.readString(path);
            builtinTemplates.put(templateName, content);
            logger.info("从文件加载提示词模板: {}", templateName);
        } catch (IOException e) {
            logger.error("从文件加载提示词模板失败: {}", templateName, e);
            throw new RuntimeException("加载模板文件失败", e);
        }
    }

    /**
     * 批量处理模板
     */
    public Map<String, String> batchProcessTemplates(Map<String, Map<String, Object>> requests) {
        Map<String, String> results = new HashMap<>();

        requests.forEach((key, request) -> {
            try {
                String templateName = request.get("templateName").toString();
                Map<String, Object> variables = (Map<String, Object>) request.get("variables");
                results.put(key, getPrompt(templateName, variables));
            } catch (Exception e) {
                results.put(key, "处理失败: " + e.getMessage());
                logger.error("批量处理模板失败，key: {}", key, e);
            }
        });

        return results;
    }
}