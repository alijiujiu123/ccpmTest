package com.cvagent.repository;

import com.cvagent.model.AiUsageLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI使用日志Repository
 */
@Repository
public interface AiUsageLogRepository extends MongoRepository<AiUsageLog, String> {

    /**
     * 根据服务类型查找日志
     */
    List<AiUsageLog> findByServiceType(String serviceType);

    /**
     * 查找指定时间之后的日志
     */
    List<AiUsageLog> findByRequestTimeAfter(LocalDateTime requestTime);

    /**
     * 查找指定时间之前的日志
     */
    List<AiUsageLog> findByRequestTimeBefore(LocalDateTime requestTime);

    /**
     * 查找指定时间范围内的日志
     */
    List<AiUsageLog> findByRequestTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查找失败的日志
     */
    List<AiUsageLog> findBySuccessFalse();

    /**
     * 根据用户ID查找日志
     */
    List<AiUsageLog> findByUserId(String userId);

    /**
     * 根据服务类型统计成功请求数
     */
    @Query(value = "{ 'serviceType': ?0, 'success': true }", count = true)
    long countSuccessfulRequestsByServiceType(String serviceType);

    /**
     * 根据服务类型统计失败请求数
     */
    @Query(value = "{ 'serviceType': ?0, 'success': false }", count = true)
    long countFailedRequestsByServiceType(String serviceType);

    /**
     * 计算平均响应时间
     */
    @Query(value = "{ 'serviceType': ?0 }", fields = "{ 'responseTime': 1 }")
    List<AiUsageLog> findResponseTimesByServiceType(String serviceType);

    /**
     * 查找用户的最近使用记录
     */
    List<AiUsageLog> findTop10ByUserIdOrderByRequestTimeDesc(String userId);

    /**
     * 删除指定时间之前的日志
     */
    void deleteByRequestTimeBefore(LocalDateTime cutoffDate);

    /**
     * 统计每日使用量
     */
    @Query(value = "{ 'requestTime': { '$gte': ?0, '$lt': ?1 } }", count = true)
    long countByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}