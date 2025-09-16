package com.cvagent.service;

import com.cvagent.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown内容处理服务
 * 提供Markdown内容的解析、转换和处理功能
 */
@Service
public class MarkdownService {

    private static final Logger logger = LoggerFactory.getLogger(MarkdownService.class);

    /**
     * 解析Markdown内容并提取结构化信息
     */
    public Map<String, Object> parseMarkdownContent(String markdownContent) {
        logger.info("解析Markdown内容，长度: {}", markdownContent != null ? markdownContent.length() : 0);

        Map<String, Object> result = new HashMap<>();

        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            return result;
        }

        // 提取标题结构
        result.put("headings", extractHeadings(markdownContent));

        // 提取代码块
        result.put("codeBlocks", extractCodeBlocks(markdownContent));

        // 提取链接
        result.put("links", extractLinks(markdownContent));

        // 提取图片
        result.put("images", extractImages(markdownContent));

        // 提取列表项
        result.put("lists", extractLists(markdownContent));

        // 提取表格
        result.put("tables", extractTables(markdownContent));

        // 提取重点内容（粗体和斜体）
        result.put("emphasis", extractEmphasis(markdownContent));

        // 生成摘要
        result.put("summary", generateSummary(markdownContent));

        // 提取关键词
        result.put("keywords", extractKeywords(markdownContent));

        // 估计阅读时间
        result.put("readingTime", estimateReadingTime(markdownContent));

        logger.info("Markdown内容解析完成");
        return result;
    }

    /**
     * 提取标题结构
     */
    private java.util.List<Map<String, Object>> extractHeadings(String content) {
        java.util.List<Map<String, Object>> headings = new java.util.ArrayList<>();

        // 匹配Markdown标题 # ## ### 等
        Pattern headingPattern = Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);
        Matcher matcher = headingPattern.matcher(content);

        while (matcher.find()) {
            Map<String, Object> heading = new HashMap<>();
            heading.put("level", matcher.group(1).length());
            heading.put("text", matcher.group(2).trim());
            heading.put("position", matcher.start());
            headings.add(heading);
        }

        return headings;
    }

    /**
     * 提取代码块
     */
    private java.util.List<Map<String, Object>> extractCodeBlocks(String content) {
        java.util.List<Map<String, Object>> codeBlocks = new java.util.ArrayList<>();

        // 匹配代码块 ```language code ```
        Pattern codeBlockPattern = Pattern.compile("```([a-zA-Z+]*)\\n([\\s\\S]*?)```");
        Matcher matcher = codeBlockPattern.matcher(content);

        while (matcher.find()) {
            Map<String, Object> codeBlock = new HashMap<>();
            codeBlock.put("language", matcher.group(1).trim());
            codeBlock.put("code", matcher.group(2).trim());
            codeBlock.put("position", matcher.start());
            codeBlocks.add(codeBlock);
        }

        return codeBlocks;
    }

    /**
     * 提取链接
     */
    private java.util.List<Map<String, Object>> extractLinks(String content) {
        java.util.List<Map<String, Object>> links = new java.util.ArrayList<>();

        // 匹配链接 [text](url)
        Pattern linkPattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
        Matcher matcher = linkPattern.matcher(content);

        while (matcher.find()) {
            Map<String, Object> link = new HashMap<>();
            link.put("text", matcher.group(1));
            link.put("url", matcher.group(2));
            link.put("position", matcher.start());
            links.add(link);
        }

        return links;
    }

    /**
     * 提取图片
     */
    private java.util.List<Map<String, Object>> extractImages(String content) {
        java.util.List<Map<String, Object>> images = new java.util.ArrayList<>();

        // 匹配图片 ![alt](src)
        Pattern imagePattern = Pattern.compile("!\\[([^\\]]*)\\]\\(([^)]+)\\)");
        Matcher matcher = imagePattern.matcher(content);

        while (matcher.find()) {
            Map<String, Object> image = new HashMap<>();
            image.put("alt", matcher.group(1));
            image.put("src", matcher.group(2));
            image.put("position", matcher.start());
            images.add(image);
        }

        return images;
    }

    /**
     * 提取列表项
     */
    private java.util.List<Map<String, Object>> extractLists(String content) {
        java.util.List<Map<String, Object>> lists = new java.util.ArrayList<>();

        // 匹配无序列表
        Pattern ulPattern = Pattern.compile("^\\*\\s+(.+)$", Pattern.MULTILINE);
        Matcher ulMatcher = ulPattern.matcher(content);

        while (ulMatcher.find()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "unordered");
            item.put("content", ulMatcher.group(1).trim());
            item.put("position", ulMatcher.start());
            lists.add(item);
        }

        // 匹配有序列表
        Pattern olPattern = Pattern.compile("^\\d+\\.\\s+(.+)$", Pattern.MULTILINE);
        Matcher olMatcher = olPattern.matcher(content);

        while (olMatcher.find()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "ordered");
            item.put("content", olMatcher.group(1).trim());
            item.put("position", olMatcher.start());
            lists.add(item);
        }

        return lists;
    }

    /**
     * 提取表格
     */
    private java.util.List<Map<String, Object>> extractTables(String content) {
        java.util.List<Map<String, Object>> tables = new java.util.ArrayList<>();

        // 简化的表格匹配
        Pattern tablePattern = Pattern.compile("\\|(.+)\\|\\s*\\n\\|\\s*[-:\\|\\s]+\\|\\s*\\n((\\|.+\\|\\s*\\n)+)");
        Matcher matcher = tablePattern.matcher(content);

        while (matcher.find()) {
            Map<String, Object> table = new HashMap<>();
            String headerRow = matcher.group(1);
            String bodyRows = matcher.group(2);

            // 解析表头
            String[] headers = headerRow.split("\\|");
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
            }
            table.put("headers", headers);

            // 解析表格内容
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            String[] bodyLines = bodyRows.trim().split("\\n");
            for (String line : bodyLines) {
                if (line.trim().startsWith("|") && line.trim().endsWith("|")) {
                    String[] cells = line.substring(1, line.length() - 1).split("\\|");
                    for (int i = 0; i < cells.length; i++) {
                        cells[i] = cells[i].trim();
                    }
                    rows.add(cells);
                }
            }
            table.put("rows", rows);
            table.put("position", matcher.start());
            tables.add(table);
        }

        return tables;
    }

    /**
     * 提取重点内容（粗体和斜体）
     */
    private java.util.List<Map<String, Object>> extractEmphasis(String content) {
        java.util.List<Map<String, Object>> emphasis = new java.util.ArrayList<>();

        // 匹配粗体 **text** 和 __text__
        Pattern boldPattern = Pattern.compile("(\\*\\*|__)(.+?)\\1");
        Matcher boldMatcher = boldPattern.matcher(content);

        while (boldMatcher.find()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "bold");
            item.put("content", boldMatcher.group(2));
            item.put("position", boldMatcher.start());
            emphasis.add(item);
        }

        // 匹配斜体 *text* 和 _text_
        Pattern italicPattern = Pattern.compile("(\\*|_)(.+?)\\1");
        Matcher italicMatcher = italicPattern.matcher(content);

        while (italicMatcher.find()) {
            // 跳过已经匹配的粗体
            boolean alreadyMatched = false;
            for (Map<String, Object> boldItem : emphasis) {
                if (italicMatcher.start() == (Integer) boldItem.get("position")) {
                    alreadyMatched = true;
                    break;
                }
            }

            if (!alreadyMatched) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "italic");
                item.put("content", italicMatcher.group(2));
                item.put("position", italicMatcher.start());
                emphasis.add(item);
            }
        }

        return emphasis;
    }

    /**
     * 生成内容摘要
     */
    private String generateSummary(String content) {
        // 移除Markdown标记
        String plainText = content.replaceAll("[#*`\\[\\]()]|\\*\\*|__|`|```", "");

        // 按句子分割
        String[] sentences = plainText.split("[。！？.!?]");

        // 取前3句话作为摘要
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < Math.min(3, sentences.length); i++) {
            String sentence = sentences[i].trim();
            if (!sentence.isEmpty()) {
                if (summary.length() > 0) {
                    summary.append("。");
                }
                summary.append(sentence);
            }
        }

        // 限制摘要长度
        String result = summary.toString();
        if (result.length() > 200) {
            result = result.substring(0, 200) + "...";
        }

        return result;
    }

    /**
     * 提取关键词
     */
    private java.util.List<String> extractKeywords(String content) {
        // 移除Markdown标记和代码块
        String cleanContent = content.replaceAll("```[\\s\\S]*?```", "")
                .replaceAll("[#*`\\[\\]()]|\\*\\*|__|`", "")
                .toLowerCase();

        // 简单的关键词提取
        java.util.List<String> keywords = new java.util.ArrayList<>();
        String[] words = cleanContent.split("\\s+");

        // 过滤常见停用词
        String[] stopWords = {"的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by"};

        java.util.Set<String> stopWordSet = new java.util.HashSet<>(java.util.Arrays.asList(stopWords));
        java.util.Map<String, Integer> wordCount = new HashMap<>();

        for (String word : words) {
            word = word.trim();
            if (word.length() > 2 && !stopWordSet.contains(word)) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        // 按频率排序并返回前10个关键词
        wordCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .forEach(entry -> keywords.add(entry.getKey()));

        return keywords;
    }

    /**
     * 估计阅读时间（分钟）
     */
    private int estimateReadingTime(String content) {
        // 移除Markdown标记和代码块
        String cleanContent = content.replaceAll("```[\\s\\S]*?```", "")
                .replaceAll("[#*`\\[\\]()]|\\*\\*|__|`", "");

        // 计算字数
        int wordCount = cleanContent.split("\\s+").length;

        // 假设每分钟阅读300字
        return Math.max(1, (int) Math.ceil(wordCount / 300.0));
    }

    /**
     * 将Markdown转换为HTML（简化版本）
     */
    public String markdownToHtml(String markdownContent) {
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            return "";
        }

        String html = markdownContent;

        // 转换标题
        html = html.replaceAll("^#\\s+(.+)$", "<h1>$1</h1>");
        html = html.replaceAll("^##\\s+(.+)$", "<h2>$1</h2>");
        html = html.replaceAll("^###\\s+(.+)$", "<h3>$1</h3>");
        html = html.replaceAll("^####\\s+(.+)$", "<h4>$1</h4>");
        html = html.replaceAll("^#####\\s+(.+)$", "<h5>$1</h5>");
        html = html.replaceAll("^######\\s+(.+)$", "<h6>$1</h6>");

        // 转换粗体
        html = html.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>");
        html = html.replaceAll("__(.+?)__", "<strong>$1</strong>");

        // 转换斜体
        html = html.replaceAll("\\*(.+?)\\*", "<em>$1</em>");
        html = html.replaceAll("_(.+?)_", "<em>$1</em>");

        // 转换代码
        html = html.replaceAll("`(.+?)`", "<code>$1</code>");

        // 转换链接
        html = html.replaceAll("\\[([^\\]]+)\\]\\(([^)]+)\\)", "<a href=\"$2\">$1</a>");

        // 转换图片
        html = html.replaceAll("!\\[([^\\]]*)\\]\\(([^)]+)\\)", "<img src=\"$2\" alt=\"$1\">");

        // 转换换行
        html = html.replaceAll("\\n", "<br>");

        return html;
    }

    /**
     * 验证Markdown内容
     */
    public boolean validateMarkdownContent(String markdownContent) {
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            return false;
        }

        // 检查Markdown语法的基本正确性
        String[] lines = markdownContent.split("\n");
        int codeBlockDepth = 0;

        for (String line : lines) {
            // 检查代码块是否正确关闭
            if (line.trim().startsWith("```")) {
                codeBlockDepth++;
            }
        }

        // 代码块应该是成对的
        if (codeBlockDepth % 2 != 0) {
            return false;
        }

        return true;
    }

    /**
     * 处理项目Markdown内容
     */
    public Map<String, Object> processProjectMarkdown(Project project) {
        logger.info("处理项目 {} 的Markdown内容", project.getName());

        String markdownContent = project.getMarkdownContent();
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Object> processedContent = parseMarkdownContent(markdownContent);

        // 生成项目特定的处理结果
        processedContent.put("projectId", project.getId());
        processedContent.put("projectName", project.getName());
        processedContent.put("htmlContent", markdownToHtml(markdownContent));
        processedContent.put("isValid", validateMarkdownContent(markdownContent));

        return processedContent;
    }
}