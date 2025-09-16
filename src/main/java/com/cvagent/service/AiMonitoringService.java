package com.cvagent.service;

import com.cvagent.model.AiUsageLog;
import com.cvagent.repository.AiUsageLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI服务监控和统计
 * 监控AI服务的使用情况和性能指标
 */
@Service
public class AiMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(AiMonitoringService.class);

    @Autowired
    private AiUsageLogRepository aiUsageLogRepository;

    // 实时统计
    private final Map<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> totalResponseTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();

    // 启动时间
    private final LocalDateTime serviceStartTime = LocalDateTime.now();

    public AiMonitoringService() {
        // 初始化统计计数器
        initializeCounters();
    }

    /**
     * 初始化统计计数器
     */
    private void initializeCounters() {
        String[] services = {
                "chat", "resume_optimization", "cover_letter_generation",
                "resume_section_improvement", "project_description_generation",
                "skill_matching_analysis", "interview_questions_prediction"
        };

        for (String service : services) {
            requestCounts.put(service, new AtomicLong(0));
            totalResponseTime.put(service, new AtomicLong(0));
            errorCounts.put(service, new AtomicLong(0));
        }
    }

    /**
     * 记录AI请求
     */
    @Async
    public void recordRequest(String serviceType, long responseTime, boolean success, String errorMessage) {
        try {
            // 更新实时统计
            requestCounts.getOrDefault(serviceType, new AtomicLong(0)).incrementAndGet();
            totalResponseTime.getOrDefault(serviceType, new AtomicLong(0)).addAndGet(responseTime);

            if (!success) {
                errorCounts.getOrDefault(serviceType, new AtomicLong(0)).incrementAndGet();
            }

            // 记录到数据库
            AiUsageLog log = new AiUsageLog();
            log.setServiceType(serviceType);
            log.setResponseTime(responseTime);
            log.setSuccess(success);
            log.setErrorMessage(errorMessage);
            log.setRequestTime(LocalDateTime.now());

            aiUsageLogRepository.save(log);

            logger.debug("记录AI请求: {}, 耗时: {}ms, 成功: {}", serviceType, responseTime, success);
        } catch (Exception e) {
            logger.error("记录AI请求失败: {}", serviceType, e);
        }
    }

    /**
     * 记录提示词请求
     */
    @Async
    public void recordPromptRequest(String templateName, long responseTime, boolean success) {
        try {
            AiUsageLog log = new AiUsageLog();
            log.setServiceType("prompt_template_" + templateName);
            log.setResponseTime(responseTime);
            log.setSuccess(success);
            log.setRequestTime(LocalDateTime.now());

            aiUsageLogRepository.save(log);
        } catch (Exception e) {
            logger.error("记录提示词请求失败: {}", templateName, e);
        }
    }

    /**
     * 获取服务状态统计
     */
    public Map<String, Object> getServiceStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总体统计
        stats.put("totalRequests", requestCounts.values().stream().mapToLong(AtomicLong::get).sum());
        stats.put("totalErrors", errorCounts.values().stream().mapToLong(AtomicLong::get).sum());
        stats.put("serviceUptime", java.time.Duration.between(serviceStartTime, LocalDateTime.now()).getSeconds());

        // 各服务统计
        Map<String, Map<String, Object>> serviceStats = new HashMap<>();
        requestCounts.forEach((service, count) -> {
            Map<String, Object> serviceStat = new HashMap<>();
            serviceStat.put("requestCount", count.get());
            serviceStat.put("errorCount", errorCounts.getOrDefault(service, new AtomicLong(0)).get());

            long totalTime = totalResponseTime.getOrDefault(service, new AtomicLong(0)).get();
            long reqCount = count.get();
            serviceStat.put("averageResponseTime", reqCount > 0 ? totalTime / reqCount : 0);
            serviceStat.put("successRate", reqCount > 0 ?
                    (reqCount - errorCounts.getOrDefault(service, new AtomicLong(0)).get()) * 100.0 / reqCount : 0.0);

            serviceStats.put(service, serviceStat);
        });
        stats.put("serviceStats", serviceStats);

        return stats;
    }

    /**
     * 获取最近的请求日志
     */
    public List<AiUsageLog> getRecentLogs(int limit) {
        return aiUsageLogRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, limit, Sort.by("requestTime").descending())
        ).getContent();
    }

    /**
     * 获取错误日志
     */
    public List<AiUsageLog> getErrorLogs(int limit) {
        List<AiUsageLog> errorLogs = aiUsageLogRepository.findBySuccessFalse();
        return errorLogs.stream()
                .sorted((a, b) -> b.getRequestTime().compareTo(a.getRequestTime()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取服务健康状态
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();

        // 检查最近的成功率
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        List<AiUsageLog> recentLogs = aiUsageLogRepository.findByRequestTimeAfter(fiveMinutesAgo);

        if (recentLogs.isEmpty()) {
            health.put("status", "UNKNOWN");
            health.put("message", "最近5分钟没有请求");
            return health;
        }

        long recentTotal = recentLogs.size();
        long recentErrors = recentLogs.stream().filter(log -> !log.isSuccess()).count();
        double recentSuccessRate = (recentTotal - recentErrors) * 100.0 / recentTotal;

        health.put("status", recentSuccessRate >= 95.0 ? "HEALTHY" :
                          recentSuccessRate >= 80.0 ? "DEGRADED" : "UNHEALTHY");
        health.put("successRate", String.format("%.2f%%", recentSuccessRate));
        health.put("recentRequests", recentTotal);
        health.put("recentErrors", recentErrors);
        health.put("checkedAt", LocalDateTime.now());

        return health;
    }

    /**
     * 获取性能报告
     */
    public Map<String, Object> getPerformanceReport() {
        Map<String, Object> report = new HashMap<>();

        // 获取过去24小时的数据
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        List<AiUsageLog> dayLogs = aiUsageLogRepository.findByRequestTimeAfter(twentyFourHoursAgo);

        if (dayLogs.isEmpty()) {
            report.put("message", "过去24小时没有数据");
            return report;
        }

        // 计算性能指标
        long totalRequests = dayLogs.size();
        long totalErrors = dayLogs.stream().filter(log -> !log.isSuccess()).count();
        long totalTime = dayLogs.stream().mapToLong(AiUsageLog::getResponseTime).sum();
        double averageResponseTime = totalTime / (double) totalRequests;
        double successRate = (totalRequests - totalErrors) * 100.0 / totalRequests;

        report.put("totalRequests", totalRequests);
        report.put("totalErrors", totalErrors);
        report.put("averageResponseTime", String.format("%.2fms", averageResponseTime));
        report.put("successRate", String.format("%.2f%%", successRate));
        report.put("reportPeriod", "过去24小时");
        report.put("generatedAt", LocalDateTime.now());

        // 按小时统计请求量
        Map<Integer, Long> hourlyRequests = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            hourlyRequests.put(i, 0L);
        }

        dayLogs.forEach(log -> {
            int hour = log.getRequestTime().getHour();
            hourlyRequests.put(hour, hourlyRequests.get(hour) + 1);
        });

        report.put("hourlyRequests", hourlyRequests);

        return report;
    }

    /**
     * 清理旧日志
     */
    @Async
    public void cleanupOldLogs(int daysToKeep) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            List<AiUsageLog> oldLogs = aiUsageLogRepository.findByRequestTimeBefore(cutoffDate);

            if (!oldLogs.isEmpty()) {
                aiUsageLogRepository.deleteAll(oldLogs);
                logger.info("清理了 {} 条旧日志，保留 {} 天", oldLogs.size(), daysToKeep);
            }
        } catch (Exception e) {
            logger.error("清理旧日志失败", e);
        }
    }

    /**
     * 重置统计数据
     */
    public void resetStatistics() {
        requestCounts.forEach((service, counter) -> counter.set(0));
        totalResponseTime.forEach((service, counter) -> counter.set(0));
        errorCounts.forEach((service, counter) -> counter.set(0));

        logger.info("AI服务统计数据已重置");
    }

    /**
     * 获取服务排行榜
     */
    public List<Map<String, Object>> getServiceRanking() {
        return requestCounts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> ranking = new HashMap<>();
                    String service = entry.getKey();
                    long count = entry.getValue().get();
                    long errors = errorCounts.getOrDefault(service, new AtomicLong(0)).get();
                    long totalTime = totalResponseTime.getOrDefault(service, new AtomicLong(0)).get();

                    ranking.put("service", service);
                    ranking.put("requestCount", count);
                    ranking.put("errorCount", errors);
                    ranking.put("averageResponseTime", count > 0 ? totalTime / count : 0);
                    ranking.put("successRate", count > 0 ? (count - errors) * 100.0 / count : 0.0);

                    return ranking;
                })
                .sorted((a, b) -> ((Long) b.get("requestCount")).compareTo((Long) a.get("requestCount")))
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
    }
}