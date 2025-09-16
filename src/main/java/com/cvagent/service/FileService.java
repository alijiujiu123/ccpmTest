package com.cvagent.service;

import com.cvagent.dto.FileUploadResponse;
import com.cvagent.model.FileDocument;
import com.cvagent.model.User;
import com.cvagent.repository.FileDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private FileDocumentRepository fileDocumentRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.max-file-size:10485760}") // 10MB
    private long maxFileSize;

    // 支持的文件类型
    private static final String[] ALLOWED_TYPES = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    };

    /**
     * 上传文件
     */
    public FileUploadResponse uploadFile(MultipartFile file, User user, String description) throws IOException {
        // 验证文件
        validateFile(file);

        // 创建上传目录
        createUploadDirectory();

        // 生成存储文件名
        String storedName = generateStoredName(file.getOriginalFilename());

        // 计算文件MD5
        String md5 = calculateMD5(file);

        // 检查文件是否已存在
        FileDocument existingFile = fileDocumentRepository.findByMd5(md5).stream()
                .findFirst()
                .orElse(null);

        if (existingFile != null) {
            logger.info("文件已存在，复用现有文件: {}", existingFile.getId());
            return createResponse(existingFile, false);
        }

        // 保存文件到磁盘
        Path filePath = Paths.get(uploadDir, storedName);
        Files.copy(file.getInputStream(), filePath);

        // 创建文件文档记录
        FileDocument fileDocument = new FileDocument(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                user
        );
        fileDocument.setStoredName(storedName);
        fileDocument.setPath(filePath.toString());
        fileDocument.setMd5(md5);
        fileDocument.setDescription(description);

        FileDocument savedFile = fileDocumentRepository.save(fileDocument);

        logger.info("文件上传成功: {}", savedFile.getId());
        return createResponse(savedFile, true);
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileId, User user) {
        FileDocument fileDocument = fileDocumentRepository.findByIdAndUserId(fileId, user.getId())
                .orElseThrow(() -> new RuntimeException("文件不存在或无权限"));

        try {
            // 删除物理文件
            Path filePath = Paths.get(fileDocument.getPath());
            Files.deleteIfExists(filePath);

            // 删除数据库记录
            fileDocumentRepository.delete(fileDocument);

            logger.info("文件删除成功: {}", fileId);
        } catch (IOException e) {
            logger.error("删除文件失败: {}", fileId, e);
            throw new RuntimeException("删除文件失败");
        }
    }

    /**
     * 获取用户文件列表
     */
    public java.util.List<FileDocument> getUserFiles(User user) {
        return fileDocumentRepository.findByUserId(user.getId());
    }

    /**
     * 根据ID获取文件
     */
    public FileDocument getFileById(String fileId, User user) {
        return fileDocumentRepository.findByIdAndUserId(fileId, user.getId())
                .orElseThrow(() -> new RuntimeException("文件不存在或无权限"));
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("文件大小超过限制（最大 " + (maxFileSize / 1024 / 1024) + "MB）");
        }

        String contentType = file.getContentType();
        if (!isAllowedType(contentType)) {
            throw new RuntimeException("不支持的文件类型: " + contentType);
        }
    }

    /**
     * 检查文件类型是否允许
     */
    private boolean isAllowedType(String contentType) {
        if (contentType == null) return false;

        for (String allowedType : ALLOWED_TYPES) {
            if (allowedType.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建上传目录
     */
    private void createUploadDirectory() throws IOException {
        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    /**
     * 生成存储文件名
     */
    private String generateStoredName(String originalName) {
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 计算文件MD5
     */
    private String calculateMD5(MultipartFile file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = file.getBytes();
            byte[] digest = md.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.error("计算MD5失败", e);
            throw new RuntimeException("计算文件MD5失败");
        }
    }

    /**
     * 创建上传响应
     */
    private FileUploadResponse createResponse(FileDocument fileDocument, boolean isNewUpload) {
        FileUploadResponse response = new FileUploadResponse();
        response.setId(fileDocument.getId());
        response.setOriginalName(fileDocument.getOriginalName());
        response.setStoredName(fileDocument.getStoredName());
        response.setContentType(fileDocument.getContentType());
        response.setSize(fileDocument.getSize());
        response.setMd5(fileDocument.getMd5());
        response.setUploadTime(fileDocument.getUploadTime());
        response.setNewUpload(isNewUpload);
        response.setHumanReadableSize(fileDocument.getHumanReadableSize());
        return response;
    }

    /**
     * 获取文件内容
     */
    public byte[] getFileContent(String fileId, User user) {
        FileDocument fileDocument = getFileById(fileId, user);

        try {
            Path filePath = Paths.get(fileDocument.getPath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            logger.error("读取文件内容失败: {}", fileId, e);
            throw new RuntimeException("读取文件失败");
        }
    }

    /**
     * 更新文件访问时间
     */
    public void updateAccessTime(String fileId, User user) {
        FileDocument fileDocument = getFileById(fileId, user);
        fileDocument.updateAccessTime();
        fileDocumentRepository.save(fileDocument);
    }
}