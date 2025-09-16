package com.cvagent.service;

import com.cvagent.service.ai.ChatAssistant;
import com.cvagent.service.ai.CreativeWritingAssistant;
import com.cvagent.service.ai.ResumeOptimizationAssistant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI服务管理器
 * 管理各种AI模型和服务
 */
@Service
public class AiServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(AiServiceManager.class);

    @Autowired
    private ChatAssistant chatAssistant;

    @Autowired
    private ResumeOptimizationAssistant resumeOptimizationAssistant;

    @Autowired
    private CreativeWritingAssistant creativeWritingAssistant;

    @Autowired
    private PromptTemplateService promptTemplateService;

    @Autowired
    private AiMonitoringService aiMonitoringService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 基础聊天功能
     */
    @Cacheable(value = "chatCache", key = "#message.hashCode()")
    public String chat(String message) {
        long startTime = System.currentTimeMillis();
        try {
            String response = chatAssistant.chat(message);
            long duration = System.currentTimeMillis() - startTime;

            // 记录监控数据
            aiMonitoringService.recordRequest("chat", duration, true, null);

            logger.info("聊天请求完成，耗时: {}ms", duration);
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            aiMonitoringService.recordRequest("chat", duration, false, e.getMessage());

            logger.error("聊天请求失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI聊天服务暂时不可用", e);
        }
    }

    /**
     * 简历优化
     */
    public String optimizeResume(String resumeContent, String jobDescription) {
        long startTime = System.currentTimeMillis();
        try {
            // 获取简历优化提示词
            String prompt = promptTemplateService.getPrompt("resume-optimization",
                    Map.of("resumeContent", resumeContent, "jobDescription", jobDescription));

            String response = resumeOptimizationAssistant.optimizeResume(resumeContent, jobDescription);
            long duration = System.currentTimeMillis() - startTime;

            aiMonitoringService.recordRequest("resume_optimization", duration, true, null);

            logger.info("简历优化完成，耗时: {}ms", duration);
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            aiMonitoringService.recordRequest("resume_optimization", duration, false, e.getMessage());

            logger.error("简历优化失败: {}", e.getMessage(), e);
            throw new RuntimeException("简历优化服务暂时不可用", e);
        }
    }

    /**
     * 生成求职信
     */
    public String generateCoverLetter(String resumeContent, String jobDescription, String companyInfo) {
        long startTime = System.currentTimeMillis();
        try {
            String response = creativeWritingAssistant.generateCoverLetter(resumeContent, jobDescription, companyInfo);
            long duration = System.currentTimeMillis() - startTime;

            aiMonitoringService.recordRequest("cover_letter_generation", duration, true, null);

            logger.info("求职信生成完成，耗时: {}ms", duration);
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            aiMonitoringService.recordRequest("cover_letter_generation", duration, false, e.getMessage());

            logger.error("求职信生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("求职信生成服务暂时不可用", e);
        }
    }

    /**
     * 改进简历章节
     */
    public String improveResumeSection(String sectionContent, String sectionType) {
        long startTime = System.currentTimeMillis();
        try {
            String response = resumeOptimizationAssistant.improveResumeSection(sectionContent, sectionType);
            long duration = System.currentTimeMillis() - startTime;

            aiMonitoringService.recordRequest("resume_section_improvement", duration, true, null);

            logger.info("简历章节改进完成，耗时: {}ms", duration);
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            aiMonitoringService.recordRequest("resume_section_improvement", duration, false, e.getMessage());

            logger.error("简历章节改进失败: {}", e.getMessage(), e);
            throw new RuntimeException("简历章节改进服务暂时不可用", e);
        }
    }

    /**
     * 生成项目描述
     */
    public String generateProjectDescription(String projectInfo) {
        long startTime = System.currentTimeMillis();
        try {
            String response = creativeWritingAssistant.generateProjectDescription(projectInfo);
            long duration = System.currentTimeMillis() - startTime;

            aiMonitoringService.recordRequest("project_description_generation", duration, true, null);

            logger.info("项目描述生成完成，耗时: {}ms", duration);
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            aiMonitoringService.recordRequest("project_description_generation", duration, false, e.getMessage());

            logger.error("项目描述生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("项目描述生成服务暂时不可用", e);
        }
    }

    /**
     * 异步聊天
     */
    public CompletableFuture<String> chatAsync(String message) {
        return CompletableFuture.supplyAsync(() -> chat(message), executorService);
    }

    /**
     * 异步简历优化
     */
    public CompletableFuture<String> optimizeResumeAsync(String resumeContent, String jobDescription) {
        return CompletableFuture.supplyAsync(() -> optimizeResume(resumeContent, jobDescription), executorService);
    }

    /**
     * 异步求职信生成
     */
    public CompletableFuture<String> generateCoverLetterAsync(String resumeContent, String jobDescription, String companyInfo) {
        return CompletableFuture.supplyAsync(() -> generateCoverLetter(resumeContent, jobDescription, companyInfo), executorService);
    }

    /**
     * 批量处理
     */
    public Map<String, String> batchProcess(Map<String, Object> requests) {
        Map<String, String> results = new HashMap<>();

        requests.forEach((key, value) -> {
            try {
                if (value instanceof Map) {
                    Map<String, String> params = (Map<String, String>) value;
                    String type = params.get("type");

                    switch (type) {
                        case "chat":
                            results.put(key, chat(params.get("message")));
                            break;
                        case "optimize_resume":
                            results.put(key, optimizeResume(params.get("resumeContent"), params.get("jobDescription")));
                            break;
                        case "generate_cover_letter":
                            results.put(key, generateCoverLetter(
                                params.get("resumeContent"),
                                params.get("jobDescription"),
                                params.get("companyInfo")
                            ));
                            break;
                        default:
                            results.put(key, "不支持的处理类型: " + type);
                    }
                } else {
                    results.put(key, "无效的请求格式");
                }
            } catch (Exception e) {
                results.put(key, "处理失败: " + e.getMessage());
                logger.error("批量处理失败，key: {}", key, e);
            }
        });

        return results;
    }

    /**
     * 获取服务状态
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("chatAvailable", true);
        status.put("resumeOptimizationAvailable", true);
        status.put("creativeWritingAvailable", true);
        status.put("codeReviewAvailable", true);
        status.put("activeThreads", ((java.util.concurrent.ThreadPoolExecutor) executorService).getActiveCount());
        status.put("queueSize", ((java.util.concurrent.ThreadPoolExecutor) executorService).getQueue().size());

        return status;
    }
}