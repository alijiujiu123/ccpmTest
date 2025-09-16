package com.cvagent.repository;

import com.cvagent.model.RuleVersion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则版本数据访问层
 */
@Repository
public interface RuleVersionRepository extends MongoRepository<RuleVersion, String> {

    /**
     * 根据规则ID查找所有版本
     */
    List<RuleVersion> findByRuleIdOrderByVersionDesc(String ruleId);

    /**
     * 根据规则ID和版本号查找特定版本
     */
    RuleVersion findByRuleIdAndVersion(String ruleId, Integer version);

    /**
     * 获取规则的最新版本
     */
    @Query(value = "{ 'ruleId': ?0 }", sort = "{ 'version': -1 }")
    RuleVersion findLatestVersionByRuleId(String ruleId);

    /**
     * 获取在特定时间有效的版本
     */
    @Query("{ 'ruleId': ?0, 'effectiveAt': { $lte: ?1 }, 'expiresAt': { $gt: ?1 } }")
    List<RuleVersion> findEffectiveVersionsAtTime(String ruleId, LocalDateTime time);

    /**
     * 获取已过期的版本
     */
    List<RuleVersion> findByExpiresAtBefore(LocalDateTime time);

    /**
     * 根据修改人查找版本
     */
    List<RuleVersion> findByChangedByOrderByCreatedAtDesc(String changedBy);

    /**
     * 获取规则版本历史
     */
    @Query(value = "{ 'ruleId': ?0 }", fields = "{ 'version': 1, 'changeReason': 1, 'changedBy': 1, 'createdAt': 1 }")
    List<RuleVersion> findVersionHistory(String ruleId);

    /**
     * 获取某个时间范围内修改的版本
     */
    List<RuleVersion> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取最大的版本号
     */
    @Query(value = "{ 'ruleId': ?0 }", fields = "{ 'version': 1 }", sort = "{ 'version': -1 }")
    RuleVersion findMaxVersion(String ruleId);

    /**
     * 删除指定规则的所有版本
     */
    void deleteByRuleId(String ruleId);

    /**
     * 检查版本是否存在
     */
    boolean existsByRuleIdAndVersion(String ruleId, Integer version);
}