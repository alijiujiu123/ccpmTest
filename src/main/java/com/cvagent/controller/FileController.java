package com.cvagent.controller;

import com.cvagent.dto.FileUploadResponse;
import com.cvagent.model.FileDocument;
import com.cvagent.model.User;
import com.cvagent.repository.FileDocumentRepository;
import com.cvagent.repository.UserRepository;
import com.cvagent.security.UserPrincipal;
import com.cvagent.service.FileService;
import com.cvagent.service.FileProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传、下载、预览和处理相关接口")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private FileDocumentRepository fileDocumentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileProcessingService fileProcessingService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到服务器")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "上传成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "要上传的文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件描述", required = false)
            @RequestParam(value = "description", required = false) String description,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) throws IOException {

        logger.info("用户 {} 开始上传文件: {}", userPrincipal.getUsername(), file.getOriginalFilename());

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        FileUploadResponse response = fileService.uploadFile(file, user, description);

        logger.info("文件上传成功: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户文件列表
     */
    @GetMapping
    @Operation(summary = "获取文件列表", description = "获取用户的所有文件列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<List<FileDocument>> getUserFiles(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        List<FileDocument> files = fileService.getUserFiles(user);

        logger.info("用户 {} 查询文件列表，共 {} 个文件", user.getUsername(), files.size());
        return ResponseEntity.ok(files);
    }

    /**
     * 下载文件
     */
    @GetMapping("/{fileId}/download")
    @Operation(summary = "下载文件", description = "下载指定的文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "下载成功"),
        @ApiResponse(responseCode = "404", description = "文件不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<byte[]> downloadFile(
            @Parameter(description = "文件ID", required = true, example = "file123")
            @PathVariable String fileId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        FileDocument fileDocument = fileService.getFileById(fileId, user);

        // 更新访问时间
        fileService.updateAccessTime(fileId, user);

        // 读取文件内容
        byte[] fileContent = fileService.getFileContent(fileId, user);

        logger.info("用户 {} 下载文件: {}", user.getUsername(), fileDocument.getOriginalName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileDocument.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileDocument.getOriginalName() + "\"")
                .body(fileContent);
    }

    /**
     * 预览文件（仅支持图片和PDF）
     */
    @GetMapping("/{fileId}/preview")
    @Operation(summary = "预览文件", description = "预览支持的文件类型（图片和PDF）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "预览成功"),
        @ApiResponse(responseCode = "400", description = "文件类型不支持预览"),
        @ApiResponse(responseCode = "404", description = "文件不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<byte[]> previewFile(
            @Parameter(description = "文件ID", required = true, example = "file123")
            @PathVariable String fileId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        FileDocument fileDocument = fileService.getFileById(fileId, user);

        // 检查文件类型是否支持预览
        String contentType = fileDocument.getContentType();
        if (!contentType.startsWith("image/") && !contentType.equals("application/pdf")) {
            throw new RuntimeException("该文件类型不支持预览");
        }

        // 更新访问时间
        fileService.updateAccessTime(fileId, user);

        // 读取文件内容
        byte[] fileContent = fileService.getFileContent(fileId, user);

        logger.info("用户 {} 预览文件: {}", user.getUsername(), fileDocument.getOriginalName());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(fileContent);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件", description = "删除指定的文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "删除成功"),
        @ApiResponse(responseCode = "404", description = "文件不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "文件ID", required = true, example = "file123")
            @PathVariable String fileId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        logger.info("用户 {} 删除文件: {}", user.getUsername(), fileId);

        fileService.deleteFile(fileId, user);

        logger.info("文件删除成功: {}", fileId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/{fileId}")
    @Operation(summary = "获取文件信息", description = "获取文件的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "文件不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<FileDocument> getFileInfo(
            @Parameter(description = "文件ID", required = true, example = "file123")
            @PathVariable String fileId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        FileDocument fileDocument = fileService.getFileById(fileId, user);

        logger.info("用户 {} 查询文件信息: {}", user.getUsername(), fileId);
        return ResponseEntity.ok(fileDocument);
    }

    /**
     * 解析简历文件
     */
    @PostMapping("/{fileId}/parse-resume")
    @Operation(summary = "解析简历文件", description = "解析简历文件并提取关键信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "解析成功"),
        @ApiResponse(responseCode = "400", description = "文件不是简历文件"),
        @ApiResponse(responseCode = "404", description = "文件不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Map<String, Object>> parseResume(
            @Parameter(description = "文件ID", required = true, example = "file123")
            @PathVariable String fileId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        FileDocument fileDocument = fileService.getFileById(fileId, user);

        // 检查是否为简历文件
        if (!fileProcessingService.isResumeFile(fileDocument)) {
            throw new RuntimeException("该文件不是简历文件，无法解析");
        }

        // 解析简历
        Map<String, Object> resumeData = fileProcessingService.parseResume(fileDocument);

        logger.info("用户 {} 解析简历文件: {}", user.getUsername(), fileId);
        return ResponseEntity.ok(resumeData);
    }

    /**
     * 提取文件文本内容
     */
    @GetMapping("/{fileId}/text-content")
    @Operation(summary = "提取文本内容", description = "提取文件的文本内容")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "提取成功"),
        @ApiResponse(responseCode = "404", description = "文件不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Map<String, String>> extractTextContent(
            @Parameter(description = "文件ID", required = true, example = "file123")
            @PathVariable String fileId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        FileDocument fileDocument = fileService.getFileById(fileId, user);

        // 提取文本内容
        String textContent = fileProcessingService.extractTextContent(fileDocument);
        Map<String, String> response = new HashMap<>();
        response.put("content", textContent);
        response.put("fileId", fileId);
        response.put("fileName", fileDocument.getOriginalName());

        logger.info("用户 {} 提取文件文本内容: {}", user.getUsername(), fileId);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取文件元数据
     */
    @GetMapping("/{fileId}/metadata")
    @Operation(summary = "获取文件元数据", description = "获取文件的元数据信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "文件不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Map<String, Object>> getFileMetadata(
            @Parameter(description = "文件ID", required = true, example = "file123")
            @PathVariable String fileId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        FileDocument fileDocument = fileService.getFileById(fileId, user);

        // 获取文件元数据
        Map<String, Object> metadata = fileProcessingService.getFileMetadata(fileDocument);

        logger.info("用户 {} 获取文件元数据: {}", user.getUsername(), fileId);
        return ResponseEntity.ok(metadata);
    }

    /**
     * 检查文件是否存在（用于断点续传）
     */
    @PostMapping("/check-existence")
    @Operation(summary = "检查文件存在性", description = "检查文件是否已存在，用于断点续传")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "文件已存在"),
        @ApiResponse(responseCode = "204", description = "文件不存在"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<FileUploadResponse> checkFileExistence(
            @Parameter(description = "文件MD5值", required = true, example = "d41d8cd98f00b204e9800998ecf8427e")
            @RequestParam("md5") String md5,
            @Parameter(description = "文件大小", required = true, example = "1024")
            @RequestParam("size") Long size,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        FileDocument existingFile = fileDocumentRepository.findByMd5(md5).stream()
                .filter(file -> file.getSize().equals(size))
                .findFirst()
                .orElse(null);

        if (existingFile != null) {
            FileUploadResponse response = new FileUploadResponse();
            response.setId(existingFile.getId());
            response.setOriginalName(existingFile.getOriginalName());
            response.setStoredName(existingFile.getStoredName());
            response.setContentType(existingFile.getContentType());
            response.setSize(existingFile.getSize());
            response.setMd5(existingFile.getMd5());
            response.setUploadTime(existingFile.getUploadTime());
            response.setNewUpload(false);
            response.setHumanReadableSize(existingFile.getHumanReadableSize());

            logger.info("文件已存在，MD5: {}", md5);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.noContent().build();
    }
}