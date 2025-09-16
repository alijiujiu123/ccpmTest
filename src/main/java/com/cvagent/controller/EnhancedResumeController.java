package com.cvagent.controller;

import com.cvagent.model.EnhancedResume;
import com.cvagent.model.JobRequirement;
import com.cvagent.model.ResumeTemplate;
import com.cvagent.service.ResumeGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 增强简历管理控制器
 * 提供简历生成、导出、预览等功能的API接口
 */
@RestController
@RequestMapping("/api/enhanced-resumes")
@CrossOrigin(origins = "*")
public class EnhancedResumeController {

    @Autowired
    private ResumeGenerationService resumeGenerationService;

    /**
     * 创建基础简历
     */
    @PostMapping("/basic")
    public ResponseEntity<EnhancedResume> createBasicResume(
            @RequestBody Map<String, Object> resumeData,
            @RequestHeader("X-User-Id") String userId) {
        try {
            EnhancedResume resume = resumeGenerationService.generateBasicResume(userId, resumeData);
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据招聘需求生成优化简历
     */
    @PostMapping("/optimized")
    public ResponseEntity<EnhancedResume> createOptimizedResume(
            @RequestBody Map<String, String> request,
            @RequestHeader("X-User-Id") String userId) {
        try {
            String baseResumeId = request.get("baseResumeId");
            String jobRequirementId = request.get("jobRequirementId");

            EnhancedResume resume = resumeGenerationService.generateOptimizedResume(userId, baseResumeId, jobRequirementId);
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 导出简历
     */
    @GetMapping("/{resumeId}/export")
    public ResponseEntity<byte[]> exportResume(
            @PathVariable String resumeId,
            @RequestParam String templateId,
            @RequestParam String format,
            @RequestParam(required = false) String filename) {
        try {
            byte[] content = resumeGenerationService.generateResumeWithTemplate(resumeId, templateId, format);

            String contentType = getContentType(format);
            String defaultFilename = "resume_" + System.currentTimeMillis() + getFileExtension(format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", filename != null ? filename : defaultFilename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取简历预览
     */
    @GetMapping("/{resumeId}/preview")
    public ResponseEntity<String> getResumePreview(
            @PathVariable String resumeId,
            @RequestParam String templateId) {
        try {
            String preview = resumeGenerationService.getResumePreview(resumeId, templateId);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(preview);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户的简历历史
     */
    @GetMapping("/history")
    public ResponseEntity<List<EnhancedResume>> getUserResumeHistory(
            @RequestHeader("X-User-Id") String userId) {
        try {
            List<EnhancedResume> resumes = resumeGenerationService.getUserResumeHistory(userId);
            return ResponseEntity.ok(resumes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取简历详情
     */
    @GetMapping("/{resumeId}")
    public ResponseEntity<EnhancedResume> getResumeDetails(@PathVariable String resumeId) {
        // 这里应该添加从数据库获取简历详情的逻辑
        // 暂时返回一个示例响应
        return ResponseEntity.ok().build();
    }

    /**
     * 简历版本对比
     */
    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareResumes(
            @RequestBody Map<String, String> request) {
        try {
            String resumeId1 = request.get("resumeId1");
            String resumeId2 = request.get("resumeId2");

            Map<String, Object> comparison = resumeGenerationService.compareResumeVersions(resumeId1, resumeId2);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除简历
     */
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable String resumeId,
            @RequestHeader("X-User-Id") String userId) {
        try {
            resumeGenerationService.deleteResume(resumeId, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取可用模板列表
     */
    @GetMapping("/templates")
    public ResponseEntity<List<ResumeTemplate>> getAvailableTemplates() {
        // 这里应该添加从数据库获取模板列表的逻辑
        // 暂时返回一个示例响应
        return ResponseEntity.ok().build();
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/templates/{templateId}")
    public ResponseEntity<ResumeTemplate> getTemplateDetails(@PathVariable String templateId) {
        // 这里应该添加从数据库获取模板详情的逻辑
        // 暂时返回一个示例响应
        return ResponseEntity.ok().build();
    }

    /**
     * 获取支持导出的格式列表
     */
    @GetMapping("/export-formats")
    public ResponseEntity<List<Map<String, String>>> getExportFormats() {
        List<Map<String, String>> formats = List.of(
                Map.of("format", "pdf", "name", "PDF文档", "mimeType", "application/pdf"),
                Map.of("format", "docx", "name", "Word文档", "mimeType", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
                Map.of("format", "html", "name", "HTML网页", "mimeType", "text/html")
        );
        return ResponseEntity.ok(formats);
    }

    /**
     * 获取文件内容类型
     */
    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "doc":
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "html":
                return "text/html";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return ".pdf";
            case "doc":
                return ".doc";
            case "docx":
                return ".docx";
            case "html":
                return ".html";
            default:
                return "";
        }
    }
}