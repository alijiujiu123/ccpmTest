package com.cvagent.repository;

import com.cvagent.model.FileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileDocumentRepository extends MongoRepository<FileDocument, String> {

    // 根据用户ID查找文件
    List<FileDocument> findByUserId(String userId);

    // 根据用户ID和文件ID查找文件
    Optional<FileDocument> findByIdAndUserId(String id, String userId);

    // 根据存储文件名查找
    Optional<FileDocument> findByStoredName(String storedName);

    // 根据MD5查找重复文件
    List<FileDocument> findByMd5(String md5);

    // 根据文件类型查找
    List<FileDocument> findByContentType(String contentType);

    // 根据文件大小范围查找
    List<FileDocument> findBySizeBetween(Long minSize, Long maxSize);

    // 根据上传时间范围查找
    List<FileDocument> findByUploadTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 查找用户的所有PDF文件
    List<FileDocument> findByUserIdAndContentType(String userId, String contentType);

    // 根据简历ID查找相关文件
    List<FileDocument> findByRelatedResumeId(String resumeId);

    // 统计用户的文件总大小
    @Query(value = "{ 'user.$id': ?0 }", fields = "{ 'size': 1 }")
    List<FileDocument> findFileSizeByUserId(String userId);

    // 查找大文件
    List<FileDocument> findBySizeGreaterThan(Long size);

    // 查找最近上传的文件
    @Query(value = "{ 'user.$id': ?0 }", sort = "{ 'uploadTime': -1 }")
    List<FileDocument> findRecentByUserId(String userId);

    // 删除用户的所有文件
    void deleteByUserId(String userId);

    // 删除简历相关的所有文件
    void deleteByRelatedResumeId(String resumeId);
}