package com.cvagent.repository;

import com.cvagent.model.ResumeTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 简历模板数据访问层
 */
@Repository
public interface ResumeTemplateRepository extends MongoRepository<ResumeTemplate, String> {

    /**
     * 查找所有活跃的模板
     */
    List<ResumeTemplate> findByIsActiveTrue();

    /**
     * 查找免费模板
     */
    List<ResumeTemplate> findByIsPremiumFalse();

    /**
     * 查找付费模板
     */
    List<ResumeTemplate> findByIsPremiumTrue();

    /**
     * 根据类别查找模板
     */
    List<ResumeTemplate> findByCategory(String category);

    /**
     * 根据风格查找模板
     */
    List<ResumeTemplate> findByStyle(String style);

    /**
     * 查找特定类别和风格的活跃模板
     */
    List<ResumeTemplate> findByCategoryAndStyleAndIsActiveTrue(String category, String style);

    /**
     * 查找使用次数最多的模板
     */
    List<ResumeTemplate> findTop10ByOrderByUsageCountDesc();

    /**
     * 查找最新创建的模板
     */
    List<ResumeTemplate> findTop10ByOrderByCreatedAtDesc();

    /**
     * 搜索模板
     */
    @Query("{ '$or': [ " +
          "{ 'name': { '$regex': ?0, '$options': 'i' } }, " +
          "{ 'description': { '$regex': ?0, '$options': 'i' } }, " +
          "{ 'category': { '$regex': ?0, '$options': 'i' } }, " +
          "{ 'style': { '$regex': ?0, '$options': 'i' } } " +
          "] }")
    List<ResumeTemplate> searchByKeyword(String keyword);

    /**
     * 根据创建者查找模板
     */
    List<ResumeTemplate> findByCreatedBy(String createdBy);

    /**
     * 更新模板使用次数
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'usageCount': 1 }, '$set': { 'updatedAt': new Date() } }")
    void incrementUsageCount(String id);

    /**
     * 激活或停用模板
     */
    @Query("{ '_id': ?0 }")
    @Update("{ '$set': { 'isActive': ?1, 'updatedAt': new Date() } }")
    void updateActiveStatus(String id, Boolean isActive);

    /**
     * 统计活跃模板数量
     */
    long countByIsActiveTrue();

    /**
     * 统计特定类别的模板数量
     */
    long countByCategory(String category);

    /**
     * 获取所有模板类别
     */
    @Query(value = "{}", fields = "{ 'category': 1 }")
    List<String> findAllCategories();

    /**
     * 获取所有模板风格
     */
    @Query(value = "{}", fields = "{ 'style': 1 }")
    List<String> findAllStyles();
}