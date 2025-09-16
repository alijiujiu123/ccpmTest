package com.cvagent.repository;

import com.cvagent.model.CoverLetter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 求职信数据访问层
 */
@Repository
public interface CoverLetterRepository extends MongoRepository<CoverLetter, String> {

    /**
     * 根据用户ID查找求职信
     */
    List<CoverLetter> findByUserId(String userId);

    /**
     * 根据用户ID查找求职信（按创建时间倒序）
     */
    List<CoverLetter> findByUserIdOrderByGeneratedAtDesc(String userId);

    /**
     * 根据关联的简历ID查找求职信
     */
    List<CoverLetter> findByResumeId(String resumeId);

    /**
     * 根据招聘需求ID查找求职信
     */
    List<CoverLetter> findByJobRequirementId(String jobRequirementId);

    /**
     * 根据模板ID查找求职信
     */
    List<CoverLetter> findByTemplateId(String templateId);

    /**
     * 根据状态查找求职信
     */
    List<CoverLetter> findByStatus(String status);

    /**
     * 根据生成方式查找求职信
     */
    List<CoverLetter> findByGeneratedBy(String generatedBy);

    /**
     * 根据用户ID和生成方式查找求职信
     */
    List<CoverLetter> findByUserIdAndGeneratedByOrderByGeneratedAtDesc(String userId, String generatedBy);

    /**
     * 查找AI优化的求职信
     */
    List<CoverLetter> findByAiOptimizedTrue();

    /**
     * 根据用户ID查找AI优化的求职信
     */
    List<CoverLetter> findByUserIdAndAiOptimizedTrueOrderByGeneratedAtDesc(String userId);

    /**
     * 查找公开的求职信
     */
    List<CoverLetter> findByIsPublicTrue();

    /**
     * 查找可用的模板
     */
    List<CoverLetter> findByIsTemplateTrue();

    /**
     * 根据公司和职位查找求职信
     */
    List<CoverLetter> findByCompanyNameAndPosition(String companyName, String position);

    /**
     * 查找用户的草稿求职信
     */
    List<CoverLetter> findByUserIdAndStatusOrderByGeneratedAtDesc(String userId, String status);

    /**
     * 搜索求职信（按标题、公司名、职位）
     */
    @Query("{ '$and': [ { 'userId': ?0 }, { '$or': [ { 'title': { '$regex': ?1, '$options': 'i' } }, { 'companyName': { '$regex': ?1, '$options': 'i' } }, { 'position': { '$regex': ?1, '$options': 'i' } } ] } ] }")
    List<CoverLetter> searchByUserAndKeyword(String userId, String keyword);

    /**
     * 查找特定时间段内创建的求职信
     */
    List<CoverLetter> findByUserIdAndGeneratedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 查找匹配度高的求职信
     */
    List<CoverLetter> findByUserIdAndMatchScoreGreaterThanOrderByMatchScoreDesc(String userId, Double minScore);

    /**
     * 增加查看次数
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'viewCount': 1 }, '$set': { 'lastModifiedAt': new java.util.Date() } }")
    void incrementViewCount(String coverLetterId);

    /**
     * 增加下载次数
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'downloadCount': 1 }, '$set': { 'lastModifiedAt': new java.util.Date() } }")
    void incrementDownloadCount(String coverLetterId);

    /**
     * 更新求职信状态
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'status': ?1, 'lastModifiedAt': new java.util.Date() } }")
    void updateStatus(String coverLetterId, String status);

    /**
     * 更新优化状态
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'optimizationStatus': ?1, 'lastModifiedAt': new java.util.Date() } }")
    void updateOptimizationStatus(String coverLetterId, String optimizationStatus);

    /**
     * 设置AI优化标志
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'aiOptimized': true, 'optimizationStatus': 'completed', 'lastModifiedAt': new java.util.Date() } }")
    void markAsAIOptimized(String coverLetterId);

    /**
     * 更新评分
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'userRating': ?1, 'lastModifiedAt': new java.util.Date() } }")
    void updateUserRating(String coverLetterId, Double rating);

    /**
     * 更新质量分数
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'qualityScore': ?1, 'lastModifiedAt': new java.util.Date() } }")
    void updateQualityScore(String coverLetterId, Double qualityScore);

    /**
     * 更新相关性分数
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'relevanceScore': ?1, 'lastModifiedAt': new java.util.Date() } }")
    void updateRelevanceScore(String coverLetterId, Double relevanceScore);

    /**
     * 更新完整性分数
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'completenessScore': ?1, 'lastModifiedAt': new java.util.Date() } }")
    void updateCompletenessScore(String coverLetterId, Double completenessScore);

    /**
     * 查找最近修改的求职信
     */
    List<CoverLetter> findTop10ByUserIdOrderByLastModifiedAtDesc(String userId);

    /**
     * 统计用户的求职信数量
     */
    long countByUserId(String userId);

    /**
     * 统计用户的特定状态求职信数量
     */
    long countByUserIdAndStatus(String userId, String status);

    /**
     * 查找需要优化的求职信
     */
    List<CoverLetter> findByUserIdAndAiOptimizedFalseOrderByGeneratedAtDesc(String userId);

    /**
     * 查找高评分的求职信
     */
    List<CoverLetter> findByUserIdAndQualityScoreGreaterThanOrderByQualityScoreDesc(String userId, Double minScore);

    /**
     * 查找特定语言的求职信
     */
    List<CoverLetter> findByUserIdAndLanguageOrderByGeneratedAtDesc(String userId, String language);

    /**
     * 批量更新状态
     */
    @Query("{ '_id': { '$in': ?0 } }")
    @Update("{ '$set': { 'status': ?1, 'lastModifiedAt': new java.util.Date() } }")
    void batchUpdateStatus(List<String> coverLetterIds, String status);
}