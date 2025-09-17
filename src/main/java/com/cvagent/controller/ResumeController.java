package com.cvagent.controller;

import com.cvagent.dto.ResumeDto;
import com.cvagent.model.Resume;
import com.cvagent.model.User;
import com.cvagent.repository.UserRepository;
import com.cvagent.security.UserPrincipal;
import com.cvagent.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@Tag(name = "简历管理", description = "简历的创建、查询、更新和删除相关接口")
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
    @Operation(summary = "创建简历", description = "创建新的简历记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Resume> createResume(
            @Parameter(description = "简历数据", required = true)
            @RequestBody ResumeDto resumeDto,
            @Parameter(hidden = true)
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
    @Operation(summary = "获取简历列表", description = "获取当前用户的所有简历记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<List<Resume>> getUserResumes(
            @Parameter(hidden = true)
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
    @Operation(summary = "获取简历详情", description = "根据简历ID获取简历详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "简历不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Resume> getResumeById(
            @Parameter(description = "简历ID", required = true, example = "12345")
            @PathVariable String id,
            @Parameter(hidden = true)
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
    @Operation(summary = "更新简历", description = "更新指定简历的信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "简历不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Resume> updateResume(
            @Parameter(description = "简历ID", required = true, example = "12345")
            @PathVariable String id,
            @Parameter(description = "更新后的简历数据", required = true)
            @RequestBody ResumeDto resumeDto,
            @Parameter(hidden = true)
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
    @Operation(summary = "删除简历", description = "删除指定的简历记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "删除成功"),
        @ApiResponse(responseCode = "404", description = "简历不存在"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<Void> deleteResume(
            @Parameter(description = "简历ID", required = true, example = "12345")
            @PathVariable String id,
            @Parameter(hidden = true)
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
    @Operation(summary = "搜索简历", description = "根据关键词搜索用户的简历")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "搜索成功"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<List<Resume>> searchResumes(
            @Parameter(description = "搜索关键词", required = true, example = "Java开发")
            @RequestParam String keyword,
            @Parameter(hidden = true)
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
    @Operation(summary = "获取最近更新的简历", description = "获取最近修改的简历列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    })
    public ResponseEntity<List<Resume>> getRecentResumes(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Resume> resumes = resumeService.getRecentResumes(user);
        logger.info("用户 {} 查询最近更新的简历，共 {} 个", user.getUsername(), resumes.size());
        return ResponseEntity.ok(resumes);
    }
}