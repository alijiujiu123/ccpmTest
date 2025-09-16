package com.cvagent.repository;

import com.cvagent.model.JobRequirement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 招聘需求数据访问层
 */
@Repository
public interface JobRequirementRepository extends MongoRepository<JobRequirement, String> {

    /**
     * 根据标题搜索招聘需求
     */
    List<JobRequirement> findByTitleContainingIgnoreCase(String title);

    /**
     * 根据公司搜索招聘需求
     */
    List<JobRequirement> findByCompanyContainingIgnoreCase(String company);

    /**
     * 根据技能搜索招聘需求
     */
    @Query("{ 'skills': { '$in': ?0 } }")
    List<JobRequirement> findBySkillsContaining(String skill);

    /**
     * 根据经验级别搜索
     */
    List<JobRequirement> findByExperienceLevel(String experienceLevel);

    /**
     * 根据地点搜索
     */
    List<JobRequirement> findByLocationContainingIgnoreCase(String location);

    /**
     * 搜索包含特定关键词的招聘需求
     */
    @Query("{ '$or': [ " +
          "{ 'title': { '$regex': ?0, '$options': 'i' } }, " +
          "{ 'description': { '$regex': ?0, '$options': 'i' } }, " +
          "{ 'requirements': { '$regex': ?0, '$options': 'i' } }, " +
          "{ 'skills': { '$in': [?0] } } " +
          "] }")
    List<JobRequirement> searchByKeyword(String keyword);

    /**
     * 获取最新的招聘需求
     */
    List<JobRequirement> findTop10ByOrderByCreatedAtDesc();

    /**
     * 根据匹配分数排序
     */
    List<JobRequirement> findByMatchScoreGreaterThanOrderByMatchScoreDesc(Double minScore);

    /**
     * 统计不同经验级别的职位数量
     */
    @Query("{ 'experienceLevel': ?0 }")
    long countByExperienceLevel(String experienceLevel);
}