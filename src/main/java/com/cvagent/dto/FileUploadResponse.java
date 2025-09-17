package com.cvagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "文件上传响应", title = "文件上传响应")
public class FileUploadResponse {

    @Schema(description = "文件ID", example = "file_123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "原始文件名", example = "简历.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalName;

    @Schema(description = "存储文件名", example = "resume_20250917.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storedName;

    @Schema(description = "文件类型", example = "application/pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contentType;

    @Schema(description = "文件大小（字节）", example = "1024000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long size;

    @Schema(description = "文件MD5值", example = "d41d8cd98f00b204e9800998ecf8427e", requiredMode = Schema.RequiredMode.REQUIRED)
    private String md5;

    @Schema(description = "上传时间", example = "2025-09-17T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime uploadTime;

    @Schema(description = "是否为新上传", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean newUpload;

    @Schema(description = "可读文件大小", example = "1MB", requiredMode = Schema.RequiredMode.REQUIRED)
    private String humanReadableSize;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getMd5() { return md5; }
    public void setMd5(String md5) { this.md5 = md5; }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }

    public boolean isNewUpload() { return newUpload; }
    public void setNewUpload(boolean newUpload) { this.newUpload = newUpload; }

    public String getHumanReadableSize() { return humanReadableSize; }
    public void setHumanReadableSize(String humanReadableSize) { this.humanReadableSize = humanReadableSize; }
}