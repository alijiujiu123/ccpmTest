package com.cvagent.service;

import com.cvagent.model.OptimizationRule;
import com.cvagent.model.RuleVersion;
import com.cvagent.repository.OptimizationRuleRepository;
import com.cvagent.repository.RuleVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleVersionControlServiceTest {

    @Mock
    private RuleVersionRepository versionRepository;

    @Mock
    private OptimizationRuleRepository ruleRepository;

    @Mock
    private RuleEngineService ruleEngineService;

    @InjectMocks
    private RuleVersionControlService versionControlService;

    private OptimizationRule testRule;
    private RuleVersion testVersion1;
    private RuleVersion testVersion2;

    @BeforeEach
    void setUp() {
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

        // 创建测试版本
        testVersion1 = new RuleVersion();
        testVersion1.setId("version1");
        testVersion1.setRuleId("rule1");
        testVersion1.setVersion(1);
        testVersion1.setPattern("^[\\s\\S]{0,50}$");
        testVersion1.setSuggestion("建议扩展简历摘要");
        testVersion1.setDescription("检查简历摘要是否过于简短");
        testVersion1.setCategory("格式优化");
        testVersion1.setPriority(1);
        testVersion1.setTargetSection("SUMMARY");
        testVersion1.setIsActive(true);
        testVersion1.setChangeReason("初始版本");
        testVersion1.setChangedBy("system");
        testVersion1.setCreatedAt(LocalDateTime.now().minusDays(1));

        testVersion2 = new RuleVersion();
        testVersion2.setId("version2");
        testVersion2.setRuleId("rule1");
        testVersion2.setVersion(2);
        testVersion2.setPattern("^[\\s\\S]{0,100}$");
        testVersion2.setSuggestion("建议扩展简历摘要到100字");
        testVersion2.setDescription("检查简历摘要长度是否适当");
        testVersion2.setCategory("格式优化");
        testVersion2.setPriority(1);
        testVersion2.setTargetSection("SUMMARY");
        testVersion2.setIsActive(true);
        testVersion2.setChangeReason("测试更新");
        testVersion2.setChangedBy("testuser");
        testVersion2.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateNewVersion() {
        // 准备测试数据
        when(versionRepository.findLatestVersionByRuleId("rule1")).thenReturn(testVersion1);
        when(versionRepository.save(any())).thenAnswer(invocation -> {
            RuleVersion saved = invocation.getArgument(0);
            // 设置保存后的版本号和其他字段
            saved.setVersion(2);
            saved.setChangeReason("测试更新");
            return saved;
        });
        when(versionRepository.findByRuleIdAndVersion("rule1", 1)).thenReturn(testVersion1);

        // 执行测试
        RuleVersion newVersion = versionControlService.createNewVersion(testRule, "测试更新", "testuser");

        // 验证结果
        assertNotNull(newVersion);
        assertEquals("rule1", newVersion.getRuleId());
        assertEquals(2, newVersion.getVersion());
        assertEquals("测试更新", newVersion.getChangeReason());
        assertEquals("testuser", newVersion.getChangedBy());

        verify(versionRepository, times(2)).save(any()); // 创建新版本和过期旧版本
        verify(versionRepository, times(1)).findByRuleIdAndVersion("rule1", 1);
    }

    @Test
    void testCreateNewVersion_FirstVersion() {
        // 准备测试数据
        when(versionRepository.findLatestVersionByRuleId("rule1")).thenReturn(null);
        when(versionRepository.save(any())).thenAnswer(invocation -> {
            RuleVersion saved = invocation.getArgument(0);
            // 设置保存后的版本号和其他字段
            saved.setVersion(1);
            saved.setChangeReason("初始版本");
            return saved;
        });

        // 执行测试
        RuleVersion newVersion = versionControlService.createNewVersion(testRule, "初始版本", "testuser");

        // 验证结果
        assertNotNull(newVersion);
        assertEquals(1, newVersion.getVersion());
        assertEquals("初始版本", newVersion.getChangeReason());

        verify(versionRepository, times(1)).save(any());
        verify(versionRepository, times(0)).findByRuleIdAndVersion(any(), any());
    }

    @Test
    void testGetVersionHistory() {
        // 准备测试数据
        List<RuleVersion> versions = Arrays.asList(testVersion2, testVersion1);
        when(versionRepository.findByRuleIdOrderByVersionDesc("rule1")).thenReturn(versions);

        // 执行测试
        List<RuleVersion> history = versionControlService.getVersionHistory("rule1");

        // 验证结果
        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals(2, history.get(0).getVersion());
        assertEquals(1, history.get(1).getVersion());

        verify(versionRepository, times(1)).findByRuleIdOrderByVersionDesc("rule1");
    }

    @Test
    void testGetVersion() {
        // 准备测试数据
        when(versionRepository.findByRuleIdAndVersion("rule1", 1)).thenReturn(testVersion1);

        // 执行测试
        RuleVersion version = versionControlService.getVersion("rule1", 1);

        // 验证结果
        assertNotNull(version);
        assertEquals("rule1", version.getRuleId());
        assertEquals(1, version.getVersion());

        verify(versionRepository, times(1)).findByRuleIdAndVersion("rule1", 1);
    }

    @Test
    void testGetVersion_NotFound() {
        // 准备测试数据
        when(versionRepository.findByRuleIdAndVersion("rule1", 99)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            versionControlService.getVersion("rule1", 99);
        });

        verify(versionRepository, times(1)).findByRuleIdAndVersion("rule1", 99);
    }

    @Test
    void testRestoreToVersion() {
        // 准备测试数据
        when(versionRepository.findByRuleIdAndVersion("rule1", 1)).thenReturn(testVersion1);
        when(ruleRepository.findById("rule1")).thenReturn(Optional.of(testRule));
        when(ruleRepository.save(any())).thenReturn(testRule);
        when(versionRepository.save(any())).thenReturn(testVersion2);

        // 执行测试
        OptimizationRule restoredRule = versionControlService.restoreToVersion("rule1", 1, "testuser");

        // 验证结果
        assertNotNull(restoredRule);
        assertEquals("rule1", restoredRule.getId());
        assertEquals("^[\\s\\S]{0,50}$", restoredRule.getPattern());

        verify(ruleRepository, times(1)).save(any());
        verify(versionRepository, times(1)).save(any()); // 创建新版本
    }

    @Test
    void testCompareVersions() {
        // 准备测试数据
        when(versionRepository.findByRuleIdAndVersion("rule1", 1)).thenReturn(testVersion1);
        when(versionRepository.findByRuleIdAndVersion("rule1", 2)).thenReturn(testVersion2);

        // 执行测试
        RuleVersionControlService.VersionComparison comparison =
                versionControlService.compareVersions("rule1", 1, 2);

        // 验证结果
        assertNotNull(comparison);
        assertEquals("rule1", comparison.getRuleId());
        assertEquals(1, comparison.getVersion1());
        assertEquals(2, comparison.getVersion2());
        assertTrue(comparison.isPatternChanged());
        assertTrue(comparison.isSuggestionChanged()); // 两个版本的suggestion不同

        verify(versionRepository, times(1)).findByRuleIdAndVersion("rule1", 1);
        verify(versionRepository, times(1)).findByRuleIdAndVersion("rule1", 2);
    }

    @Test
    void testGetVersionStatistics() {
        // 准备测试数据
        List<RuleVersion> versions = Arrays.asList(testVersion2, testVersion1);
        when(versionRepository.findByRuleIdOrderByVersionDesc("rule1")).thenReturn(versions);

        // 执行测试
        RuleVersionControlService.VersionStatistics stats =
                versionControlService.getVersionStatistics("rule1");

        // 验证结果
        assertNotNull(stats);
        assertEquals("rule1", stats.getRuleId());
        assertEquals(2, stats.getTotalVersions());
        assertEquals(2, stats.getLatestVersion());
        assertEquals(1, stats.getModificationCount());
        assertNotNull(stats.getLastModifiedAt());

        verify(versionRepository, times(1)).findByRuleIdOrderByVersionDesc("rule1");
    }

    @Test
    void testGetVersionStatistics_EmptyHistory() {
        // 准备测试数据
        List<RuleVersion> versions = Arrays.asList();
        when(versionRepository.findByRuleIdOrderByVersionDesc("rule1")).thenReturn(versions);

        // 执行测试
        RuleVersionControlService.VersionStatistics stats =
                versionControlService.getVersionStatistics("rule1");

        // 验证结果
        assertNotNull(stats);
        assertEquals("rule1", stats.getRuleId());
        assertEquals(0, stats.getTotalVersions());
        assertEquals(0, stats.getLatestVersion());
        assertEquals(-1, stats.getModificationCount());
        assertNull(stats.getLastModifiedAt());

        verify(versionRepository, times(1)).findByRuleIdOrderByVersionDesc("rule1");
    }

    @Test
    void testCleanupExpiredVersions() {
        // 准备测试数据
        List<String> ruleIds = Arrays.asList("rule1", "rule2");
        List<RuleVersion> versions = Arrays.asList(testVersion2, testVersion1);

        when(ruleRepository.findAllRuleIds()).thenReturn(ruleIds);
        when(versionRepository.findByRuleIdOrderByVersionDesc("rule1")).thenReturn(versions);
        when(versionRepository.findByRuleIdOrderByVersionDesc("rule2")).thenReturn(versions);

        // 执行测试
        assertDoesNotThrow(() -> {
            versionControlService.cleanupExpiredVersions(1);
        });

        // 验证删除调用
        verify(versionRepository, times(2)).delete(any()); // 每个规则删除一个版本
        verify(ruleRepository, times(1)).findAllRuleIds();
    }

    @Test
    void testCleanupExpiredVersions_NoCleanupNeeded() {
        // 准备测试数据
        List<String> ruleIds = Arrays.asList("rule1");
        List<RuleVersion> versions = Arrays.asList(testVersion1); // 只有一个版本

        when(ruleRepository.findAllRuleIds()).thenReturn(ruleIds);
        when(versionRepository.findByRuleIdOrderByVersionDesc("rule1")).thenReturn(versions);

        // 执行测试
        assertDoesNotThrow(() -> {
            versionControlService.cleanupExpiredVersions(1);
        });

        // 验证没有删除调用
        verify(versionRepository, times(0)).delete(any());
    }

    @Test
    void testCreateNewVersion_Exception() {
        // 准备测试数据
        when(versionRepository.findLatestVersionByRuleId("rule1"))
                .thenThrow(new RuntimeException("数据库错误"));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            versionControlService.createNewVersion(testRule, "测试更新", "testuser");
        });
    }

    @Test
    void testRestoreToVersion_RuleNotFound() {
        // 准备测试数据
        when(versionRepository.findByRuleIdAndVersion("rule1", 1)).thenReturn(testVersion1);
        when(ruleRepository.findById("rule1")).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            versionControlService.restoreToVersion("rule1", 1, "testuser");
        });
    }

    @Test
    void testCompareVersions_VersionNotFound() {
        // 准备测试数据
        when(versionRepository.findByRuleIdAndVersion("rule1", 1)).thenReturn(testVersion1);
        when(versionRepository.findByRuleIdAndVersion("rule1", 2)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            versionControlService.compareVersions("rule1", 1, 2);
        });
    }
}