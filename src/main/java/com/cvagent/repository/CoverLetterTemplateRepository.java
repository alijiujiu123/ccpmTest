package com.cvagent.repository;

import com.cvagent.model.CoverLetterTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 求职信模板数据访问层
 */
@Repository
public interface CoverLetterTemplateRepository extends MongoRepository<CoverLetterTemplate, String> {

    /**
     * 根据分类查找模板
     */
    List<CoverLetterTemplate> findByCategory(String category);

    /**
     * 根据风格查找模板
     */
    List<CoverLetterTemplate> findByStyle(String style);

    /**
     * 查找活跃的模板
     */
    List<CoverLetterTemplate> findByIsActiveTrue();

    /**
     * 根据创建者查找模板
     */
    List<CoverLetterTemplate> findByCreatedBy(String createdBy);

    /**
     * 根据语言配置查找模板
     */
    List<CoverLetterTemplate> findByLanguageConfigLanguage(String language);

    /**
     * 查找最受欢迎的模板（按使用次数排序）
     */
    List<CoverLetterTemplate> findByIsActiveTrueOrderByUsageCountDesc();

    /**
     * 查找评分最高的模板
     */
    List<CoverLetterTemplate> findByIsActiveTrueOrderByAverageRatingDesc();

    /**
     * 根据分类和风格查找模板
     */
    List<CoverLetterTemplate> findByCategoryAndStyleAndIsActiveTrue(String category, String style);

    /**
     * 搜索模板（按名称和描述）
     */
    @Query("{ '$or': [ { 'name': { '$regex': ?0, '$options': 'i' } }, { 'description': { '$regex': ?0, '$options': 'i' } } ], 'isActive': true }")
    List<CoverLetterTemplate> searchByKeyword(String keyword);

    /**
     * 增加模板使用次数
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'usageCount': 1 }, '$set': { 'updatedAt': new java.util.Date() } }")
    void incrementUsageCount(String templateId);

    /**
     * 更新模板评分
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'averageRating': ?1, 'updatedAt': new java.util.Date() } }")
    void updateRating(String templateId, Double newRating);

    /**
     * 查找用户的模板
     */
    List<CoverLetterTemplate> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(String createdBy);

    /**
     * 查找特定语言和风格的模板
     */
    List<CoverLetterTemplate> findByLanguageConfigLanguageAndStyle(String language, String style);

    /**
     * 查找支持AI优化的模板
     */
    List<CoverLetterTemplate> findByLanguageConfigAiOptimizedTrue();

    /**
     * 查找特定语调的模板
     */
    List<CoverLetterTemplate> findByLanguageConfigTone(String tone);

    /**
     * 统计活跃模板数量
     */
    long countByIsActiveTrue();

    /**
     * 查找最近更新的模板
     */
    List<CoverLetterTemplate> findTop10ByIsActiveTrueOrderByUpdatedAtDesc();
}