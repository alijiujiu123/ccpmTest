package com.cvagent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Document(collection = "files")
public class FileDocument {

    @Id
    private String id;

    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    private String originalName;

    private String storedName;
    private String path;

    @NotBlank(message = "文件类型不能为空")
    private String contentType;

    @NotNull(message = "文件大小不能为空")
    private Long size;

    private String md5;
    private String description;

    @NotNull(message = "所属用户不能为空")
    @DBRef
    private User user;

    @DBRef
    private Resume relatedResume;

    private LocalDateTime uploadTime;
    private LocalDateTime lastAccessed;

    // 构造函数
    public FileDocument() {
        this.uploadTime = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
    }

    public FileDocument(String originalName, String contentType, Long size, User user) {
        this();
        this.originalName = originalName;
        this.contentType = contentType;
        this.size = size;
        this.user = user;
        this.storedName = generateStoredName();
    }

    // 生成存储文件名
    private String generateStoredName() {
        return System.currentTimeMillis() + "_" + originalName;
    }

    // 获取文件大小的人类可读格式
    public String getHumanReadableSize() {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024.0));
        return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
    }

    // 检查文件类型是否为支持的格式
    public boolean isSupportedFormat() {
        String type = contentType.toLowerCase();
        return type.startsWith("image/") ||
               type.equals("application/pdf") ||
               type.equals("application/msword") ||
               type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getMd5() { return md5; }
    public void setMd5(String md5) { this.md5 = md5; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Resume getRelatedResume() { return relatedResume; }
    public void setRelatedResume(Resume relatedResume) { this.relatedResume = relatedResume; }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }

    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }

    // 更新访问时间
    public void updateAccessTime() {
        this.lastAccessed = LocalDateTime.now();
    }
}