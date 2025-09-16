package com.cvagent.controller;

import com.cvagent.model.OptimizationRule;
import com.cvagent.service.RuleEngineService;
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
public class OptimizationRuleController {

    private static final Logger logger = LoggerFactory.getLogger(OptimizationRuleController.class);

    @Autowired
    private RuleEngineService ruleEngineService;

    /**
     * 创建新规则
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OptimizationRule> createRule(@RequestBody OptimizationRule rule,
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
    public ResponseEntity<OptimizationRule> updateRule(@PathVariable String id,
                                                    @RequestBody OptimizationRule ruleDetails,
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
    public ResponseEntity<Void> deleteRule(@PathVariable String id,
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
    public ResponseEntity<Void> toggleRuleStatus(@PathVariable String id,
                                              @RequestParam boolean isActive,
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
    public ResponseEntity<Page<OptimizationRule>> getRulesByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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
    public ResponseEntity<List<OptimizationRule>> getRulesByCategory(@PathVariable String category) {
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
    public ResponseEntity<List<OptimizationRule>> searchRules(@RequestParam String keyword) {
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
    public ResponseEntity<Map<String, Object>> applyRules(@RequestBody Map<String, String> request) {
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
    public ResponseEntity<RuleEngineService.BatchOptimizationResult> batchApplyRules(
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