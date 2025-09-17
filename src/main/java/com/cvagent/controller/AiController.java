package com.cvagent.controller;

import com.cvagent.model.User;
import com.cvagent.repository.UserRepository;
import com.cvagent.security.UserPrincipal;
import com.cvagent.service.AiMonitoringService;
import com.cvagent.service.AiServiceManager;
import com.cvagent.service.PromptTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * AI服务控制器
 * 提供各种AI功能的API接口
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI服务", description = "AI相关功能的服务接口")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    @Autowired
    private AiServiceManager aiServiceManager;

    @Autowired
    private PromptTemplateService promptTemplateService;

    @Autowired
    private AiMonitoringService aiMonitoringService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 基础聊天功能
     */
    @PostMapping("/chat")
    @Operation(summary = "AI聊天", description = "基础AI聊天功能")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "聊天成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "AI服务暂时不可用")
    })
    public ResponseEntity<Map<String, Object>> chat(
            @Parameter(description = "聊天请求参数", required = true)
            @RequestBody Map<String, String> request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 发起聊天请求", userPrincipal.getUsername());

        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "消息内容不能为空"));
        }

        try {
            String response = aiServiceManager.chat(message);
            return ResponseEntity.ok(Map.of(
                    "response", response,
                    "type", "chat"
            ));
        } catch (Exception e) {
            logger.error("聊天请求失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "聊天服务暂时不可用"));
        }
    }

    /**
     * 简历优化
     */
    @PostMapping("/optimize-resume")
    @Operation(summary = "简历优化", description = "使用AI优化简历内容")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "优化成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "AI服务暂时不可用")
    })
    public ResponseEntity<Map<String, Object>> optimizeResume(
            @Parameter(description = "简历优化请求参数", required = true)
            @RequestBody Map<String, String> request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 发起简历优化请求", userPrincipal.getUsername());

        String resumeContent = request.get("resumeContent");
        String jobDescription = request.get("jobDescription");

        if (resumeContent == null || resumeContent.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "简历内容不能为空"));
        }

        try {
            String response = aiServiceManager.optimizeResume(resumeContent, jobDescription);
            return ResponseEntity.ok(Map.of(
                    "optimizedResume", response,
                    "type", "resume_optimization"
            ));
        } catch (Exception e) {
            logger.error("简历优化请求失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "简历优化服务暂时不可用"));
        }
    }

    /**
     * 生成求职信
     */
    @PostMapping("/generate-cover-letter")
    @Operation(summary = "生成求职信", description = "使用AI生成求职信")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "生成成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "AI服务暂时不可用")
    })
    public ResponseEntity<Map<String, Object>> generateCoverLetter(
            @Parameter(description = "求职信生成请求参数", required = true)
            @RequestBody Map<String, String> request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 发起求职信生成请求", userPrincipal.getUsername());

        String resumeContent = request.get("resumeContent");
        String jobDescription = request.get("jobDescription");
        String companyInfo = request.get("companyInfo");

        if (resumeContent == null || resumeContent.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "简历内容不能为空"));
        }

        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "职位描述不能为空"));
        }

        try {
            String response = aiServiceManager.generateCoverLetter(resumeContent, jobDescription, companyInfo);
            return ResponseEntity.ok(Map.of(
                    "coverLetter", response,
                    "type", "cover_letter_generation"
            ));
        } catch (Exception e) {
            logger.error("求职信生成请求失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "求职信生成服务暂时不可用"));
        }
    }

    /**
     * 改进简历章节
     */
    @PostMapping("/improve-resume-section")
    @Operation(summary = "改进简历章节", description = "使用AI改进简历的特定章节")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "改进成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "AI服务暂时不可用")
    })
    public ResponseEntity<Map<String, Object>> improveResumeSection(
            @Parameter(description = "章节改进请求参数", required = true)
            @RequestBody Map<String, String> request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 发起简历章节改进请求", userPrincipal.getUsername());

        String sectionContent = request.get("sectionContent");
        String sectionType = request.get("sectionType");

        if (sectionContent == null || sectionContent.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "章节内容不能为空"));
        }

        if (sectionType == null || sectionType.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "章节类型不能为空"));
        }

        try {
            String response = aiServiceManager.improveResumeSection(sectionContent, sectionType);
            return ResponseEntity.ok(Map.of(
                    "improvedSection", response,
                    "sectionType", sectionType,
                    "type", "resume_section_improvement"
            ));
        } catch (Exception e) {
            logger.error("简历章节改进请求失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "简历章节改进服务暂时不可用"));
        }
    }

    /**
     * 生成项目描述
     */
    @PostMapping("/generate-project-description")
    @Operation(summary = "生成项目描述", description = "使用AI生成项目描述")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "生成成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "AI服务暂时不可用")
    })
    public ResponseEntity<Map<String, Object>> generateProjectDescription(
            @Parameter(description = "项目描述生成请求参数", required = true)
            @RequestBody Map<String, String> request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 发起项目描述生成请求", userPrincipal.getUsername());

        String projectInfo = request.get("projectInfo");

        if (projectInfo == null || projectInfo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "项目信息不能为空"));
        }

        try {
            String response = aiServiceManager.generateProjectDescription(projectInfo);
            return ResponseEntity.ok(Map.of(
                    "projectDescription", response,
                    "type", "project_description_generation"
            ));
        } catch (Exception e) {
            logger.error("项目描述生成请求失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "项目描述生成服务暂时不可用"));
        }
    }

    /**
     * 异步聊天
     */
    @PostMapping("/chat-async")
    @Operation(summary = "异步聊天", description = "异步AI聊天功能")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "聊天成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "AI服务暂时不可用")
    })
    public CompletableFuture<ResponseEntity<Map<String, Object>>> chatAsync(
            @Parameter(description = "聊天请求参数", required = true)
            @RequestBody Map<String, String> request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 发起异步聊天请求", userPrincipal.getUsername());

        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(Map.of("error", "消息内容不能为空"))
            );
        }

        return aiServiceManager.chatAsync(message)
                .thenApply(response -> {
                    Map<String, Object> successResponse = new HashMap<>();
                    successResponse.put("response", response);
                    successResponse.put("type", "chat_async");
                    return ResponseEntity.ok(successResponse);
                })
                .exceptionally(e -> {
                    logger.error("异步聊天请求失败: {}", e.getMessage(), e);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "聊天服务暂时不可用");
                    return ResponseEntity.internalServerError().body(errorResponse);
                });
    }

    /**
     * 批量处理
     */
    @PostMapping("/batch-process")
    @Operation(summary = "批量处理", description = "批量处理AI请求")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "处理成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "AI服务暂时不可用")
    })
    public ResponseEntity<Map<String, Object>> batchProcess(
            @Parameter(description = "批量处理请求参数", required = true)
            @RequestBody Map<String, Object> request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        logger.info("用户 {} 发起批量处理请求", userPrincipal.getUsername());

        @SuppressWarnings("unchecked")
        Map<String, Object> requests = (Map<String, Object>) request.get("requests");

        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "处理请求不能为空"));
        }

        try {
            Map<String, String> results = aiServiceManager.batchProcess(requests);
            return ResponseEntity.ok(Map.of(
                    "results", results,
                    "total", results.size(),
                    "type", "batch_process"
            ));
        } catch (Exception e) {
            logger.error("批量处理请求失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "批量处理服务暂时不可用"));
        }
    }

    /**
     * 获取AI服务状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取服务状态", description = "获取AI服务的状态信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = aiServiceManager.getServiceStatus();
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }

    /**
     * 获取服务统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计信息", description = "获取AI服务的统计信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = aiMonitoringService.getServiceStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "获取健康状态", description = "获取AI服务的健康状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = aiMonitoringService.getHealthStatus();
        return ResponseEntity.ok(health);
    }

    /**
     * 获取性能报告
     */
    @GetMapping("/performance-report")
    @Operation(summary = "获取性能报告", description = "获取AI服务的性能报告")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<Map<String, Object>> getPerformanceReport() {
        Map<String, Object> report = aiMonitoringService.getPerformanceReport();
        return ResponseEntity.ok(report);
    }

    /**
     * 获取最近的使用日志
     */
    @GetMapping("/recent-logs")
    @Operation(summary = "获取最近日志", description = "获取最近的AI使用日志")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<com.cvagent.model.AiUsageLog>> getRecentLogs(
            @Parameter(description = "日志数量限制", required = false, example = "10")
            @RequestParam(defaultValue = "10") int limit) {

        if (limit > 100) {
            limit = 100; // 限制最大数量
        }

        List<com.cvagent.model.AiUsageLog> logs = aiMonitoringService.getRecentLogs(limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取错误日志
     */
    @GetMapping("/error-logs")
    @Operation(summary = "获取错误日志", description = "获取AI服务的错误日志")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<com.cvagent.model.AiUsageLog>> getErrorLogs(
            @Parameter(description = "日志数量限制", required = false, example = "10")
            @RequestParam(defaultValue = "10") int limit) {

        if (limit > 100) {
            limit = 100; // 限制最大数量
        }

        List<com.cvagent.model.AiUsageLog> logs = aiMonitoringService.getErrorLogs(limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取服务排行榜
     */
    @GetMapping("/service-ranking")
    @Operation(summary = "获取服务排行榜", description = "获取AI服务的使用排行榜")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<Map<String, Object>>> getServiceRanking() {
        List<Map<String, Object>> ranking = aiMonitoringService.getServiceRanking();
        return ResponseEntity.ok(ranking);
    }

    /**
     * 获取所有提示词模板
     */
    @GetMapping("/prompt-templates")
    @Operation(summary = "获取提示词模板", description = "获取所有的提示词模板名称")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<String>> getPromptTemplates() {
        List<String> templates = promptTemplateService.getAllTemplateNames();
        return ResponseEntity.ok(templates);
    }

    /**
     * 获取提示词模板详情
     */
    @GetMapping("/prompt-templates/{templateName}")
    @Operation(summary = "获取模板详情", description = "获取指定提示词模板的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "模板不存在")
    })
    public ResponseEntity<Map<String, Object>> getPromptTemplateDetails(
            @Parameter(description = "模板名称", required = true, example = "resume_optimization")
            @PathVariable String templateName) {

        Map<String, Object> details = promptTemplateService.getTemplateDetails(templateName);
        if (details.get("content") == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(details);
    }

    /**
     * 使用提示词模板
     */
    @PostMapping("/prompt-templates/{templateName}/use")
    @Operation(summary = "使用模板", description = "使用指定的提示词模板")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "使用成功"),
        @ApiResponse(responseCode = "404", description = "模板不存在"),
        @ApiResponse(responseCode = "500", description = "处理模板失败")
    })
    public ResponseEntity<Map<String, Object>> usePromptTemplate(
            @Parameter(description = "模板名称", required = true, example = "resume_optimization")
            @PathVariable String templateName,
            @Parameter(description = "模板变量", required = true)
            @RequestBody Map<String, Object> variables) {

        try {
            String prompt = promptTemplateService.getPrompt(templateName, variables);
            return ResponseEntity.ok(Map.of(
                    "prompt", prompt,
                    "templateName", templateName
            ));
        } catch (Exception e) {
            logger.error("使用提示词模板失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "处理提示词模板失败"));
        }
    }

    /**
     * 重置统计数据
     */
    @PostMapping("/reset-statistics")
    @Operation(summary = "重置统计", description = "重置AI服务的统计数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "重置成功")
    })
    public ResponseEntity<Map<String, String>> resetStatistics() {
        aiMonitoringService.resetStatistics();
        return ResponseEntity.ok(Map.of("message", "统计数据已重置"));
    }

    /**
     * 清理旧日志
     */
    @PostMapping("/cleanup-logs")
    @Operation(summary = "清理日志", description = "清理指定天数前的旧日志")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "清理成功")
    })
    public ResponseEntity<Map<String, String>> cleanupLogs(
            @Parameter(description = "保留天数", required = false, example = "30")
            @RequestParam(defaultValue = "30") int daysToKeep) {

        aiMonitoringService.cleanupOldLogs(daysToKeep);
        return ResponseEntity.ok(Map.of("message", "清理了 " + daysToKeep + " 天前的日志"));
    }
}