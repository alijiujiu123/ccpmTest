package com.cvagent.controller;

import com.cvagent.model.CoverLetter;
import com.cvagent.model.CoverLetterTemplate;
import com.cvagent.service.CoverLetterExportService;
import com.cvagent.service.CoverLetterGenerationService;
import com.cvagent.repository.CoverLetterRepository;
import com.cvagent.repository.CoverLetterTemplateRepository;
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
    public ResponseEntity<List<CoverLetter>> getUserCoverLetters(
            @RequestParam String userId,
            @RequestParam(required = false) String status,
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
    public ResponseEntity<CoverLetter> getCoverLetterById(@PathVariable String id) {
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
    public ResponseEntity<CoverLetter> createBasicCoverLetter(@RequestBody Map<String, Object> request) {
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
    public ResponseEntity<CoverLetter> createPersonalizedCoverLetter(@RequestBody Map<String, Object> request) {
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
    public ResponseEntity<CoverLetter> optimizeCoverLetter(
            @PathVariable String id,
            @RequestBody Map<String, Object> optimizationOptions) {

        CoverLetter optimizedCoverLetter = coverLetterGenerationService.optimizeCoverLetter(id, optimizationOptions);
        return ResponseEntity.ok(optimizedCoverLetter);
    }

    /**
     * 定制化求职信内容
     */
    @PutMapping("/{id}/customize")
    public ResponseEntity<CoverLetter> customizeCoverLetter(
            @PathVariable String id,
            @RequestBody Map<String, Object> customizations) {

        CoverLetter customizedCoverLetter = coverLetterGenerationService.customizeCoverLetter(id, customizations);
        return ResponseEntity.ok(customizedCoverLetter);
    }

    /**
     * 复制求职信
     */
    @PostMapping("/{id}/copy")
    public ResponseEntity<CoverLetter> copyCoverLetter(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {

        String newTitle = request.get("newTitle");
        CoverLetter copiedCoverLetter = coverLetterGenerationService.copyCoverLetter(id, newTitle);
        return ResponseEntity.ok(copiedCoverLetter);
    }

    /**
     * 删除求职信
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoverLetter(@PathVariable String id) {
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
    public ResponseEntity<Void> updateCoverLetterStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");
        coverLetterRepository.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取求职信建议
     */
    @GetMapping("/{id}/suggestions")
    public ResponseEntity<Map<String, Object>> getCoverLetterSuggestions(@PathVariable String id) {
        Map<String, Object> suggestions = coverLetterGenerationService.getCoverLetterSuggestions(id);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * 搜索求职信
     */
    @GetMapping("/search")
    public ResponseEntity<List<CoverLetter>> searchCoverLetters(
            @RequestParam String userId,
            @RequestParam String keyword) {

        List<CoverLetter> results = coverLetterRepository.searchByUserAndKeyword(userId, keyword);
        return ResponseEntity.ok(results);
    }

    /**
     * 导出求职信
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportCoverLetter(
            @PathVariable String id,
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
    public ResponseEntity<byte[]> exportCombinedPackage(
            @PathVariable String id,
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
    public ResponseEntity<List<CoverLetterTemplate>> getCoverLetterTemplates(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String style,
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
    public ResponseEntity<CoverLetterTemplate> getCoverLetterTemplateById(@PathVariable String id) {
        CoverLetterTemplate template = coverLetterTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));
        return ResponseEntity.ok(template);
    }

    /**
     * 获取用户的求职信统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserCoverLetterStats(@RequestParam String userId) {
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
    public ResponseEntity<List<CoverLetter>> getHighMatchCoverLetters(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0.7") double minScore) {

        List<CoverLetter> coverLetters = coverLetterRepository.findByUserIdAndMatchScoreGreaterThanOrderByMatchScoreDesc(userId, minScore);
        return ResponseEntity.ok(coverLetters);
    }

    /**
     * 获取需要优化的求职信
     */
    @GetMapping("/needs-optimization")
    public ResponseEntity<List<CoverLetter>> getCoverLettersNeedingOptimization(@RequestParam String userId) {
        List<CoverLetter> coverLetters = coverLetterRepository.findByUserIdAndAiOptimizedFalseOrderByGeneratedAtDesc(userId);
        return ResponseEntity.ok(coverLetters);
    }

    /**
     * 批量更新求职信状态
     */
    @PatchMapping("/batch-status")
    public ResponseEntity<Void> batchUpdateCoverLetterStatus(@RequestBody Map<String, Object> request) {
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
    public ResponseEntity<Void> rateCoverLetter(
            @PathVariable String id,
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
    public ResponseEntity<List<CoverLetter>> getRecentCoverLetters(@RequestParam String userId) {
        List<CoverLetter> coverLetters = coverLetterRepository.findTop10ByUserIdOrderByLastModifiedAtDesc(userId);
        return ResponseEntity.ok(coverLetters);
    }

    /**
     * 异常处理
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}