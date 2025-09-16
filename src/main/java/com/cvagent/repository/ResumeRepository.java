package com.cvagent.repository;

import com.cvagent.model.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends MongoRepository<Resume, String> {

    // 根据用户ID查找简历
    List<Resume> findByUserId(String userId);

    // 根据用户ID和简历ID查找简历
    Optional<Resume> findByIdAndUserId(String id, String userId);

    // 查找用户最新的简历
    @Query(value = "{ 'user.$id': ?0 }", sort = "{ 'updatedAt': -1 }")
    List<Resume> findLatestByUserId(String userId);

    // 根据关键词搜索简历
    @Query("{ '$or': [ " +
           "{ 'title': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'summary': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'skills': { '$regex': ?0, '$options': 'i' } } " +
           "] }")
    List<Resume> searchByKeyword(String keyword);

    // 根据目标行业和职位搜索
    List<Resume> findByTargetIndustryAndTargetPosition(String industry, String position);

    // 根据工作经验范围查询
    List<Resume> findByYearsOfExperienceGreaterThanEqual(Integer years);

    // 根据创建时间范围查询
    List<Resume> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 统计用户的简历数量
    long countByUserId(String userId);

    // 删除用户的所有简历
    void deleteByUserId(String userId);
}