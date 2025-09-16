package com.cvagent.controller;

import com.cvagent.dto.ResumeDto;
import com.cvagent.model.Resume;
import com.cvagent.model.User;
import com.cvagent.repository.UserRepository;
import com.cvagent.security.UserPrincipal;
import com.cvagent.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 创建简历
     */
    @PostMapping
    public ResponseEntity<Resume> createResume(
            @RequestBody ResumeDto resumeDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        logger.info("用户 {} 创建简历", user.getUsername());
        Resume resume = resumeService.createResume(resumeDto, user);
        return ResponseEntity.ok(resume);
    }

    /**
     * 获取用户的简历列表
     */
    @GetMapping
    public ResponseEntity<List<Resume>> getUserResumes(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Resume> resumes = resumeService.getUserResumes(user);
        logger.info("用户 {} 查询简历列表，共 {} 个简历", user.getUsername(), resumes.size());
        return ResponseEntity.ok(resumes);
    }

    /**
     * 根据ID获取简历
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resume> getResumeById(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Resume resume = resumeService.getResumeById(id, user);
        logger.info("用户 {} 查询简历: {}", user.getUsername(), id);
        return ResponseEntity.ok(resume);
    }

    /**
     * 更新简历
     */
    @PutMapping("/{id}")
    public ResponseEntity<Resume> updateResume(
            @PathVariable String id,
            @RequestBody ResumeDto resumeDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Resume resume = resumeService.updateResume(id, resumeDto, user);
        logger.info("用户 {} 更新简历: {}", user.getUsername(), id);
        return ResponseEntity.ok(resume);
    }

    /**
     * 删除简历
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        logger.info("用户 {} 删除简历: {}", user.getUsername(), id);
        resumeService.deleteResume(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * 搜索简历
     */
    @GetMapping("/search")
    public ResponseEntity<List<Resume>> searchResumes(
            @RequestParam String keyword,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Resume> resumes = resumeService.searchResumes(user, keyword);
        logger.info("用户 {} 搜索简历，关键词: {}, 结果数: {}", user.getUsername(), keyword, resumes.size());
        return ResponseEntity.ok(resumes);
    }

    /**
     * 获取最近更新的简历
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Resume>> getRecentResumes(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Resume> resumes = resumeService.getRecentResumes(user);
        logger.info("用户 {} 查询最近更新的简历，共 {} 个", user.getUsername(), resumes.size());
        return ResponseEntity.ok(resumes);
    }
}