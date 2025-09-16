package com.cvagent.controller;

import com.cvagent.model.OptimizationRule;
import com.cvagent.service.RuleEngineService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OptimizationRuleControllerTest {

    @Mock
    private RuleEngineService ruleEngineService;

    @InjectMocks
    private OptimizationRuleController ruleController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private OptimizationRule testRule;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ruleController).build();
        objectMapper = new ObjectMapper();
        // 配置Java 8时间类型支持
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 创建测试规则
        testRule = new OptimizationRule();
        testRule.setId("rule1");
        testRule.setName("简历摘要长度检查");
        testRule.setPattern("^[\\s\\S]{0,50}$");
        testRule.setSuggestion("简历摘要应该包含3-5个关键成就和技能");
        testRule.setCategory("格式优化");
        testRule.setTargetSection("SUMMARY");
        testRule.setPriority(1);
        testRule.setIsActive(true);
        testRule.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateRule() throws Exception {
        // 准备测试数据
        when(ruleEngineService.createRule(any())).thenReturn(testRule);

        // 执行测试
        mockMvc.perform(post("/api/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("rule1"))
                .andExpect(jsonPath("$.name").value("简历摘要长度检查"));
    }

    @Test
    void testUpdateRule() throws Exception {
        // 准备测试数据
        OptimizationRule updatedRule = new OptimizationRule();
        updatedRule.setName("更新后的规则");
        when(ruleEngineService.updateRule(eq("rule1"), any())).thenReturn(testRule);

        // 执行测试
        mockMvc.perform(put("/api/rules/rule1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("rule1"));
    }

    @Test
    void testDeleteRule() throws Exception {
        // 执行测试
        mockMvc.perform(delete("/api/rules/rule1"))
                .andExpect(status().isOk());
    }

    @Test
    void testToggleRuleStatus() throws Exception {
        // 执行测试
        mockMvc.perform(patch("/api/rules/rule1/status")
                .param("isActive", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllRules() throws Exception {
        // 准备测试数据
        List<OptimizationRule> rules = Arrays.asList(testRule);
        when(ruleEngineService.getAllRules()).thenReturn(rules);

        // 执行测试
        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("rule1"))
                .andExpect(jsonPath("$[0].name").value("简历摘要长度检查"));
    }

    @Test
    void testGetRulesByPage() throws Exception {
        // 准备测试数据
        List<OptimizationRule> rules = Arrays.asList(testRule);
        when(ruleEngineService.getAllRules()).thenReturn(rules);

        // 执行测试
        mockMvc.perform(get("/api/rules/page")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "priority,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("rule1"));
    }

    @Test
    void testGetActiveRules() throws Exception {
        // 准备测试数据
        List<OptimizationRule> rules = Arrays.asList(testRule);
        when(ruleEngineService.getActiveRules()).thenReturn(rules);

        // 执行测试
        mockMvc.perform(get("/api/rules/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("rule1"));
    }

    @Test
    void testGetRulesByCategory() throws Exception {
        // 准备测试数据
        List<OptimizationRule> rules = Arrays.asList(testRule);
        when(ruleEngineService.getRulesByCategory("格式优化")).thenReturn(rules);

        // 执行测试
        mockMvc.perform(get("/api/rules/category/格式优化"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("格式优化"));
    }

    @Test
    void testSearchRules() throws Exception {
        // 准备测试数据
        List<OptimizationRule> rules = Arrays.asList(testRule);
        when(ruleEngineService.searchRules("简历")).thenReturn(rules);

        // 执行测试
        mockMvc.perform(get("/api/rules/search")
                .param("keyword", "简历"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("简历摘要长度检查"));
    }

    @Test
    void testGetAllCategories() throws Exception {
        // 准备测试数据
        Set<String> categories = Set.of("格式优化", "内容优化");
        when(ruleEngineService.getAllCategories()).thenReturn(categories);

        // 执行测试
        mockMvc.perform(get("/api/rules/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("格式优化"));
    }

    @Test
    void testApplyRules() throws Exception {
        // 准备测试数据
        List<RuleEngineService.OptimizationResult> results = Arrays.asList(
            new RuleEngineService.OptimizationResult()
        );
        when(ruleEngineService.applyAllRules(any(), any())).thenReturn(results);

        // 准备请求数据
        Map<String, String> request = new HashMap<>();
        request.put("resumeContent", "测试简历内容");
        request.put("targetSection", "SUMMARY");

        // 执行测试
        mockMvc.perform(post("/api/rules/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void testApplyRules_EmptyContent() throws Exception {
        // 准备请求数据
        Map<String, String> request = new HashMap<>();
        request.put("resumeContent", "");
        request.put("targetSection", "SUMMARY");

        // 执行测试
        mockMvc.perform(post("/api/rules/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("简历内容不能为空"));
    }

    @Test
    void testBatchApplyRules() throws Exception {
        // 准备测试数据
        RuleEngineService.BatchOptimizationResult batchResult =
            new RuleEngineService.BatchOptimizationResult();
        when(ruleEngineService.batchApplyRules(any())).thenReturn(batchResult);

        // 准备请求数据
        Map<String, String> request = new HashMap<>();
        request.put("resumeContent", "测试简历内容");

        // 执行测试
        mockMvc.perform(post("/api/rules/batch-apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStatistics() throws Exception {
        // 准备测试数据
        List<OptimizationRule> allRules = Arrays.asList(testRule);
        List<OptimizationRule> activeRules = Arrays.asList(testRule);
        Set<String> categories = Set.of("格式优化");

        when(ruleEngineService.getAllRules()).thenReturn(allRules);
        when(ruleEngineService.getActiveRules()).thenReturn(activeRules);
        when(ruleEngineService.getAllCategories()).thenReturn(categories);

        // 执行测试
        mockMvc.perform(get("/api/rules/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRules").value(1))
                .andExpect(jsonPath("$.activeRules").value(1))
                .andExpect(jsonPath("$.inactiveRules").value(0))
                .andExpect(jsonPath("$.categories").value(1));
    }

    @Test
    void testCreateRule_InvalidData() throws Exception {
        // 准备无效数据
        OptimizationRule invalidRule = new OptimizationRule();
        invalidRule.setName(""); // 无效的名称
        invalidRule.setPattern(null); // 无效的模式

        when(ruleEngineService.createRule(any())).thenThrow(new RuntimeException("规则数据无效"));

        // 执行测试
        mockMvc.perform(post("/api/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRule)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateRule_NotFound() throws Exception {
        // 准备测试数据
        OptimizationRule updatedRule = new OptimizationRule();
        updatedRule.setName("更新的规则");

        when(ruleEngineService.updateRule(eq("nonexistent"), any()))
                .thenThrow(new RuntimeException("规则不存在"));

        // 执行测试
        mockMvc.perform(put("/api/rules/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedRule)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteRule_NotFound() throws Exception {
        // 准备测试数据
        doThrow(new RuntimeException("规则不存在"))
                .when(ruleEngineService).deleteRule(eq("nonexistent"));

        // 执行测试
        mockMvc.perform(delete("/api/rules/nonexistent"))
                .andExpect(status().isBadRequest());
    }
}