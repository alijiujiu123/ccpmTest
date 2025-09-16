package com.cvagent.service;

import com.cvagent.model.FileDocument;
import com.cvagent.model.User;
import com.cvagent.repository.FileDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessingService.class);

    @Autowired
    private FileDocumentRepository fileDocumentRepository;

    /**
     * 提取文件内容（文本）
     */
    public String extractTextContent(FileDocument fileDocument) {
        try {
            Path filePath = Paths.get(fileDocument.getPath());
            String contentType = fileDocument.getContentType();

            switch (contentType) {
                case "application/pdf":
                    return extractPdfText(filePath);
                case "application/msword":
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                    return extractWordText(filePath, contentType);
                case "text/plain":
                    return new String(Files.readAllBytes(filePath));
                default:
                    logger.warn("不支持的文本提取格式: {}", contentType);
                    return "";
            }
        } catch (Exception e) {
            logger.error("提取文件内容失败: {}", fileDocument.getId(), e);
            return "";
        }
    }

    /**
     * 提取PDF文本内容
     */
    private String extractPdfText(Path filePath) throws IOException {
        // 这里应该集成PDF处理库，如Apache PDFBox
        // 暂时返回占位符
        logger.info("PDF文本提取功能待实现");
        return "PDF内容提取功能待实现";
    }

    /**
     * 提取Word文档文本内容
     */
    private String extractWordText(Path filePath, String contentType) throws IOException {
        // 这里应该集成Word处理库，如Apache POI
        // 暂时返回占位符
        logger.info("Word文档文本提取功能待实现");
        return "Word文档内容提取功能待实现";
    }

    /**
     * 分析文件内容并返回关键词
     */
    public Map<String, Integer> analyzeKeywords(FileDocument fileDocument) {
        String content = extractTextContent(fileDocument);
        Map<String, Integer> keywords = new HashMap<>();

        // 简单的关键词提取实现
        // 实际项目中可以使用更复杂的NLP算法
        String[] words = content.toLowerCase()
                .replaceAll("[^a-zA-Z\\s\\u4e00-\\u9fff]", " ")
                .split("\\s+");

        for (String word : words) {
            if (word.length() > 2) { // 过滤过短的词
                keywords.put(word, keywords.getOrDefault(word, 0) + 1);
            }
        }

        logger.info("文件 {} 关键词分析完成，共 {} 个关键词", fileDocument.getId(), keywords.size());
        return keywords;
    }

    /**
     * 验证文件安全性
     */
    public boolean validateFileSecurity(FileDocument fileDocument) {
        try {
            Path filePath = Paths.get(fileDocument.getPath());

            // 检查文件大小
            if (fileDocument.getSize() > 50 * 1024 * 1024) { // 50MB
                logger.warn("文件过大: {} bytes", fileDocument.getSize());
                return false;
            }

            // 检查文件扩展名
            String originalName = fileDocument.getOriginalName().toLowerCase();
            if (!originalName.matches(".*\\.(pdf|doc|docx|txt|jpg|jpeg|png|gif)$")) {
                logger.warn("不支持的文件扩展名: {}", originalName);
                return false;
            }

            // 这里可以添加更多的安全检查，如：
            // - 病毒扫描
            // - 文件内容验证
            // - 恶意代码检测

            logger.info("文件安全性验证通过: {}", fileDocument.getId());
            return true;

        } catch (Exception e) {
            logger.error("文件安全性验证失败: {}", fileDocument.getId(), e);
            return false;
        }
    }

    /**
     * 获取文件元数据
     */
    public Map<String, Object> getFileMetadata(FileDocument fileDocument) {
        Map<String, Object> metadata = new HashMap<>();

        try {
            Path filePath = Paths.get(fileDocument.getPath());

            // 基本元数据
            metadata.put("fileId", fileDocument.getId());
            metadata.put("originalName", fileDocument.getOriginalName());
            metadata.put("contentType", fileDocument.getContentType());
            metadata.put("size", fileDocument.getSize());
            metadata.put("md5", fileDocument.getMd5());
            metadata.put("uploadTime", fileDocument.getUploadTime());
            metadata.put("lastAccessed", fileDocument.getLastAccessed());

            // 文件系统元数据
            metadata.put("fileSize", Files.size(filePath));
            metadata.put("lastModified", Files.getLastModifiedTime(filePath));
            metadata.put("isReadable", Files.isReadable(filePath));
            metadata.put("isWritable", Files.isWritable(filePath));

            // 内容分析结果
            Map<String, Integer> keywords = analyzeKeywords(fileDocument);
            metadata.put("keywords", keywords);
            metadata.put("keywordCount", keywords.size());

            // 安全验证结果
            metadata.put("securityValid", validateFileSecurity(fileDocument));

            logger.info("文件元数据提取完成: {}", fileDocument.getId());
            return metadata;

        } catch (Exception e) {
            logger.error("获取文件元数据失败: {}", fileDocument.getId(), e);
            return metadata;
        }
    }

    /**
     * 检查文件是否为简历文件
     */
    public boolean isResumeFile(FileDocument fileDocument) {
        String contentType = fileDocument.getContentType();
        String originalName = fileDocument.getOriginalName().toLowerCase();

        // 基于文件类型和名称判断
        return contentType.equals("application/pdf") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               originalName.contains("resume") ||
               originalName.contains("简历") ||
               originalName.contains("cv");
    }

    /**
     * 清理过期文件
     */
    public void cleanupExpiredFiles() {
        // 清理30天未访问的文件
        // 这个方法可以定期调用
        logger.info("开始清理过期文件");

        // 实际实现需要查询数据库并删除过期文件
        // 这里只是示例
        logger.info("过期文件清理功能待实现");
    }
}