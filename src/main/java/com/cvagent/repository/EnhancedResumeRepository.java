package com.cvagent.repository;

import com.cvagent.model.EnhancedResume;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 增强版简历数据访问层
 */
@Repository
public interface EnhancedResumeRepository extends MongoRepository<EnhancedResume, String> {

    /**
     * 根据用户ID查找简历
     */
    List<EnhancedResume> findByUserId(String userId);

    /**
     * 根据基础简历ID查找生成的简历
     */
    List<EnhancedResume> findByBaseResumeId(String baseResumeId);

    /**
     * 根据招聘需求ID查找简历
     */
    List<EnhancedResume> findByJobRequirementId(String jobRequirementId);

    /**
     * 查找用户的公开简历
     */
    List<EnhancedResume> findByUserIdAndIsPublicTrue(String userId);

    /**
     * 根据匹配分数排序
     */
    List<EnhancedResume> findByMatchScoreGreaterThanOrderByMatchScoreDesc(Double minScore);

    /**
     * 根据优化状态查找
     */
    List<EnhancedResume> findByOptimizationStatus(String optimizationStatus);

    /**
     * 根据生成方式查找
     */
    List<EnhancedResume> findByGeneratedBy(String generatedBy);

    /**
     * 查找最近生成的简历
     */
    List<EnhancedResume> findTop10ByOrderByGeneratedAtDesc();

    /**
     * 根据模板ID查找
     */
    List<EnhancedResume> findByTemplateId(String templateId);

    /**
     * 查找特定用户的特定版本简历
     */
    EnhancedResume findByUserIdAndVersion(String userId, String version);

    /**
     * 统计用户简历数量
     */
    long countByUserId(String userId);

    /**
     * 删除特定用户的所有简历
     */
    void deleteByUserId(String userId);

    /**
     * 根据标题搜索
     */
    @Query("{ 'title': { '$regex': ?0, '$options': 'i' } }")
    List<EnhancedResume> searchByTitle(String title);

    /**
     * 根据技能搜索
     */
    @Query("{ 'skills.technicalSkills': { '$in': [?0] } }")
    List<EnhancedResume> searchByTechnicalSkill(String skill);

    /**
     * 复杂搜索：根据多个条件搜索
     */
    @Query("{ '$and': [ " +
          "{ 'userId': ?0 }, " +
          "{ '$or': [ " +
          "  { 'title': { '$regex': ?1, '$options': 'i' } }, " +
          "  { 'summary': { '$regex': ?1, '$options': 'i' } }, " +
          "  { 'skills.technicalSkills': { '$in': [?1] } } " +
          "] } " +
          "] }")
    List<EnhancedResume> searchByUserAndKeyword(String userId, String keyword);

    /**
     * 获取用户的简历版本历史
     */
    @Query("{ 'userId': ?0 }")
    List<EnhancedResume> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 更新简历匹配分数
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'matchScore': ?1, 'updatedAt': new Date() } }")
    void updateMatchScore(String id, Double matchScore);

    /**
     * 更新优化状态
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'optimizationStatus': ?1, 'updatedAt': new Date() } }")
    void updateOptimizationStatus(String id, String optimizationStatus);
}