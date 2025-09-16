package com.cvagent.service;

import com.cvagent.model.CoverLetter;
import com.cvagent.model.CoverLetterTemplate;
import com.cvagent.repository.CoverLetterRepository;
import com.cvagent.repository.CoverLetterTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 求职信导出服务
 * 负责将求职信导出为不同格式（HTML、Word、PDF等）
 */
@Service
public class CoverLetterExportService {

    @Autowired
    private CoverLetterRepository coverLetterRepository;

    @Autowired
    private CoverLetterTemplateRepository coverLetterTemplateRepository;

    /**
     * 导出求职信为指定格式
     */
    public ExportResult exportCoverLetter(String coverLetterId, String format) {
        CoverLetter coverLetter = coverLetterRepository.findById(coverLetterId)
                .orElseThrow(() -> new RuntimeException("求职信不存在"));

        try {
            byte[] content;
            String contentType;
            String filename = generateFilename(coverLetter, format);

            switch (format.toLowerCase()) {
                case "html":
                    content = exportToHtml(coverLetter);
                    contentType = "text/html";
                    break;
                case "docx":
                    content = exportToWord(coverLetter);
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    break;
                case "pdf":
                    content = exportToPdf(coverLetter);
                    contentType = "application/pdf";
                    break;
                default:
                    throw new RuntimeException("不支持的导出格式: " + format);
            }

            // 更新下载统计
            coverLetterRepository.incrementDownloadCount(coverLetterId);

            return new ExportResult(content, contentType, filename);

        } catch (Exception e) {
            throw new RuntimeException("导出求职信失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出求职信和简历组合包
     */
    public ExportResult exportCombinedPackage(String coverLetterId, String resumeContent, String format) {
        CoverLetter coverLetter = coverLetterRepository.findById(coverLetterId)
                .orElseThrow(() -> new RuntimeException("求职信不存在"));

        try {
            byte[] content;
            String contentType;
            String filename = generateCombinedFilename(coverLetter, format);

            switch (format.toLowerCase()) {
                case "html":
                    content = exportCombinedToHtml(coverLetter, resumeContent);
                    contentType = "text/html";
                    break;
                case "docx":
                    content = exportCombinedToWord(coverLetter, resumeContent);
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                    break;
                case "pdf":
                    content = exportCombinedToPdf(coverLetter, resumeContent);
                    contentType = "application/pdf";
                    break;
                default:
                    throw new RuntimeException("不支持的导出格式: " + format);
            }

            return new ExportResult(content, contentType, filename);

        } catch (Exception e) {
            throw new RuntimeException("导出组合文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出为HTML格式
     */
    private byte[] exportToHtml(CoverLetter coverLetter) {
        String htmlTemplate = buildHtmlTemplate(coverLetter);
        return htmlTemplate.getBytes();
    }

    /**
     * 导出为Word格式
     */
    private byte[] exportToWord(CoverLetter coverLetter) throws IOException {
        // 简化的Word文档生成（HTML格式）
        String wordContent = buildWordContent(coverLetter);
        return wordContent.getBytes();
    }

    /**
     * 导出为PDF格式
     */
    private byte[] exportToPdf(CoverLetter coverLetter) {
        // 简化的PDF导出（返回HTML内容）
        String htmlContent = buildHtmlTemplate(coverLetter);
        return htmlContent.getBytes();
    }

    /**
     * 导出组合包为HTML格式
     */
    private byte[] exportCombinedToHtml(CoverLetter coverLetter, String resumeContent) {
        String combinedHtml = buildCombinedHtmlTemplate(coverLetter, resumeContent);
        return combinedHtml.getBytes();
    }

    /**
     * 导出组合包为Word格式
     */
    private byte[] exportCombinedToWord(CoverLetter coverLetter, String resumeContent) throws IOException {
        String combinedWordContent = buildCombinedWordContent(coverLetter, resumeContent);
        return combinedWordContent.getBytes();
    }

    /**
     * 导出组合包为PDF格式
     */
    private byte[] exportCombinedToPdf(CoverLetter coverLetter, String resumeContent) {
        String combinedHtml = buildCombinedHtmlTemplate(coverLetter, resumeContent);
        return combinedHtml.getBytes();
    }

    /**
     * 构建HTML模板
     */
    private String buildHtmlTemplate(CoverLetter coverLetter) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>")
            .append("<html lang=\"").append(coverLetter.getLanguage()).append("\">")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            .append("<title>").append(coverLetter.getTitle()).append("</title>")
            .append("<style>")
            .append(getHtmlStyles())
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<div class=\"cover-letter\">")
            .append("<div class=\"header\">")
            .append("<h1>").append(coverLetter.getTitle()).append("</h1>")
            .append("<p>").append(coverLetter.getCompanyName()).append(" - ").append(coverLetter.getPosition()).append("</p>")
            .append("</div>");

        CoverLetter.CoverLetterContent content = coverLetter.getContent();
        if (content != null) {
            if (content.getSalutation() != null) {
                html.append("<div class=\"salutation\">").append(content.getSalutation()).append("</div>");
            }
            if (content.getOpeningParagraph() != null) {
                html.append("<div class=\"opening\">").append(content.getOpeningParagraph()).append("</div>");
            }
            if (content.getBodyParagraphs() != null) {
                html.append("<div class=\"body\">").append(content.getBodyParagraphs().replace("\n", "<br>")).append("</div>");
            }
            if (content.getClosingParagraph() != null) {
                html.append("<div class=\"closing\">").append(content.getClosingParagraph()).append("</div>");
            }
            if (content.getSignature() != null) {
                html.append("<div class=\"signature\">").append(content.getSignature().replace("\n", "<br>")).append("</div>");
            }
            if (content.getContactInfo() != null) {
                html.append("<div class=\"contact\">").append(content.getContactInfo()).append("</div>");
            }
            if (content.getPostscript() != null) {
                html.append("<div class=\"postscript\">").append(content.getPostscript()).append("</div>");
            }
        }

        html.append("<div class=\"footer\">")
            .append("<p>生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>")
            .append("<p>AI优化: ").append(coverLetter.getAiOptimized() ? "是" : "否").append("</p>")
            .append("<p>匹配度: ").append(coverLetter.getMatchScore() != null ? String.format("%.1f%%", coverLetter.getMatchScore() * 100) : "未评分").append("</p>")
            .append("</div>")
            .append("</div>")
            .append("</body>")
            .append("</html>");

        return html.toString();
    }

    /**
     * 构建Word内容
     */
    private String buildWordContent(CoverLetter coverLetter) {
        StringBuilder wordContent = new StringBuilder();

        wordContent.append("<html xmlns:o='urn:schemas-microsoft-com:office:office' ")
                  .append("xmlns:w='urn:schemas-microsoft-com:office:word' ")
                  .append("xmlns='http://www.w3.org/TR/REC-html40'>")
                  .append("<head><meta charset='utf-8'><title>").append(coverLetter.getTitle()).append("</title></head>")
                  .append("<body>");

        // 添加Word格式的样式
        wordContent.append("<style>")
                  .append("body { font-family: 'SimSun'; font-size: 12pt; line-height: 1.5; }")
                  .append("h1 { font-size: 16pt; font-weight: bold; margin-bottom: 10pt; }")
                  .append("p { margin-bottom: 8pt; }")
                  .append(".header { margin-bottom: 20pt; }")
                  .append(".footer { margin-top: 20pt; font-size: 10pt; color: #666; }")
                  .append("</style>");

        wordContent.append("<div class='header'>")
                  .append("<h1>").append(coverLetter.getTitle()).append("</h1>")
                  .append("<p>").append(coverLetter.getCompanyName()).append(" - ").append(coverLetter.getPosition()).append("</p>")
                  .append("</div>");

        CoverLetter.CoverLetterContent content = coverLetter.getContent();
        if (content != null) {
            if (content.getSalutation() != null) {
                wordContent.append("<p>").append(content.getSalutation()).append("</p>");
            }
            if (content.getOpeningParagraph() != null) {
                wordContent.append("<p>").append(content.getOpeningParagraph()).append("</p>");
            }
            if (content.getBodyParagraphs() != null) {
                wordContent.append("<p>").append(content.getBodyParagraphs().replace("\n", "<br>")).append("</p>");
            }
            if (content.getClosingParagraph() != null) {
                wordContent.append("<p>").append(content.getClosingParagraph()).append("</p>");
            }
            if (content.getSignature() != null) {
                wordContent.append("<p>").append(content.getSignature().replace("\n", "<br>")).append("</p>");
            }
            if (content.getContactInfo() != null) {
                wordContent.append("<p>").append(content.getContactInfo()).append("</p>");
            }
            if (content.getPostscript() != null) {
                wordContent.append("<p>").append(content.getPostscript()).append("</p>");
            }
        }

        wordContent.append("<div class='footer'>")
                  .append("<p>生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>")
                  .append("<p>AI优化: ").append(coverLetter.getAiOptimized() ? "是" : "否").append("</p>")
                  .append("</div>")
                  .append("</body></html>");

        return wordContent.toString();
    }

    /**
     * 构建组合HTML模板
     */
    private String buildCombinedHtmlTemplate(CoverLetter coverLetter, String resumeContent) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>")
            .append("<html lang=\"").append(coverLetter.getLanguage()).append("\">")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            .append("<title>").append(coverLetter.getTitle()).append(" - 申请材料包</title>")
            .append("<style>")
            .append(getCombinedHtmlStyles())
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<div class=\"package\">")
            .append("<h1>求职申请材料包</h1>")
            .append("<div class=\"cover-letter-section\">")
            .append("<h2>求职信</h2>");

        CoverLetter.CoverLetterContent content = coverLetter.getContent();
        if (content != null) {
            if (content.getSalutation() != null) {
                html.append("<div class=\"salutation\">").append(content.getSalutation()).append("</div>");
            }
            if (content.getOpeningParagraph() != null) {
                html.append("<div class=\"opening\">").append(content.getOpeningParagraph()).append("</div>");
            }
            if (content.getBodyParagraphs() != null) {
                html.append("<div class=\"body\">").append(content.getBodyParagraphs().replace("\n", "<br>")).append("</div>");
            }
            if (content.getClosingParagraph() != null) {
                html.append("<div class=\"closing\">").append(content.getClosingParagraph()).append("</div>");
            }
            if (content.getSignature() != null) {
                html.append("<div class=\"signature\">").append(content.getSignature().replace("\n", "<br>")).append("</div>");
            }
        }

        html.append("</div>")
            .append("<div class=\"resume-section\">")
            .append("<h2>个人简历</h2>")
            .append("<div class=\"resume-content\">").append(resumeContent).append("</div>")
            .append("</div>")
            .append("<div class=\"footer\">")
            .append("<p>生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>")
            .append("<p>申请职位: ").append(coverLetter.getCompanyName()).append(" - ").append(coverLetter.getPosition()).append("</p>")
            .append("</div>")
            .append("</div>")
            .append("</body>")
            .append("</html>");

        return html.toString();
    }

    /**
     * 构建组合Word内容
     */
    private String buildCombinedWordContent(CoverLetter coverLetter, String resumeContent) {
        StringBuilder wordContent = new StringBuilder();

        wordContent.append("<html xmlns:o='urn:schemas-microsoft-com:office:office' ")
                  .append("xmlns:w='urn:schemas-microsoft-com:office:word' ")
                  .append("xmlns='http://www.w3.org/TR/REC-html40'>")
                  .append("<head><meta charset='utf-8'><title>").append(coverLetter.getTitle()).append(" - 申请材料包</title></head>")
                  .append("<body>");

        wordContent.append("<h1>求职申请材料包</h1>")
                  .append("<h2>求职信</h2>");

        CoverLetter.CoverLetterContent content = coverLetter.getContent();
        if (content != null) {
            if (content.getSalutation() != null) {
                wordContent.append("<p>").append(content.getSalutation()).append("</p>");
            }
            if (content.getOpeningParagraph() != null) {
                wordContent.append("<p>").append(content.getOpeningParagraph()).append("</p>");
            }
            if (content.getBodyParagraphs() != null) {
                wordContent.append("<p>").append(content.getBodyParagraphs().replace("\n", "<br>")).append("</p>");
            }
            if (content.getClosingParagraph() != null) {
                wordContent.append("<p>").append(content.getClosingParagraph()).append("</p>");
            }
            if (content.getSignature() != null) {
                wordContent.append("<p>").append(content.getSignature().replace("\n", "<br>")).append("</p>");
            }
        }

        wordContent.append("<h2>个人简历</h2>")
                  .append("<div>").append(resumeContent).append("</div>");

        wordContent.append("</body></html>");
        return wordContent.toString();
    }

    /**
     * 生成文件名
     */
    private String generateFilename(CoverLetter coverLetter, String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safeTitle = coverLetter.getTitle().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
        return String.format("%s_%s.%s", safeTitle, timestamp, format);
    }

    /**
     * 生成组合包文件名
     */
    private String generateCombinedFilename(CoverLetter coverLetter, String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safeCompany = coverLetter.getCompanyName().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
        return String.format("申请材料包_%s_%s.%s", safeCompany, timestamp, format);
    }

    /**
     * 获取HTML样式
     */
    private String getHtmlStyles() {
        return """
            body {
                font-family: 'Microsoft YaHei', Arial, sans-serif;
                line-height: 1.6;
                color: #333;
                max-width: 800px;
                margin: 0 auto;
                padding: 20px;
                background-color: #f9f9f9;
            }
            .cover-letter {
                background: white;
                padding: 40px;
                border-radius: 8px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            }
            .header {
                text-align: center;
                margin-bottom: 30px;
                border-bottom: 2px solid #007bff;
                padding-bottom: 20px;
            }
            .header h1 {
                color: #007bff;
                margin: 0 0 10px 0;
            }
            .salutation {
                margin: 20px 0;
                font-weight: bold;
            }
            .opening {
                margin: 20px 0;
            }
            .body {
                margin: 20px 0;
            }
            .closing {
                margin: 20px 0;
            }
            .signature {
                margin: 30px 0 20px 0;
                text-align: right;
            }
            .contact {
                margin: 10px 0;
                font-size: 0.9em;
                color: #666;
            }
            .postscript {
                margin: 20px 0;
                font-style: italic;
                color: #555;
            }
            .footer {
                margin-top: 30px;
                padding-top: 20px;
                border-top: 1px solid #ddd;
                font-size: 0.8em;
                color: #888;
                text-align: center;
            }
            """;
    }

    /**
     * 获取组合HTML样式
     */
    private String getCombinedHtmlStyles() {
        return """
            body {
                font-family: 'Microsoft YaHei', Arial, sans-serif;
                line-height: 1.6;
                color: #333;
                max-width: 900px;
                margin: 0 auto;
                padding: 20px;
                background-color: #f9f9f9;
            }
            .package {
                background: white;
                padding: 30px;
                border-radius: 8px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            }
            h1 {
                text-align: center;
                color: #007bff;
                margin-bottom: 30px;
            }
            .cover-letter-section, .resume-section {
                margin: 30px 0;
                padding: 20px;
                border: 1px solid #ddd;
                border-radius: 5px;
            }
            .cover-letter-section {
                background-color: #f8f9fa;
            }
            .resume-section {
                background-color: #fff3cd;
            }
            h2 {
                color: #495057;
                border-bottom: 2px solid #dee2e6;
                padding-bottom: 10px;
                margin-top: 0;
            }
            .footer {
                margin-top: 30px;
                padding-top: 20px;
                border-top: 1px solid #ddd;
                font-size: 0.8em;
                color: #888;
                text-align: center;
            }
            .resume-content {
                background: white;
                padding: 15px;
                border-radius: 3px;
            }
            """;
    }

    /**
     * 导出结果类
     */
    public static class ExportResult {
        private final byte[] content;
        private final String contentType;
        private final String filename;

        public ExportResult(byte[] content, String contentType, String filename) {
            this.content = content;
            this.contentType = contentType;
            this.filename = filename;
        }

        public byte[] getContent() {
            return content;
        }

        public String getContentType() {
            return contentType;
        }

        public String getFilename() {
            return filename;
        }
    }
}