package com.cvagent.repository;

import com.cvagent.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    // 根据简历ID查找项目
    List<Project> findByResumeId(String resumeId);

    // 根据简历ID查找特定项目
    Optional<Project> findByIdAndResumeId(String id, String resumeId);

    // 查找简历的最新项目
    @Query(value = "{ 'resume.$id': ?0 }", sort = "{ 'updatedAt': -1 }")
    List<Project> findLatestByResumeId(String resumeId);

    // 根据技术栈搜索项目
    @Query("{ 'technologyStack': { '$regex': ?0, '$options': 'i' } }")
    List<Project> searchByTechnologyStack(String tech);

    // 查找进行中的项目
    List<Project> findByIsOngoingTrue();

    // 根据时间范围查找项目
    List<Project> findByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 根据项目名称搜索
    @Query("{ 'name': { '$regex': ?0, '$options': 'i' } }")
    List<Project> searchByName(String name);

    // 统计简历的项目数量
    long countByResumeId(String resumeId);

    // 删除简历的所有项目
    void deleteByResumeId(String resumeId);
}