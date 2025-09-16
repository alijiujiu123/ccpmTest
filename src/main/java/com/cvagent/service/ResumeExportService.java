package com.cvagent.service;

import com.cvagent.model.EnhancedResume;
import com.cvagent.model.ResumeTemplate;
import com.itextpdf.html2pdf.HtmlConverter;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * 简历导出服务
 * 负责将简历数据导出为不同格式
 */
@Service
public class ResumeExportService {

    /**
     * 导出为PDF格式
     */
    public byte[] exportToPDF(EnhancedResume resume, ResumeTemplate template) {
        try {
            String htmlContent = generateHTMLContent(resume, template);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            HtmlConverter.convertToPdf(htmlContent, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出为Word格式
     */
    public byte[] exportToWord(EnhancedResume resume, ResumeTemplate template) {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 添加标题
            XWPFParagraph titleParagraph = document.createParagraph();
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(resume.getTitle());
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            // 添加个人信息
            if (resume.getPersonalInfo() != null) {
                addPersonalInfoToWord(document, resume.getPersonalInfo());
            }

            // 添加工作经历
            if (resume.getWorkExperience() != null) {
                addWorkExperienceToWord(document, resume.getWorkExperience());
            }

            // 添加教育背景
            if (resume.getEducation() != null) {
                addEducationToWord(document, resume.getEducation());
            }

            // 添加技能
            if (resume.getSkills() != null) {
                addSkillsToWord(document, resume.getSkills());
            }

            // 添加项目经验
            if (resume.getProjects() != null) {
                addProjectsToWord(document, resume.getProjects());
            }

            document.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Word导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出为HTML格式
     */
    public byte[] exportToHTML(EnhancedResume resume, ResumeTemplate template) {
        String htmlContent = generateHTMLContent(resume, template);
        return htmlContent.getBytes();
    }

    /**
     * 生成简历预览
     */
    public String generatePreview(EnhancedResume resume, ResumeTemplate template) {
        return generateHTMLContent(resume, template);
    }

    /**
     * 生成HTML内容
     */
    private String generateHTMLContent(EnhancedResume resume, ResumeTemplate template) {
        StringBuilder html = new StringBuilder();

        // HTML头部
        html.append("<!DOCTYPE html>")
           .append("<html lang=\"zh-CN\">")
           .append("<head>")
           .append("<meta charset=\"UTF-8\">")
           .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
           .append("<title>").append(resume.getTitle()).append("</title>")
           .append("<style>")
           .append(generateCSSStyles(template))
           .append("</style>")
           .append("</head>")
           .append("<body>");

        // 简历内容
        html.append("<div class=\"resume-container\">");

        // 标题
        html.append("<h1 class=\"resume-title\">").append(resume.getTitle()).append("</h1>");

        // 个人信息
        if (resume.getPersonalInfo() != null) {
            html.append(generatePersonalInfoHTML(resume.getPersonalInfo()));
        }

        // 工作经历
        if (resume.getWorkExperience() != null) {
            html.append(generateWorkExperienceHTML(resume.getWorkExperience()));
        }

        // 教育背景
        if (resume.getEducation() != null) {
            html.append(generateEducationHTML(resume.getEducation()));
        }

        // 技能
        if (resume.getSkills() != null) {
            html.append(generateSkillsHTML(resume.getSkills()));
        }

        // 项目经验
        if (resume.getProjects() != null) {
            html.append(generateProjectsHTML(resume.getProjects()));
        }

        // 匹配度和优化状态
        html.append("<div class=\"resume-metrics\">")
           .append("<div class=\"match-score\">匹配度: ").append(String.format("%.1f%%", resume.getMatchScore() * 100)).append("</div>")
           .append("<div class=\"optimization-status\">优化状态: ").append(getOptimizationStatusText(resume.getOptimizationStatus())).append("</div>")
           .append("</div>");

        html.append("</div></body></html>");

        return html.toString();
    }

    /**
     * 生成CSS样式
     */
    private String generateCSSStyles(ResumeTemplate template) {
        StringBuilder css = new StringBuilder();

        css.append("body { font-family: 'Microsoft YaHei', Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }")
           .append(".resume-container { max-width: 800px; margin: 0 auto; background-color: white; padding: 40px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }")
           .append(".resume-title { text-align: center; color: #333; margin-bottom: 30px; }")
           .append(".section { margin-bottom: 30px; }")
           .append(".section-title { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 5px; margin-bottom: 15px; }")
           .append(".personal-info { text-align: center; margin-bottom: 30px; }")
           .append(".personal-info h2 { margin: 0; color: #2c3e50; }")
           .append(".personal-info p { margin: 5px 0; color: #666; }")
           .append(".work-experience-item, .education-item, .project-item { margin-bottom: 20px; padding: 15px; background-color: #f8f9fa; border-radius: 5px; }")
           .append(".item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }")
           .append(".item-title { font-weight: bold; color: #2c3e50; }")
           .append(".item-date { color: #666; font-size: 0.9em; }")
           .append(".item-company, .item-school { color: #3498db; margin-bottom: 5px; }")
           .append(".item-description { color: #555; line-height: 1.6; }")
           .append(".skills-list { display: flex; flex-wrap: wrap; gap: 10px; }")
           .append(".skill-tag { background-color: #3498db; color: white; padding: 5px 10px; border-radius: 15px; font-size: 0.9em; }")
           .append(".resume-metrics { margin-top: 30px; padding: 15px; background-color: #e8f4f8; border-radius: 5px; text-align: center; }")
           .append(".match-score { font-size: 1.2em; font-weight: bold; color: #27ae60; }")
           .append(".optimization-status { margin-top: 10px; color: #666; }");

        // 应用模板样式
        if (template.getStyling() != null) {
            css.append(applyTemplateStyles(template.getStyling()));
        }

        return css.toString();
    }

    /**
     * 应用模板样式
     */
    private String applyTemplateStyles(ResumeTemplate.TemplateStyling styling) {
        StringBuilder styles = new StringBuilder();

        if (styling.getPrimaryColor() != null) {
            styles.append(".section-title { border-bottom-color: ").append(styling.getPrimaryColor()).append("; }")
                  .append(".item-title, .personal-info h2 { color: ").append(styling.getPrimaryColor()).append("; }");
        }

        if (styling.getBackgroundColor() != null) {
            styles.append(".resume-container { background-color: ").append(styling.getBackgroundColor()).append("; }");
        }

        if (styling.getTextColor() != null) {
            styles.append("body { color: ").append(styling.getTextColor()).append("; }");
        }

        return styles.toString();
    }

    /**
     * 生成个人信息HTML
     */
    private String generatePersonalInfoHTML(EnhancedResume.PersonalInfo personalInfo) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"section personal-info\">")
           .append("<h2>").append(personalInfo.getName()).append("</h2>")
           .append("<p>邮箱: ").append(personalInfo.getEmail()).append("</p>")
           .append("<p>电话: ").append(personalInfo.getPhone()).append("</p>")
           .append("<p>地址: ").append(personalInfo.getLocation()).append("</p>");

        if (personalInfo.getSummary() != null && !personalInfo.getSummary().isEmpty()) {
            html.append("<div class=\"summary\">").append("<p>").append(personalInfo.getSummary()).append("</p></div>");
        }

        html.append("</div>");
        return html.toString();
    }

    /**
     * 生成工作经历HTML
     */
    private String generateWorkExperienceHTML(EnhancedResume.WorkExperience workExperience) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"section\">")
           .append("<h3 class=\"section-title\">工作经历</h3>");

        // 这里需要根据实际的工作经历数据结构来生成HTML
        // 暂时提供一个示例结构
        html.append("<div class=\"work-experience-item\">")
           .append("<div class=\"item-header\">")
           .append("<span class=\"item-title\">职位名称</span>")
           .append("<span class=\"item-date\">2020.01 - 至今</span>")
           .append("</div>")
           .append("<div class=\"item-company\">公司名称</div>")
           .append("<div class=\"item-description\">工作描述...</div>")
           .append("</div>");

        html.append("</div>");
        return html.toString();
    }

    /**
     * 生成教育背景HTML
     */
    private String generateEducationHTML(EnhancedResume.Education education) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"section\">")
           .append("<h3 class=\"section-title\">教育背景</h3>");

        // 这里需要根据实际的教育背景数据结构来生成HTML
        html.append("<div class=\"education-item\">")
           .append("<div class=\"item-header\">")
           .append("<span class=\"item-title\">学位</span>")
           .append("<span class=\"item-date\">2016.09 - 2020.06</span>")
           .append("</div>")
           .append("<div class=\"item-school\">学校名称</div>")
           .append("<div class=\"item-description\">专业描述...</div>")
           .append("</div>");

        html.append("</div>");
        return html.toString();
    }

    /**
     * 生成技能HTML
     */
    private String generateSkillsHTML(EnhancedResume.Skills skills) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"section\">")
           .append("<h3 class=\"section-title\">技能</h3>")
           .append("<div class=\"skills-list\">");

        if (skills.getTechnicalSkills() != null) {
            for (String skill : skills.getTechnicalSkills()) {
                html.append("<span class=\"skill-tag\">").append(skill).append("</span>");
            }
        }

        html.append("</div></div>");
        return html.toString();
    }

    /**
     * 生成项目经验HTML
     */
    private String generateProjectsHTML(EnhancedResume.Projects projects) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"section\">")
           .append("<h3 class=\"section-title\">项目经验</h3>");

        // 这里需要根据实际的项目经验数据结构来生成HTML
        html.append("<div class=\"project-item\">")
           .append("<div class=\"item-header\">")
           .append("<span class=\"item-title\">项目名称</span>")
           .append("<span class=\"item-date\">2023.01 - 2023.06</span>")
           .append("</div>")
           .append("<div class=\"item-description\">项目描述...</div>")
           .append("</div>");

        html.append("</div>");
        return html.toString();
    }

    /**
     * 获取优化状态文本
     */
    private String getOptimizationStatusText(String status) {
        switch (status) {
            case "pending": return "待优化";
            case "processing": return "优化中";
            case "completed": return "优化完成";
            case "failed": return "优化失败";
            default: return "未知状态";
        }
    }

    // Word文档生成辅助方法
    private void addPersonalInfoToWord(XWPFDocument document, EnhancedResume.PersonalInfo personalInfo) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("个人信息");
        run.setBold(true);
        run.setFontSize(14);

        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setText("姓名: " + personalInfo.getName());
        run.addBreak();
        run.setText("邮箱: " + personalInfo.getEmail());
        run.addBreak();
        run.setText("电话: " + personalInfo.getPhone());
        run.addBreak();
        run.setText("地址: " + personalInfo.getLocation());
    }

    private void addWorkExperienceToWord(XWPFDocument document, EnhancedResume.WorkExperience workExperience) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("工作经历");
        run.setBold(true);
        run.setFontSize(14);

        // 这里需要根据实际的工作经历数据结构来添加内容
        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setText("工作经历内容...");
    }

    private void addEducationToWord(XWPFDocument document, EnhancedResume.Education education) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("教育背景");
        run.setBold(true);
        run.setFontSize(14);

        // 这里需要根据实际的教育背景数据结构来添加内容
        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setText("教育背景内容...");
    }

    private void addSkillsToWord(XWPFDocument document, EnhancedResume.Skills skills) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("技能");
        run.setBold(true);
        run.setFontSize(14);

        if (skills.getTechnicalSkills() != null) {
            paragraph = document.createParagraph();
            run = paragraph.createRun();
            run.setText("技术技能: " + String.join(", ", skills.getTechnicalSkills()));
        }
    }

    private void addProjectsToWord(XWPFDocument document, EnhancedResume.Projects projects) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("项目经验");
        run.setBold(true);
        run.setFontSize(14);

        // 这里需要根据实际的项目经验数据结构来添加内容
        paragraph = document.createParagraph();
        run = paragraph.createRun();
        run.setText("项目经验内容...");
    }
}