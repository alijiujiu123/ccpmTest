package com.cvagent.controller;

import com.cvagent.dto.FileUploadResponse;
import com.cvagent.model.FileDocument;
import com.cvagent.model.User;
import com.cvagent.repository.FileDocumentRepository;
import com.cvagent.repository.UserRepository;
import com.cvagent.security.UserPrincipal;
import com.cvagent.service.FileService;
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

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private FileDocumentRepository fileDocumentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
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
    public ResponseEntity<List<FileDocument>> getUserFiles(
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
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable String fileId,
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
    public ResponseEntity<byte[]> previewFile(
            @PathVariable String fileId,
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
    public ResponseEntity<Void> deleteFile(
            @PathVariable String fileId,
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
    public ResponseEntity<FileDocument> getFileInfo(
            @PathVariable String fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        FileDocument fileDocument = fileService.getFileById(fileId, user);

        logger.info("用户 {} 查询文件信息: {}", user.getUsername(), fileId);
        return ResponseEntity.ok(fileDocument);
    }

    /**
     * 检查文件是否存在（用于断点续传）
     */
    @PostMapping("/check-existence")
    public ResponseEntity<FileUploadResponse> checkFileExistence(
            @RequestParam("md5") String md5,
            @RequestParam("size") Long size,
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