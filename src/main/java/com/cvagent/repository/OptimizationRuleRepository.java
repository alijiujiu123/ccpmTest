package com.cvagent.repository;

import com.cvagent.model.OptimizationRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptimizationRuleRepository extends MongoRepository<OptimizationRule, String> {

    // 查找所有活跃的规则
    List<OptimizationRule> findByIsActiveTrue();

    // 根据类别查找规则
    List<OptimizationRule> findByCategoryAndIsActiveTrue(String category);

    // 根据目标区域查找规则
    List<OptimizationRule> findByTargetSectionAndIsActiveTrue(String targetSection);

    // 根据优先级查找规则
    List<OptimizationRule> findByIsActiveTrueOrderByPriorityDesc();

    // 查找特定区域的规则，按优先级排序
    List<OptimizationRule> findByTargetSectionAndIsActiveTrueOrderByPriorityDesc(String targetSection);

    // 根据名称搜索规则
    @Query("{ 'name': { '$regex': ?0, '$options': 'i' } }")
    List<OptimizationRule> searchByName(String name);

    // 查找所有规则类别
    @Query(value = "{}", fields = "{ 'category': 1 }")
    List<OptimizationRule> findAllCategories();

    // 查找所有规则ID
    @Query(value = "{}", fields = "{ 'id': 1 }")
    List<String> findAllRuleIds();

    // 激活或停用规则
    @Query("{ '_id': ?1 }")
    @Update("{ '$set': { 'isActive': ?0 } }")
    void updateIsActiveById(Boolean isActive, String id);

    // 删除特定类别的所有规则
    void deleteByCategory(String category);
}