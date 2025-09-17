package com.cvagent.controller;

import com.cvagent.model.CoverLetter;
import com.cvagent.model.CoverLetterTemplate;
import com.cvagent.service.CoverLetterExportService;
import com.cvagent.service.CoverLetterGenerationService;
import com.cvagent.repository.CoverLetterRepository;
import com.cvagent.repository.CoverLetterTemplateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 求职信控制器
 * 提供求职信生成、管理、优化和导出的REST API
 */
@RestController
@RequestMapping("/api/cover-letters")
@CrossOrigin(origins = "*")
@Tag(name = "求职信管理", description = "求职信的生成、管理、优化和导出相关接口")
@SecurityRequirement(name = "bearerAuth")
public class CoverLetterController {

    @Autowired
    private CoverLetterGenerationService coverLetterGenerationService;

    @Autowired
    private CoverLetterExportService coverLetterExportService;

    @Autowired
    private CoverLetterRepository coverLetterRepository;

    @Autowired
    private CoverLetterTemplateRepository coverLetterTemplateRepository;

    /**
     * 获取用户的求职信列表
     */
    @GetMapping
    @Operation(summary = "获取求职信列表", description = "获取用户的求职信列表，支持按状态和生成方式筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<List<CoverLetter>> getUserCoverLetters(
            @Parameter(description = "用户ID", required = true, example = "user123")
            @RequestParam String userId,
            @Parameter(description = "求职信状态", required = false, example = "draft")
            @RequestParam(required = false) String status,
            @Parameter(description = "生成方式", required = false, example = "ai")
            @RequestParam(required = false) String generatedBy) {

        List<CoverLetter> coverLetters;

        if (status != null) {
            coverLetters = coverLetterRepository.findByUserIdAndStatusOrderByGeneratedAtDesc(userId, status);
        } else if (generatedBy != null) {
            coverLetters = coverLetterRepository.findByUserIdAndGeneratedByOrderByGeneratedAtDesc(userId, generatedBy);
        } else {
            coverLetters = coverLetterRepository.findByUserIdOrderByGeneratedAtDesc(userId);
        }

        return ResponseEntity.ok(coverLetters);
    }

    /**
     * 根据ID获取求职信详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取求职信详情", description = "根据求职信ID获取详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "求职信不存在")
    })
    public ResponseEntity<CoverLetter> getCoverLetterById(
            @Parameter(description = "求职信ID", required = true, example = "letter123")
            @PathVariable String id) {
        CoverLetter coverLetter = coverLetterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("求职信不存在"));

        // 增加查看次数
        coverLetterRepository.incrementViewCount(id);

        return ResponseEntity.ok(coverLetter);
    }

    /**
     * 创建基于模板的基础求职信
     */
    @PostMapping("/basic")
    @Operation(summary = "创建基础求职信", description = "基于模板创建基础求职信")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<CoverLetter> createBasicCoverLetter(
            @Parameter(description = "求职信创建请求参数", required = true)
            @RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String templateId = (String) request.get("templateId");
        @SuppressWarnings("unchecked")
        Map<String, Object> letterData = (Map<String, Object>) request.get("letterData");

        CoverLetter coverLetter = coverLetterGenerationService.generateBasicCoverLetter(userId, templateId, letterData);
        return ResponseEntity.ok(coverLetter);
    }

    /**
     * 创建个性化求职信（基于简历和招聘需求）
     */
    @PostMapping("/personalized")
    @Operation(summary = "创建个性化求职信", description = "基于简历和招聘需求创建个性化求职信")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<CoverLetter> createPersonalizedCoverLetter(
            @Parameter(description = "个性化求职信创建请求参数", required = true)
            @RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String resumeId = (String) request.get("resumeId");
        String jobRequirementId = (String) request.get("jobRequirementId");
        String templateId = (String) request.get("templateId");
        @SuppressWarnings("unchecked")
        Map<String, Object> customData = (Map<String, Object>) request.get("customData");

        CoverLetter coverLetter = coverLetterGenerationService.generatePersonalizedCoverLetter(
            userId, resumeId, jobRequirementId, templateId, customData);

        return ResponseEntity.ok(coverLetter);
    }

    /**
     * 优化现有求职信
     */
    @PostMapping("/{id}/optimize")
    @Operation(summary = "优化求职信", description = "优化现有的求职信内容")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "优化成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "求职信不存在")
    })
    public ResponseEntity<CoverLetter> optimizeCoverLetter(
            @Parameter(description = "求职信ID", required = true, example = "letter123")
            @PathVariable String id,
            @Parameter(description = "优化选项", required = true)
            @RequestBody Map<String, Object> optimizationOptions) {

        CoverLetter optimizedCoverLetter = coverLetterGenerationService.optimizeCoverLetter(id, optimizationOptions);
        return ResponseEntity.ok(optimizedCoverLetter);
    }

    /**
     * 定制化求职信内容
     */
    @PutMapping("/{id}/customize")
    @Operation(summary = "定制化求职信", description = "定制化求职信的内容")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "定制化成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "求职信不存在")
    })
    public ResponseEntity<CoverLetter> customizeCoverLetter(
            @Parameter(description = "求职信ID", required = true, example = "letter123")
            @PathVariable String id,
            @Parameter(description = "定制化选项", required = true)
            @RequestBody Map<String, Object> customizations) {

        CoverLetter customizedCoverLetter = coverLetterGenerationService.customizeCoverLetter(id, customizations);
        return ResponseEntity.ok(customizedCoverLetter);
    }

    /**
     * 复制求职信
     */
    @PostMapping("/{id}/copy")
    @Operation(summary = "复制求职信", description = "复制现有的求职信")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "复制成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "求职信不存在")
    })
    public ResponseEntity<CoverLetter> copyCoverLetter(
            @Parameter(description = "求职信ID", required = true, example = "letter123")
            @PathVariable String id,
            @Parameter(description = "复制请求参数，包含新标题", required = true)
            @RequestBody Map<String, String> request) {

        String newTitle = request.get("newTitle");
        CoverLetter copiedCoverLetter = coverLetterGenerationService.copyCoverLetter(id, newTitle);
        return ResponseEntity.ok(copiedCoverLetter);
    }

    /**
     * 删除求职信
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除求职信", description = "删除指定的求职信")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "404", description = "求职信不存在")
    })
    public ResponseEntity<Void> deleteCoverLetter(
            @Parameter(description = "求职信ID", required = true, example = "letter123")
            @PathVariable String id) {
        if (!coverLetterRepository.existsById(id)) {
            throw new RuntimeException("求职信不存在");
        }

        coverLetterRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新求职信状态
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "更新求职信状态", description = "更新求职信的状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "求职信不存在")
    })
    public ResponseEntity<Void> updateCoverLetterStatus(
            @Parameter(description = "求职信ID", required = true, example = "letter123")
            @PathVariable String id,
            @Parameter(description = "状态更新请求参数", required = true)
            @RequestBody Map<String, String> request) {

        String status = request.get("status");
        coverLetterRepository.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取求职信建议
     */
    @GetMapping("/{id}/suggestions")
    @Operation(summary = "获取求职信建议", description = "获取求职信的优化建议")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "求职信不存在")
    })
    public ResponseEntity<Map<String, Object>> getCoverLetterSuggestions(
            @Parameter(description = "求职信ID", required = true, example = "letter123")
            @PathVariable String id) {
        Map<String, Object> suggestions = coverLetterGenerationService.getCoverLetterSuggestions(id);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * 搜索求职信
     */
    @GetMapping("/search")
    @Operation(summary = "搜索求职信", description = "根据关键词搜索求职信")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "搜索成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<List<CoverLetter>> searchCoverLetters(
            @Parameter(description = "用户ID", required = true, example = "user123")
            @RequestParam String userId,
            @Parameter(description = "搜索关键词", required = true, example = "Java")
            @RequestParam String keyword) {

        List<CoverLetter> results = coverLetterRepository.searchByUserAndKeyword(userId, keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 导出求职信
     */
    @GetMapping("/{id}/export")
    @Operation(summary = "导出求职信", description = "导出求职信为指定格式")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "导出成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "求职信不存在")
    })
    public ResponseEntity<byte[]> exportCoverLetter(
            @Parameter(description = "求职信ID", required = true, example = "letter123")
            @PathVariable String id,
            @Parameter(description = "导出格式", required = true, example = "pdf")
            @RequestParam String format) {

        CoverLetterExportService.ExportResult result = coverLetterExportService.exportCoverLetter(id, format);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.getContentType()));
        headers.setContentDispositionFormData("attachment", result.getFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .body(result.getContent());
    }

    /**
     * 导出求职信和简历组合包
     */
    @PostMapping("/{id}/export-package")
    @Operation(summary = "导出组合包", description = "导出求职信和简历的组合包")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "导出成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "求职信不存在")
    })
    public ResponseEntity<byte[]> exportCombinedPackage(
            @Parameter(description = "求职信ID", required = true, example = "letter123")
            @PathVariable String id,
            @Parameter(description = "导出请求参数", required = true)
            @RequestBody Map<String, Object> request) {

        String format = (String) request.get("format");
        String resumeContent = (String) request.get("resumeContent");

        CoverLetterExportService.ExportResult result = coverLetterExportService.exportCombinedPackage(id, resumeContent, format);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.getContentType()));
        headers.setContentDispositionFormData("attachment", result.getFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .body(result.getContent());
    }

    /**
     * 获取求职信模板列表
     */
    @GetMapping("/templates")
    @Operation(summary = "获取模板列表", description = "获取求职信模板列表，支持分类和关键词筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<CoverLetterTemplate>> getCoverLetterTemplates(
            @Parameter(description = "模板分类", required = false, example = "正式")
            @RequestParam(required = false) String category,
            @Parameter(description = "模板风格", required = false, example = "简约")
            @RequestParam(required = false) String style,
            @Parameter(description = "搜索关键词", required = false, example = "技术")
            @RequestParam(required = false) String keyword) {

        List<CoverLetterTemplate> templates;

        if (keyword != null) {
            templates = coverLetterTemplateRepository.searchByKeyword(keyword);
        } else if (category != null && style != null) {
            templates = coverLetterTemplateRepository.findByCategoryAndStyleAndIsActiveTrue(category, style);
        } else if (category != null) {
            templates = coverLetterTemplateRepository.findByCategory(category);
        } else if (style != null) {
            templates = coverLetterTemplateRepository.findByStyle(style);
        } else {
            templates = coverLetterTemplateRepository.findByIsActiveTrueOrderByUsageCountDesc();
        }

        return ResponseEntity.ok(templates);
    }

    /**
     * 获取求职信模板详情
     */
    @GetMapping("/templates/{id}")
    @Operation(summary = "获取模板详情", description = "获取求职信模板的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "模板不存在")
    })
    public ResponseEntity<CoverLetterTemplate> getCoverLetterTemplateById(
            @Parameter(description = "模板ID", required = true, example = "template123")
            @PathVariable String id) {
        CoverLetterTemplate template = coverLetterTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));
        return ResponseEntity.ok(template);
    }

    /**
     * 获取用户的求职信统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取统计信息", description = "获取用户的求职信统计信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<Map<String, Object>> getUserCoverLetterStats(
            @Parameter(description = "用户ID", required = true, example = "user123")
            @RequestParam String userId) {
        Map<String, Object> stats = Map.of(
            "total", coverLetterRepository.countByUserId(userId),
            "draft", coverLetterRepository.countByUserIdAndStatus(userId, "draft"),
            "ready", coverLetterRepository.countByUserIdAndStatus(userId, "ready"),
            "sent", coverLetterRepository.countByUserIdAndStatus(userId, "sent"),
            "aiOptimized", coverLetterRepository.findByUserIdAndAiOptimizedTrueOrderByGeneratedAtDesc(userId).size(),
            "highQuality", coverLetterRepository.findByUserIdAndQualityScoreGreaterThanOrderByQualityScoreDesc(userId, 0.8).size()
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * 获取高匹配度的求职信
     */
    @GetMapping("/high-match")
    @Operation(summary = "获取高匹配度求职信", description = "获取匹配度高于指定阈值的求职信")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<List<CoverLetter>> getHighMatchCoverLetters(
            @Parameter(description = "用户ID", required = true, example = "user123")
            @RequestParam String userId,
            @Parameter(description = "最小匹配度阈值", required = false, example = "0.7")
            @RequestParam(defaultValue = "0.7") double minScore) {

        List<CoverLetter> coverLetters = coverLetterRepository.findByUserIdAndMatchScoreGreaterThanOrderByMatchScoreDesc(userId, minScore);
        return ResponseEntity.ok(coverLetters);
    }

    /**
     * 获取需要优化的求职信
     */
    @GetMapping("/needs-optimization")
    @Operation(summary = "获取需优化的求职信", description = "获取需要AI优化的求职信列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<List<CoverLetter>> getCoverLettersNeedingOptimization(
            @Parameter(description = "用户ID", required = true, example = "user123")
            @RequestParam String userId) {
        List<CoverLetter> coverLetters = coverLetterRepository.findByUserIdAndAiOptimizedFalseOrderByGeneratedAtDesc(userId);
        return ResponseEntity.ok(coverLetters);
    }

    /**
     * 批量更新求职信状态
     */
    @PatchMapping("/batch-status")
    @Operation(summary = "批量更新状态", description = "批量更新多个求职信的状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<Void> batchUpdateCoverLetterStatus(
            @Parameter(description = "批量更新请求参数", required = true)
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> coverLetterIds = (List<String>) request.get("coverLetterIds");
        String status = (String) request.get("status");

        coverLetterRepository.batchUpdateStatus(coverLetterIds, status);
        return ResponseEntity.ok().build();
    }

    /**
     * 评分求职信
     */
    @PostMapping("/{id}/rate")
    @Operation(summary = "评分求职信", description = "为求职信评分")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "评分成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "求职信不存在")
    })
    public ResponseEntity<Void> rateCoverLetter(
            @Parameter(description = "求职信ID", required = true, example = "letter123")
            @PathVariable String id,
            @Parameter(description = "评分请求参数", required = true)
            @RequestBody Map<String, Object> request) {

        Double rating = (Double) request.get("rating");
        if (rating != null && rating >= 0 && rating <= 5) {
            coverLetterRepository.updateUserRating(id, rating);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 获取最近修改的求职信
     */
    @GetMapping("/recent")
    @Operation(summary = "获取最近修改的求职信", description = "获取最近修改的求职信列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<List<CoverLetter>> getRecentCoverLetters(
            @Parameter(description = "用户ID", required = true, example = "user123")
            @RequestParam String userId) {
        List<CoverLetter> coverLetters = coverLetterRepository.findTop10ByUserIdOrderByLastModifiedAtDesc(userId);
        return ResponseEntity.ok(coverLetters);
    }

    /**
     * 异常处理
     */
    @Operation(summary = "异常处理", description = "处理运行时异常")
    @ApiResponse(responseCode = "400", description = "请求处理失败")
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}