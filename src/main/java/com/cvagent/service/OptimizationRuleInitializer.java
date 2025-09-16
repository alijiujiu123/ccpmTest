package com.cvagent.service;

import com.cvagent.model.OptimizationRule;
import com.cvagent.repository.OptimizationRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 优化规则初始化服务
 * 在系统启动时创建默认的简历优化规则
 */
@Component
public class OptimizationRuleInitializer {

    private static final Logger logger = LoggerFactory.getLogger(OptimizationRuleInitializer.class);

    @Autowired
    private OptimizationRuleRepository ruleRepository;

    @Autowired
    private RuleEngineService ruleEngineService;

    /**
     * 系统启动时初始化默认优化规则
     */
    @EventListener(ContextRefreshedEvent.class)
    public void initializeDefaultRules() {
        logger.info("开始初始化默认简历优化规则");

        // 检查是否已有规则存在
        if (ruleRepository.count() > 0) {
            logger.info("已存在优化规则，跳过初始化");
            return;
        }

        List<OptimizationRule> defaultRules = createDefaultRules();

        for (OptimizationRule rule : defaultRules) {
            ruleEngineService.createRule(rule);
        }

        logger.info("成功初始化 {} 条默认优化规则", defaultRules.size());
    }

    /**
     * 创建默认的10条优化规则
     */
    private List<OptimizationRule> createDefaultRules() {
        return Arrays.asList(
            // 1. 简历摘要优化规则
            createRule("简历摘要长度检查", "格式优化", "SUMMARY", "^[\\s\\S]{0,50}$",
                    "检查简历摘要是否过于简短", "简历摘要应该包含3-5个关键成就和技能，长度建议在100-200字之间", 1),

            // 2. 技能关键词优化规则
            createRule("技能关键词缺失", "内容优化", "SKILLS", "(?i)(java|python|javascript|react|spring|sql|git|docker|kubernetes)",
                    "检查是否包含热门技术关键词", "建议添加与目标职位相关的技术关键词，提高简历通过率", 2),

            // 3. 工作经历量化检查
            createRule("工作经历量化指标", "内容优化", "EXPERIENCE", "负责.*工作|参与.*项目|协助.*完成",
                    "检查工作经历是否包含量化成果", "使用具体数字量化你的成就，例如：'提高效率30%'、'节省成本50万'等", 1),

            // 4. 时间格式一致性检查
            createRule("时间格式一致性", "格式优化", "ALL", "\\d{4}\\.\\d{1,2}|\\d{4}/\\d{1,2}|\\d{4}年\\d{1,2}月",
                    "检查时间格式是否一致", "请统一时间格式，建议使用'YYYY.MM'格式，例如：2023.01", 3),

            // 5. 联系方式完整性检查
            createRule("联系方式完整性", "完整性检查", "ALL", "(?i)(电话|手机|邮箱|email|@.*\\.com)",
                    "检查联系方式是否完整", "请确保包含电话和邮箱联系方式，建议使用专业邮箱地址", 1),

            // 6. 教育背景详细信息检查
            createRule("教育背景详细程度", "内容优化", "EDUCATION", "^[^\\n]*大学|^[^\\n]*学院",
                    "检查教育背景是否包含足够详细的信息", "建议添加学位、专业、毕业时间，以及相关课程和GPA信息", 2),

            // 7. 项目经验结果导向检查
            createRule("项目经验结果导向", "内容优化", "EXPERIENCE", "负责.*开发|参与.*设计|协助.*测试",
                    "检查项目经验是否突出结果", "使用STAR法则描述项目经验：情境(Situation)、任务(Task)、行动(Action)、结果(Result)", 2),

            // 8. 个人技能水平描述检查
            createRule("技能水平描述", "内容优化", "SKILLS", "(?i)(精通|熟练|掌握|了解|熟悉)",
                    "检查技能水平描述是否恰当", "准确描述技能水平，避免过度使用'精通'，建议使用'熟练掌握'、'具备经验'等更客观的描述", 3),

            // 9. 简历长度检查
            createRule("简历长度控制", "格式优化", "ALL", "^[\\s\\S]{1000,}",
                    "检查简历是否过长", "简历长度建议控制在1-2页，重点突出与目标职位相关的经验和技能", 2),

            // 10. 职业目标明确性检查
            createRule("职业目标明确性", "内容优化", "SUMMARY", "寻求.*机会|希望.*职位|应聘.*岗位",
                    "检查职业目标是否明确", "在简历开头明确表达职业目标，与应聘职位要求相匹配", 2)
        );
    }

    /**
     * 创建规则的辅助方法
     */
    private OptimizationRule createRule(String name, String category, String targetSection, String pattern,
                                      String description, String suggestion, int priority) {
        OptimizationRule rule = new OptimizationRule();
        rule.setName(name);
        rule.setCategory(category);
        rule.setTargetSection(targetSection);
        rule.setPattern(pattern);
        rule.setDescription(description);
        rule.setSuggestion(suggestion);
        rule.setPriority(priority);
        rule.setIsActive(true);
        return rule;
    }
}