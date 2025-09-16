package com.cvagent.service;

import com.cvagent.model.OptimizationRule;
import com.cvagent.repository.OptimizationRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 规则效果评估服务
 * 评估优化规则的效果和用户反馈
 */
@Service
public class RuleEffectEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(RuleEffectEvaluationService.class);

    @Autowired
    private OptimizationRuleRepository ruleRepository;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private AiServiceManager aiServiceManager;

    // 缓存规则效果数据
    private final Map<String, RuleEffectStats> ruleEffectCache = new ConcurrentHashMap<>();

    // 用户反馈记录
    private final Map<String, List<UserFeedback>> userFeedbackMap = new ConcurrentHashMap<>();

    /**
     * 评估规则效果
     */
    public RuleEffectEvaluation evaluateRuleEffect(String ruleId, String originalContent, String optimizedContent) {
        logger.info("评估规则效果: {}", ruleId);

        RuleEffectEvaluation evaluation = new RuleEffectEvaluation();
        evaluation.setRuleId(ruleId);
        evaluation.setEvaluatedAt(LocalDateTime.now());

        try {
            // 1. 计算基础指标
            evaluation.setContentLengthChange(calculateContentLengthChange(originalContent, optimizedContent));
            evaluation.setKeywordImprovement(calculateKeywordImprovement(originalContent, optimizedContent));
            evaluation.setReadabilityScore(calculateReadabilityScore(optimizedContent));

            // 2. AI评估
            String aiAssessment = performAIAssessment(originalContent, optimizedContent);
            evaluation.setAiAssessment(aiAssessment);
            evaluation.setAiScore(extractAIScore(aiAssessment));

            // 3. 更新统计信息
            updateRuleStats(ruleId, evaluation);

            logger.info("规则效果评估完成: {}, 评分: {}", ruleId, evaluation.getAiScore());
            return evaluation;

        } catch (Exception e) {
            logger.error("评估规则效果失败: {}", ruleId, e);
            evaluation.setError("评估失败: " + e.getMessage());
            return evaluation;
        }
    }

    /**
     * 记录用户反馈
     */
    public void recordUserFeedback(String ruleId, UserFeedback feedback) {
        logger.info("记录用户反馈: {}, 类型: {}", ruleId, feedback.getFeedbackType());

        userFeedbackMap.computeIfAbsent(ruleId, k -> new ArrayList<>()).add(feedback);

        // 更新规则效果统计
        updateRuleStatsWithFeedback(ruleId, feedback);
    }

    /**
     * 获取规则效果报告
     */
    @Cacheable(value = "ruleEffectReport", key = "#ruleId")
    public RuleEffectReport getRuleEffectReport(String ruleId) {
        logger.info("生成规则效果报告: {}", ruleId);

        RuleEffectReport report = new RuleEffectReport();
        report.setRuleId(ruleId);
        report.setGeneratedAt(LocalDateTime.now());

        try {
            // 获取规则基本信息
            OptimizationRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("规则不存在: " + ruleId));

            report.setRuleName(rule.getName());
            report.setRuleCategory(rule.getCategory());

            // 获取效果统计
            RuleEffectStats stats = ruleEffectCache.getOrDefault(ruleId, new RuleEffectStats());
            report.setEffectStats(stats);

            // 获取用户反馈统计
            List<UserFeedback> feedbacks = userFeedbackMap.getOrDefault(ruleId, Collections.emptyList());
            report.setFeedbackStats(calculateFeedbackStats(feedbacks));

            // 计算综合评分
            report.setOverallScore(calculateOverallScore(stats, report.getFeedbackStats()));

            logger.info("规则效果报告生成完成: {}", ruleId);
            return report;

        } catch (Exception e) {
            logger.error("生成规则效果报告失败: {}", ruleId, e);
            report.setError("报告生成失败: " + e.getMessage());
            return report;
        }
    }

    /**
     * 获取所有规则的效果排名
     */
    public List<RuleRanking> getRuleRankings() {
        logger.info("生成规则效果排名");

        List<RuleRanking> rankings = new ArrayList<>();

        for (String ruleId : ruleEffectCache.keySet()) {
            try {
                RuleEffectReport report = getRuleEffectReport(ruleId);
                RuleRanking ranking = new RuleRanking();
                ranking.setRuleId(ruleId);
                ranking.setRuleName(report.getRuleName());
                ranking.setOverallScore(report.getOverallScore());
                ranking.setUsageCount(report.getEffectStats().getUsageCount());
                ranking.setPositiveRate(report.getFeedbackStats().getPositiveRate());
                rankings.add(ranking);
            } catch (Exception e) {
                logger.warn("生成规则排名失败，跳过规则: {}", ruleId, e);
            }
        }

        // 按综合评分排序
        rankings.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        return rankings;
    }

    /**
     * AI评估规则效果
     */
    private String performAIAssessment(String originalContent, String optimizedContent) {
        try {
            String prompt = String.format(
                "请评估以下简历优化效果，提供详细分析和改进建议：\n\n" +
                "【原始内容】：\\n%s\\n\\n【优化后内容】：\\n%s\\n\\n" +
                "请从以下方面进行评估：\n" +
                "1. 内容改进程度 (1-10分)\n" +
                "2. 关键词优化效果 (1-10分)\n" +
                "3. 语言表达提升 (1-10分)\n" +
                "4. 专业性提升 (1-10分)\n" +
                "5. 整体优化效果 (1-10分)\n\n" +
                "请给出具体评分和详细改进建议。",
                originalContent, optimizedContent
            );

            return aiServiceManager.improveResumeSection(prompt, "rule_evaluation");
        } catch (Exception e) {
            logger.warn("AI评估失败: {}", e.getMessage());
            return "AI评估暂时不可用，建议进行人工评估";
        }
    }

    /**
     * 提取AI评分
     */
    private double extractAIScore(String aiAssessment) {
        try {
            // 简单的评分提取逻辑
            String[] lines = aiAssessment.split("\\n");
            for (String line : lines) {
                if (line.contains("整体优化效果") || line.contains("Overall")) {
                    // 提取评分数字
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)分?");
                    java.util.regex.Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        return Double.parseDouble(matcher.group(1));
                    }
                }
            }
            return 7.0; // 默认评分
        } catch (Exception e) {
            logger.warn("提取AI评分失败: {}", e.getMessage());
            return 7.0;
        }
    }

    /**
     * 计算内容长度变化
     */
    private double calculateContentLengthChange(String original, String optimized) {
        int originalLength = original.length();
        int optimizedLength = optimized.length();

        if (originalLength == 0) return 0.0;
        return ((double) (optimizedLength - originalLength) / originalLength) * 100;
    }

    /**
     * 计算关键词改进
     */
    private double calculateKeywordImprovement(String original, String optimized) {
        // 常见关键词列表
        Set<String> keywords = Set.of("java", "python", "javascript", "react", "spring",
                                     "leadership", "management", "teamwork", "communication");

        long originalCount = keywords.stream().filter(k -> original.toLowerCase().contains(k)).count();
        long optimizedCount = keywords.stream().filter(k -> optimized.toLowerCase().contains(k)).count();

        return optimizedCount - originalCount;
    }

    /**
     * 计算可读性评分
     */
    private double calculateReadabilityScore(String content) {
        // 简化的可读性计算
        double avgSentenceLength = content.split("[.!?]+").length / (double) content.split("\\s+").length;
        return Math.max(0, Math.min(10, 10 - avgSentenceLength * 50));
    }

    /**
     * 更新规则统计
     */
    private void updateRuleStats(String ruleId, RuleEffectEvaluation evaluation) {
        RuleEffectStats stats = ruleEffectCache.computeIfAbsent(ruleId, k -> new RuleEffectStats());
        stats.incrementUsage();
        stats.addScore(evaluation.getAiScore());
        stats.addContentChange(evaluation.getContentLengthChange());
    }

    /**
     * 更新用户反馈统计
     */
    private void updateRuleStatsWithFeedback(String ruleId, UserFeedback feedback) {
        RuleEffectStats stats = ruleEffectCache.computeIfAbsent(ruleId, k -> new RuleEffectStats());

        if (feedback.getFeedbackType() == FeedbackType.POSITIVE) {
            stats.incrementPositiveFeedback();
        } else if (feedback.getFeedbackType() == FeedbackType.NEGATIVE) {
            stats.incrementNegativeFeedback();
        }
    }

    /**
     * 计算反馈统计
     */
    private FeedbackStats calculateFeedbackStats(List<UserFeedback> feedbacks) {
        FeedbackStats stats = new FeedbackStats();
        stats.setTotalCount(feedbacks.size());

        long positiveCount = feedbacks.stream()
            .filter(f -> f.getFeedbackType() == FeedbackType.POSITIVE)
            .count();

        stats.setPositiveCount((int) positiveCount);
        stats.setPositiveRate(stats.getTotalCount() > 0 ?
            (double) positiveCount / stats.getTotalCount() : 0.0);

        return stats;
    }

    /**
     * 计算综合评分
     */
    private double calculateOverallScore(RuleEffectStats stats, FeedbackStats feedbackStats) {
        double aiScore = stats.getAverageScore();
        double feedbackScore = feedbackStats.getPositiveRate() * 10;
        double usageWeight = Math.min(1.0, stats.getUsageCount() / 100.0);

        return (aiScore * 0.6 + feedbackScore * 0.4) * usageWeight;
    }

    // 内部类定义
    public static class RuleEffectEvaluation {
        private String ruleId;
        private double contentLengthChange;
        private double keywordImprovement;
        private double readabilityScore;
        private String aiAssessment;
        private double aiScore;
        private LocalDateTime evaluatedAt;
        private String error;

        // Getters and Setters
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public double getContentLengthChange() { return contentLengthChange; }
        public void setContentLengthChange(double contentLengthChange) { this.contentLengthChange = contentLengthChange; }
        public double getKeywordImprovement() { return keywordImprovement; }
        public void setKeywordImprovement(double keywordImprovement) { this.keywordImprovement = keywordImprovement; }
        public double getReadabilityScore() { return readabilityScore; }
        public void setReadabilityScore(double readabilityScore) { this.readabilityScore = readabilityScore; }
        public String getAiAssessment() { return aiAssessment; }
        public void setAiAssessment(String aiAssessment) { this.aiAssessment = aiAssessment; }
        public double getAiScore() { return aiScore; }
        public void setAiScore(double aiScore) { this.aiScore = aiScore; }
        public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
        public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class RuleEffectStats {
        private final AtomicInteger usageCount = new AtomicInteger(0);
        private final AtomicInteger positiveFeedbackCount = new AtomicInteger(0);
        private final AtomicInteger negativeFeedbackCount = new AtomicInteger(0);
        private final AtomicLong totalScore = new AtomicLong(0);
        private final AtomicLong totalContentChange = new AtomicLong(0);

        public void incrementUsage() { usageCount.incrementAndGet(); }
        public void incrementPositiveFeedback() { positiveFeedbackCount.incrementAndGet(); }
        public void incrementNegativeFeedback() { negativeFeedbackCount.incrementAndGet(); }
        public void addScore(double score) { totalScore.addAndGet((long) (score * 100)); }
        public void addContentChange(double change) { totalContentChange.addAndGet((long) (change * 100)); }

        public int getUsageCount() { return usageCount.get(); }
        public int getPositiveFeedbackCount() { return positiveFeedbackCount.get(); }
        public int getNegativeFeedbackCount() { return negativeFeedbackCount.get(); }
        public double getAverageScore() {
            int count = usageCount.get();
            return count > 0 ? totalScore.get() / (double) (count * 100) : 0.0;
        }
        public double getAverageContentChange() {
            int count = usageCount.get();
            return count > 0 ? totalContentChange.get() / (double) (count * 100) : 0.0;
        }
    }

    public static class RuleEffectReport {
        private String ruleId;
        private String ruleName;
        private String ruleCategory;
        private RuleEffectStats effectStats;
        private FeedbackStats feedbackStats;
        private double overallScore;
        private LocalDateTime generatedAt;
        private String error;

        // Getters and Setters
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getRuleCategory() { return ruleCategory; }
        public void setRuleCategory(String ruleCategory) { this.ruleCategory = ruleCategory; }
        public RuleEffectStats getEffectStats() { return effectStats; }
        public void setEffectStats(RuleEffectStats effectStats) { this.effectStats = effectStats; }
        public FeedbackStats getFeedbackStats() { return feedbackStats; }
        public void setFeedbackStats(FeedbackStats feedbackStats) { this.feedbackStats = feedbackStats; }
        public double getOverallScore() { return overallScore; }
        public void setOverallScore(double overallScore) { this.overallScore = overallScore; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public static class FeedbackStats {
        private int totalCount;
        private int positiveCount;
        private double positiveRate;

        // Getters and Setters
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public int getPositiveCount() { return positiveCount; }
        public void setPositiveCount(int positiveCount) { this.positiveCount = positiveCount; }
        public double getPositiveRate() { return positiveRate; }
        public void setPositiveRate(double positiveRate) { this.positiveRate = positiveRate; }
    }

    public static class RuleRanking {
        private String ruleId;
        private String ruleName;
        private double overallScore;
        private int usageCount;
        private double positiveRate;

        // Getters and Setters
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public double getOverallScore() { return overallScore; }
        public void setOverallScore(double overallScore) { this.overallScore = overallScore; }
        public int getUsageCount() { return usageCount; }
        public void setUsageCount(int usageCount) { this.usageCount = usageCount; }
        public double getPositiveRate() { return positiveRate; }
        public void setPositiveRate(double positiveRate) { this.positiveRate = positiveRate; }
    }

    public static class UserFeedback {
        private String ruleId;
        private FeedbackType feedbackType;
        private String comment;
        private LocalDateTime feedbackTime;
        private String userId;

        // Getters and Setters
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public FeedbackType getFeedbackType() { return feedbackType; }
        public void setFeedbackType(FeedbackType feedbackType) { this.feedbackType = feedbackType; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public LocalDateTime getFeedbackTime() { return feedbackTime; }
        public void setFeedbackTime(LocalDateTime feedbackTime) { this.feedbackTime = feedbackTime; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    public enum FeedbackType {
        POSITIVE, NEGATIVE, NEUTRAL
    }
}