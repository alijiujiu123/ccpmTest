package com.cvagent.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * AI使用日志实体
 * 记录AI服务的使用情况
 */
@Document(collection = "ai_usage_logs")
public class AiUsageLog {

    @Id
    private String id;

    // 服务类型
    private String serviceType;

    // 响应时间（毫秒）
    private long responseTime;

    // 是否成功
    private boolean success;

    // 错误信息
    private String errorMessage;

    // 请求时间
    private LocalDateTime requestTime;

    // 用户ID（可选）
    private String userId;

    // 令牌使用量（可选）
    private Integer tokensUsed;

    // 请求内容摘要（可选）
    private String requestSummary;

    // 响应内容摘要（可选）
    private String responseSummary;

    // 构造函数
    public AiUsageLog() {
        this.requestTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public String getRequestSummary() {
        return requestSummary;
    }

    public void setRequestSummary(String requestSummary) {
        this.requestSummary = requestSummary;
    }

    public String getResponseSummary() {
        return responseSummary;
    }

    public void setResponseSummary(String responseSummary) {
        this.responseSummary = responseSummary;
    }

    /**
     * 创建成功日志
     */
    public static AiUsageLog createSuccessLog(String serviceType, long responseTime) {
        AiUsageLog log = new AiUsageLog();
        log.setServiceType(serviceType);
        log.setResponseTime(responseTime);
        log.setSuccess(true);
        return log;
    }

    /**
     * 创建错误日志
     */
    public static AiUsageLog createErrorLog(String serviceType, long responseTime, String errorMessage) {
        AiUsageLog log = new AiUsageLog();
        log.setServiceType(serviceType);
        log.setResponseTime(responseTime);
        log.setSuccess(false);
        log.setErrorMessage(errorMessage);
        return log;
    }
}