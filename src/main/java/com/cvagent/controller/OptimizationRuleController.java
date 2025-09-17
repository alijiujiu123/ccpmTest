package com.cvagent.controller;

import com.cvagent.model.OptimizationRule;
import com.cvagent.service.RuleEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 优化规则管理控制器
 * 提供规则的CRUD操作和管理功能
 */
@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = "*")
@Tag(name = "优化规则管理", description = "简历优化规则的CRUD操作和管理功能接口")
@SecurityRequirement(name = "bearerAuth")
public class OptimizationRuleController {

    private static final Logger logger = LoggerFactory.getLogger(OptimizationRuleController.class);

    @Autowired
    private RuleEngineService ruleEngineService;

    /**
     * 创建新规则
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建规则", description = "创建新的优化规则（需要管理员权限）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    public ResponseEntity<OptimizationRule> createRule(
            @Parameter(description = "优化规则数据", required = true)
            @RequestBody OptimizationRule rule,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Object userPrincipal) {
        logger.info("用户 {} 创建新规则: {}", userPrincipal, rule.getName());

        try {
            OptimizationRule createdRule = ruleEngineService.createRule(rule);
            return ResponseEntity.ok(createdRule);
        } catch (Exception e) {
            logger.error("创建规则失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新规则
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新规则", description = "更新指定的优化规则（需要管理员权限）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "404", description = "规则不存在"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    public ResponseEntity<OptimizationRule> updateRule(
            @Parameter(description = "规则ID", required = true, example = "rule123")
            @PathVariable String id,
            @Parameter(description = "规则更新数据", required = true)
            @RequestBody OptimizationRule ruleDetails,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Object userPrincipal) {
        logger.info("用户 {} 更新规则: {}", userPrincipal, id);

        try {
            OptimizationRule updatedRule = ruleEngineService.updateRule(id, ruleDetails);
            return ResponseEntity.ok(updatedRule);
        } catch (Exception e) {
            logger.error("更新规则失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除规则", description = "删除指定的优化规则（需要管理员权限）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "404", description = "规则不存在"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    public ResponseEntity<Void> deleteRule(
            @Parameter(description = "规则ID", required = true, example = "rule123")
            @PathVariable String id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Object userPrincipal) {
        logger.info("用户 {} 删除规则: {}", userPrincipal, id);

        try {
            ruleEngineService.deleteRule(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("删除规则失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 激活/停用规则
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "切换规则状态", description = "激活或停用指定的优化规则（需要管理员权限）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "状态切换成功"),
        @ApiResponse(responseCode = "404", description = "规则不存在"),
        @ApiResponse(responseCode = "403", description = "权限不足")
    })
    public ResponseEntity<Void> toggleRuleStatus(
            @Parameter(description = "规则ID", required = true, example = "rule123")
            @PathVariable String id,
            @Parameter(description = "是否激活", required = true, example = "true")
            @RequestParam boolean isActive,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Object userPrincipal) {
        logger.info("用户 {} 切换规则状态: {} -> {}", userPrincipal, id, isActive);

        try {
            ruleEngineService.toggleRuleStatus(id, isActive);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("切换规则状态失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取所有规则
     */
    @GetMapping
    @Operation(summary = "获取所有规则", description = "获取所有的优化规则列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<OptimizationRule>> getAllRules() {
        logger.info("获取所有规则");

        try {
            List<OptimizationRule> rules = ruleEngineService.getAllRules();
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("获取规则列表失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 分页获取规则
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取规则", description = "分页获取优化规则列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<Page<OptimizationRule>> getRulesByPage(
            @Parameter(description = "页码", required = false, example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页面大小", required = false, example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序方式", required = false, example = "priority,desc")
            @RequestParam(defaultValue = "priority,desc") String sort) {

        logger.info("分页获取规则: page={}, size={}, sort={}", page, size, sort);

        try {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

            // 简化的分页实现
            List<OptimizationRule> allRules = ruleEngineService.getAllRules();
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allRules.size());

            List<OptimizationRule> pageContent = allRules.subList(start, end);
            Page<OptimizationRule> pageResult = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, allRules.size());

            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            logger.error("分页获取规则失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取活跃规则
     */
    @GetMapping("/active")
    @Operation(summary = "获取活跃规则", description = "获取所有活跃的优化规则")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<OptimizationRule>> getActiveRules() {
        logger.info("获取活跃规则");

        try {
            List<OptimizationRule> rules = ruleEngineService.getActiveRules();
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("获取活跃规则失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 按类别获取规则
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "按类别获取规则", description = "获取指定类别的优化规则")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<List<OptimizationRule>> getRulesByCategory(
            @Parameter(description = "规则类别", required = true, example = "格式优化")
            @PathVariable String category) {
        logger.info("按类别获取规则: {}", category);

        try {
            List<OptimizationRule> rules = ruleEngineService.getRulesByCategory(category);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("按类别获取规则失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 搜索规则
     */
    @GetMapping("/search")
    @Operation(summary = "搜索规则", description = "根据关键词搜索优化规则")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "搜索成功")
    })
    public ResponseEntity<List<OptimizationRule>> searchRules(
            @Parameter(description = "搜索关键词", required = true, example = "格式")
            @RequestParam String keyword) {
        logger.info("搜索规则: {}", keyword);

        try {
            List<OptimizationRule> rules = ruleEngineService.searchRules(keyword);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("搜索规则失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取所有规则类别
     */
    @GetMapping("/categories")
    @Operation(summary = "获取规则类别", description = "获取所有的规则类别")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<Set<String>> getAllCategories() {
        logger.info("获取所有规则类别");

        try {
            Set<String> categories = ruleEngineService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("获取规则类别失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 应用规则到简历内容
     */
    @PostMapping("/apply")
    @Operation(summary = "应用规则", description = "将优化规则应用到简历内容")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "应用成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<Map<String, Object>> applyRules(
            @Parameter(description = "规则应用请求参数", required = true)
            @RequestBody Map<String, String> request) {
        logger.info("应用规则到简历内容");

        try {
            String resumeContent = request.get("resumeContent");
            String targetSection = request.get("targetSection");

            if (resumeContent == null || resumeContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "简历内容不能为空"));
            }

            List<RuleEngineService.OptimizationResult> results =
                ruleEngineService.applyAllRules(resumeContent, targetSection);

            return ResponseEntity.ok(Map.of(
                "results", results,
                "count", results.size(),
                "processedAt", LocalDateTime.now()
            ));
        } catch (Exception e) {
            logger.error("应用规则失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "规则应用失败"));
        }
    }

    /**
     * 批量应用规则
     */
    @PostMapping("/batch-apply")
    @Operation(summary = "批量应用规则", description = "批量将优化规则应用到简历内容")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "应用成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<RuleEngineService.BatchOptimizationResult> batchApplyRules(
            @Parameter(description = "批量应用请求参数", required = true)
            @RequestBody Map<String, String> request) {

        logger.info("批量应用规则");

        try {
            String resumeContent = request.get("resumeContent");

            if (resumeContent == null || resumeContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            RuleEngineService.BatchOptimizationResult result =
                ruleEngineService.batchApplyRules(resumeContent);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("批量应用规则失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取规则统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计信息", description = "获取优化规则的统计信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public ResponseEntity<Map<String, Object>> getStatistics() {
        logger.info("获取规则统计信息");

        try {
            List<OptimizationRule> allRules = ruleEngineService.getAllRules();
            List<OptimizationRule> activeRules = ruleEngineService.getActiveRules();
            Set<String> categories = ruleEngineService.getAllCategories();

            Map<String, Object> statistics = Map.of(
                "totalRules", allRules.size(),
                "activeRules", activeRules.size(),
                "inactiveRules", allRules.size() - activeRules.size(),
                "categories", categories.size(),
                "categoryList", categories
            );

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("获取规则统计失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}