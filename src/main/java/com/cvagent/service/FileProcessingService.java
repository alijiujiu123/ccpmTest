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
        try {
            // 使用Apache PDFBox提取PDF文本
            org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.pdmodel.PDDocument.load(filePath.toFile());
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            logger.info("PDF文本提取成功，文件长度: {}", text.length());
            return text;
        } catch (Exception e) {
            logger.error("PDF文本提取失败: {}", filePath, e);
            throw new IOException("PDF文本提取失败: " + e.getMessage());
        }
    }

    /**
     * 提取Word文档文本内容
     */
    private String extractWordText(Path filePath, String contentType) throws IOException {
        try {
            String text = "";

            if (contentType.equals("application/msword")) {
                // 处理 .doc 文件
                // 注意：HWPFDocument在较新版本的POI中可能不被完全支持
                // 这里暂时使用基础方法处理
                logger.warn("DOC文件处理需要额外配置，暂时返回基础内容");
                text = "DOC文件内容提取功能需要额外配置";
            } else if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                // 处理 .docx 文件
                org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument(
                    new java.io.FileInputStream(filePath.toFile())
                );
                org.apache.poi.xwpf.extractor.XWPFWordExtractor extractor = new org.apache.poi.xwpf.extractor.XWPFWordExtractor(document);
                text = extractor.getText();
                extractor.close();
                document.close();
            }

            logger.info("Word文档文本提取成功，文件长度: {}", text.length());
            return text;
        } catch (Exception e) {
            logger.error("Word文档文本提取失败: {}", filePath, e);
            throw new IOException("Word文档文本提取失败: " + e.getMessage());
        }
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
     * 解析简历文件并提取结构化信息
     */
    public Map<String, Object> parseResume(FileDocument fileDocument) {
        Map<String, Object> resumeData = new HashMap<>();

        try {
            String content = extractTextContent(fileDocument);

            // 提取基本信息
            resumeData.put("personalInfo", extractPersonalInfo(content));
            resumeData.put("education", extractEducation(content));
            resumeData.put("workExperience", extractWorkExperience(content));
            resumeData.put("skills", extractSkills(content));
            resumeData.put("projects", extractProjects(content));
            resumeData.put("contactInfo", extractContactInfo(content));

            logger.info("简历解析完成: {}", fileDocument.getId());
            return resumeData;

        } catch (Exception e) {
            logger.error("简历解析失败: {}", fileDocument.getId(), e);
            throw new RuntimeException("简历解析失败: " + e.getMessage());
        }
    }

    /**
     * 提取个人信息
     */
    private Map<String, String> extractPersonalInfo(String content) {
        Map<String, String> personalInfo = new HashMap<>();

        // 简单的姓名提取（中文姓名）
        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(
            "([\\u4e00-\\u9fa5]{2,4})\\s*(?:先生|女士|同学)"
        );
        java.util.regex.Matcher nameMatcher = namePattern.matcher(content);
        if (nameMatcher.find()) {
            personalInfo.put("name", nameMatcher.group(1));
        }

        // 年龄提取
        java.util.regex.Pattern agePattern = java.util.regex.Pattern.compile(
            "(\\d+)\\s*岁"
        );
        java.util.regex.Matcher ageMatcher = agePattern.matcher(content);
        if (ageMatcher.find()) {
            personalInfo.put("age", ageMatcher.group(1));
        }

        return personalInfo;
    }

    /**
     * 提取教育背景
     */
    private java.util.List<Map<String, String>> extractEducation(String content) {
        java.util.List<Map<String, String>> education = new java.util.ArrayList<>();

        // 匹配教育背景
        java.util.regex.Pattern eduPattern = java.util.regex.Pattern.compile(
            "([\\u4e00-\\u9fa5\\w\\s]+?)\\s*(?:大学|学院|学校)\\s*([\\u4e00-\\u9fa5\\w\\s]+?)\\s*(\\d{4})\\s*[~-]\\s*(\\d{4}|至今)"
        );
        java.util.regex.Matcher eduMatcher = eduPattern.matcher(content);

        while (eduMatcher.find()) {
            Map<String, String> eduItem = new HashMap<>();
            eduItem.put("school", eduMatcher.group(1) + eduMatcher.group(2));
            eduItem.put("major", eduMatcher.group(1));
            eduItem.put("startYear", eduMatcher.group(3));
            eduItem.put("endYear", eduMatcher.group(4));
            education.add(eduItem);
        }

        return education;
    }

    /**
     * 提取工作经验
     */
    private java.util.List<Map<String, String>> extractWorkExperience(String content) {
        java.util.List<Map<String, String>> experience = new java.util.ArrayList<>();

        // 匹配工作经验
        java.util.regex.Pattern expPattern = java.util.regex.Pattern.compile(
            "([\\u4e00-\\u9fa5\\w\\s]+?)\\s*(?:公司|有限公司|科技)\\s*([\\u4e00-\\u9fa5\\w\\s]+?)\\s*(\\d{4})\\s*[~-]\\s*(\\d{4}|至今)"
        );
        java.util.regex.Matcher expMatcher = expPattern.matcher(content);

        while (expMatcher.find()) {
            Map<String, String> expItem = new HashMap<>();
            expItem.put("company", expMatcher.group(1));
            expItem.put("position", expMatcher.group(2));
            expItem.put("startYear", expMatcher.group(3));
            expItem.put("endYear", expMatcher.group(4));
            experience.add(expItem);
        }

        return experience;
    }

    /**
     * 提取技能
     */
    private java.util.List<String> extractSkills(String content) {
        java.util.List<String> skills = new java.util.ArrayList<>();

        // 常见技能关键词
        String[] skillKeywords = {
            "Java", "Python", "JavaScript", "React", "Vue", "Spring", "MySQL",
            "MongoDB", "Docker", "Kubernetes", "Git", "Linux", "AWS", "微服务",
            "机器学习", "深度学习", "数据分析", "项目管理", "团队协作"
        };

        for (String skill : skillKeywords) {
            if (content.contains(skill)) {
                skills.add(skill);
            }
        }

        return skills;
    }

    /**
     * 提取项目经验
     */
    private java.util.List<Map<String, String>> extractProjects(String content) {
        java.util.List<Map<String, String>> projects = new java.util.ArrayList<>();

        // 匹配项目经验
        java.util.regex.Pattern projectPattern = java.util.regex.Pattern.compile(
            "([\\u4e00-\\u9fa5\\w\\s]+?)\\s*(?:项目|系统)\\s*([\\u4e00-\\u9fa5\\w\\s]+?)\\s*(\\d{4})\\s*[~-]\\s*(\\d{4}|至今)"
        );
        java.util.regex.Matcher projectMatcher = projectPattern.matcher(content);

        while (projectMatcher.find()) {
            Map<String, String> projectItem = new HashMap<>();
            projectItem.put("name", projectMatcher.group(1));
            projectItem.put("description", projectMatcher.group(2));
            projectItem.put("startYear", projectMatcher.group(3));
            projectItem.put("endYear", projectMatcher.group(4));
            projects.add(projectItem);
        }

        return projects;
    }

    /**
     * 提取联系信息
     */
    private Map<String, String> extractContactInfo(String content) {
        Map<String, String> contactInfo = new HashMap<>();

        // 邮箱提取
        java.util.regex.Pattern emailPattern = java.util.regex.Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        );
        java.util.regex.Matcher emailMatcher = emailPattern.matcher(content);
        if (emailMatcher.find()) {
            contactInfo.put("email", emailMatcher.group());
        }

        // 电话提取
        java.util.regex.Pattern phonePattern = java.util.regex.Pattern.compile(
            "(?:1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})"
        );
        java.util.regex.Matcher phoneMatcher = phonePattern.matcher(content);
        if (phoneMatcher.find()) {
            contactInfo.put("phone", phoneMatcher.group());
        }

        return contactInfo;
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