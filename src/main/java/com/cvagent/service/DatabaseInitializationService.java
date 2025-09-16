package com.cvagent.service;

import com.cvagent.model.OptimizationRule;
import com.cvagent.repository.OptimizationRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DatabaseInitializationService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializationService.class);

    @Autowired
    private OptimizationRuleRepository optimizationRuleRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDatabase() {
        logger.info("开始初始化数据库数据...");

        // 初始化简历优化规则
        initializeOptimizationRules();

        logger.info("数据库数据初始化完成");
    }

    private void initializeOptimizationRules() {
        // 检查是否已存在规则
        if (optimizationRuleRepository.count() > 0) {
            logger.info("简历优化规则已存在，跳过初始化");
            return;
        }

        List<OptimizationRule> rules = Arrays.asList(
            // 关键词优化规则
            new OptimizationRule("避免使用弱动词", "KEYWORD") {{
                setPattern(".*(负责|参与|协助).*");
                setSuggestion("使用更强的动词，如：领导、开发、实现、优化、创建、设计");
                setTargetSection("EXPERIENCE");
                setPriority(4);
            }},

            new OptimizationRule("量化成就", "KEYWORD") {{
                setPattern(".*(?:提高|改善|增加|减少|优化).*");
                setSuggestion("使用具体数字量化成就，如：效率提高30%、成本降低20%、用户增长50%");
                setTargetSection("EXPERIENCE");
                setPriority(5);
            }},

            // 格式优化规则
            new OptimizationRule("技能列表格式", "FORMAT") {{
                setPattern(".*(?:熟悉|了解|掌握).*");
                setSuggestion("技能应分类并明确熟练程度：精通、熟练、熟悉、了解");
                setTargetSection("SKILLS");
                setPriority(3);
            }},

            // 内容优化规则
            new OptimizationRule("个人简介长度", "CONTENT") {{
                setPattern("^(.{0,50}|.{300,})$");
                setSuggestion("个人简介应保持在50-300字之间，突出核心优势");
                setTargetSection("SUMMARY");
                setPriority(4);
            }},

            new OptimizationRule("项目经验结构", "CONTENT") {{
                setPattern("^(?!.*(?:项目描述|技术栈|主要职责|项目成果)).*$");
                setSuggestion("项目经验应包含：项目描述、技术栈、主要职责、项目成果");
                setTargetSection("EXPERIENCE");
                setPriority(3);
            }},

            // 结构优化规则
            new OptimizationRule("教育背景完整性", "STRUCTURE") {{
                setPattern("^(?!.*(?:学校|专业|学历|时间)).*$");
                setSuggestion("教育背景应包含：学校名称、专业、学历、时间");
                setTargetSection("EDUCATION");
                setPriority(3);
            }}
        );

        optimizationRuleRepository.saveAll(rules);
        logger.info("已初始化 {} 个简历优化规则", rules.size());
    }
}