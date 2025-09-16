package com.cvagent.dto;

import java.time.LocalDateTime;

public class FileUploadResponse {

    private String id;
    private String originalName;
    private String storedName;
    private String contentType;
    private Long size;
    private String md5;
    private LocalDateTime uploadTime;
    private boolean newUpload;
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