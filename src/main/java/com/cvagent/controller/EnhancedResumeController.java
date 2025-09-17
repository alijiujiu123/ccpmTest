package com.cvagent.controller;

import com.cvagent.model.EnhancedResume;
import com.cvagent.model.JobRequirement;
import com.cvagent.model.ResumeTemplate;
import com.cvagent.service.ResumeGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "增强简历管理", description = "增强简历的生成、导出、预览等功能接口")
public class EnhancedResumeController {

    @Autowired
    private ResumeGenerationService resumeGenerationService;

    /**
     * 创建基础简历
     */
    @PostMapping("/basic")
    @Operation(summary = "创建基础简历", description = "根据用户提供的数据创建基础简历")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<EnhancedResume> createBasicResume(
            @Parameter(description = "简历数据", required = true)
            @RequestBody Map<String, Object> resumeData,
            @Parameter(description = "用户ID", required = true, example = "user123")
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
    @Operation(summary = "生成优化简历", description = "根据基础简历和招聘需求生成优化简历")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "生成成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<EnhancedResume> createOptimizedResume(
            @Parameter(description = "请求参数，包含基础简历ID和招聘需求ID", required = true)
            @RequestBody Map<String, String> request,
            @Parameter(description = "用户ID", required = true, example = "user123")
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
    @Operation(summary = "导出简历", description = "将简历导出为指定格式文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "导出成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "简历不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<byte[]> exportResume(
            @Parameter(description = "简历ID", required = true, example = "resume123")
            @PathVariable String resumeId,
            @Parameter(description = "模板ID", required = true, example = "template1")
            @RequestParam String templateId,
            @Parameter(description = "导出格式", required = true, example = "pdf")
            @RequestParam String format,
            @Parameter(description = "自定义文件名", required = false)
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
    @Operation(summary = "获取简历预览", description = "生成简历的HTML预览")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "预览生成成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<String> getResumePreview(
            @Parameter(description = "简历ID", required = true, example = "resume123")
            @PathVariable String resumeId,
            @Parameter(description = "模板ID", required = true, example = "template1")
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
    @Operation(summary = "获取简历历史", description = "获取用户的简历生成历史记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<List<EnhancedResume>> getUserResumeHistory(
            @Parameter(description = "用户ID", required = true, example = "user123")
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
    @Operation(summary = "获取简历详情", description = "获取指定简历的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "简历不存在")
    })
    public ResponseEntity<EnhancedResume> getResumeDetails(
            @Parameter(description = "简历ID", required = true, example = "resume123")
            @PathVariable String resumeId) {
        // 这里应该添加从数据库获取简历详情的逻辑
        // 暂时返回一个示例响应
        return ResponseEntity.ok().build();
    }

    /**
     * 简历版本对比
     */
    @PostMapping("/compare")
    @Operation(summary = "简历版本对比", description = "对比两个不同版本的简历差异")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "对比成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Map<String, Object>> compareResumes(
            @Parameter(description = "对比请求参数，包含两个简历ID", required = true)
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
    @Operation(summary = "删除简历", description = "删除指定的增强简历")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "删除成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<Void> deleteResume(
            @Parameter(description = "简历ID", required = true, example = "resume123")
            @PathVariable String resumeId,
            @Parameter(description = "用户ID", required = true, example = "user123")
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
    @Operation(summary = "获取模板列表", description = "获取所有可用的简历模板")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<ResumeTemplate>> getAvailableTemplates() {
        // 这里应该添加从数据库获取模板列表的逻辑
        // 暂时返回一个示例响应
        return ResponseEntity.ok().build();
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/templates/{templateId}")
    @Operation(summary = "获取模板详情", description = "获取指定模板的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "模板不存在")
    })
    public ResponseEntity<ResumeTemplate> getTemplateDetails(
            @Parameter(description = "模板ID", required = true, example = "template1")
            @PathVariable String templateId) {
        // 这里应该添加从数据库获取模板详情的逻辑
        // 暂时返回一个示例响应
        return ResponseEntity.ok().build();
    }

    /**
     * 获取支持导出的格式列表
     */
    @GetMapping("/export-formats")
    @Operation(summary = "获取导出格式", description = "获取所有支持的简历导出格式")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
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