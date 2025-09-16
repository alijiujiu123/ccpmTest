package com.cvagent.service;

import com.cvagent.model.EnhancedResume;
import com.cvagent.model.JobRequirement;
import com.cvagent.model.ResumeTemplate;
import com.cvagent.repository.EnhancedResumeRepository;
import com.cvagent.repository.JobRequirementRepository;
import com.cvagent.repository.ResumeTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 简历生成服务
 * 负责将简历数据转换为不同格式并应用模板
 */
@Service
@Transactional
public class ResumeGenerationService {

    @Autowired
    private EnhancedResumeRepository enhancedResumeRepository;

    @Autowired
    private JobRequirementRepository jobRequirementRepository;

    @Autowired
    private ResumeTemplateRepository resumeTemplateRepository;

    @Autowired
    private ResumeExportService resumeExportService;

    /**
     * 基础简历生成
     */
    public EnhancedResume generateBasicResume(String userId, Map<String, Object> resumeData) {
        EnhancedResume resume = new EnhancedResume();
        resume.setUserId(userId);
        resume.setTitle((String) resumeData.getOrDefault("title", "我的简历"));
        resume.setGeneratedBy("system");
        resume.setVersion("1.0");
        resume.setOptimizationStatus("pending");
        resume.setMatchScore(0.0);
        resume.setIsPublic(false);

        // 设置个人信息
        if (resumeData.containsKey("personalInfo")) {
            resume.setPersonalInfo(mapToPersonalInfo((Map<String, Object>) resumeData.get("personalInfo")));
        }

        // 设置工作经验
        if (resumeData.containsKey("workExperience")) {
            resume.setWorkExperience(mapToWorkExperience((List<Map<String, Object>>) resumeData.get("workExperience")));
        }

        // 设置教育背景
        if (resumeData.containsKey("education")) {
            resume.setEducation(mapToEducation((List<Map<String, Object>>) resumeData.get("education")));
        }

        // 设置技能
        if (resumeData.containsKey("skills")) {
            resume.setSkills(mapToSkills((Map<String, Object>) resumeData.get("skills")));
        }

        // 设置项目经验
        if (resumeData.containsKey("projects")) {
            resume.setProjects(mapToProjects((List<Map<String, Object>>) resumeData.get("projects")));
        }

        return enhancedResumeRepository.save(resume);
    }

    /**
     * 根据招聘需求生成优化简历
     */
    public EnhancedResume generateOptimizedResume(String userId, String baseResumeId, String jobRequirementId) {
        // 获取基础简历和招聘需求
        EnhancedResume baseResume = enhancedResumeRepository.findById(baseResumeId)
                .orElseThrow(() -> new RuntimeException("基础简历不存在"));

        JobRequirement jobRequirement = jobRequirementRepository.findById(jobRequirementId)
                .orElseThrow(() -> new RuntimeException("招聘需求不存在"));

        // 创建新的优化简历
        EnhancedResume optimizedResume = new EnhancedResume();
        optimizedResume.setUserId(userId);
        optimizedResume.setBaseResumeId(baseResumeId);
        optimizedResume.setJobRequirementId(jobRequirementId);
        optimizedResume.setTitle(baseResume.getTitle() + " - 优化版");
        optimizedResume.setGeneratedBy("ai_optimized");
        optimizedResume.setVersion(generateNextVersion(baseResume.getVersion()));
        optimizedResume.setOptimizationStatus("processing");
        optimizedResume.setIsPublic(false);

        // 复制基础数据
        optimizedResume.setPersonalInfo(baseResume.getPersonalInfo());
        optimizedResume.setWorkExperience(baseResume.getWorkExperience());
        optimizedResume.setEducation(baseResume.getEducation());
        optimizedResume.setSkills(baseResume.getSkills());
        optimizedResume.setProjects(baseResume.getProjects());

        // 保存到数据库
        optimizedResume = enhancedResumeRepository.save(optimizedResume);

        // 异步优化简历内容
        optimizeResumeContent(optimizedResume, jobRequirement);

        return optimizedResume;
    }

    /**
     * 使用模板生成简历
     */
    public byte[] generateResumeWithTemplate(String resumeId, String templateId, String format) {
        EnhancedResume resume = enhancedResumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("简历不存在"));

        ResumeTemplate template = resumeTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在"));

        // 更新模板使用次数
        resumeTemplateRepository.incrementUsageCount(templateId);

        // 根据格式生成简历
        switch (format.toLowerCase()) {
            case "pdf":
                return resumeExportService.exportToPDF(resume, template);
            case "doc":
            case "docx":
                return resumeExportService.exportToWord(resume, template);
            case "html":
                return resumeExportService.exportToHTML(resume, template);
            default:
                throw new RuntimeException("不支持的导出格式: " + format);
        }
    }

    /**
     * 获取简历预览
     */
    public String getResumePreview(String resumeId, String templateId) {
        EnhancedResume resume = enhancedResumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("简历不存在"));

        ResumeTemplate template = resumeTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("模板不存在"));

        return resumeExportService.generatePreview(resume, template);
    }

    /**
     * 获取用户的简历历史
     */
    public List<EnhancedResume> getUserResumeHistory(String userId) {
        return enhancedResumeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 获取简历版本对比
     */
    public Map<String, Object> compareResumeVersions(String resumeId1, String resumeId2) {
        EnhancedResume resume1 = enhancedResumeRepository.findById(resumeId1)
                .orElseThrow(() -> new RuntimeException("简历1不存在"));

        EnhancedResume resume2 = enhancedResumeRepository.findById(resumeId2)
                .orElseThrow(() -> new RuntimeException("简历2不存在"));

        return Map.of(
                "resume1", Map.of(
                        "id", resume1.getId(),
                        "title", resume1.getTitle(),
                        "version", resume1.getVersion(),
                        "matchScore", resume1.getMatchScore(),
                        "optimizationStatus", resume1.getOptimizationStatus(),
                        "createdAt", resume1.getCreatedAt()
                ),
                "resume2", Map.of(
                        "id", resume2.getId(),
                        "title", resume2.getTitle(),
                        "version", resume2.getVersion(),
                        "matchScore", resume2.getMatchScore(),
                        "optimizationStatus", resume2.getOptimizationStatus(),
                        "createdAt", resume2.getCreatedAt()
                ),
                "comparison", compareResumeContent(resume1, resume2)
        );
    }

    /**
     * 删除简历
     */
    public void deleteResume(String resumeId, String userId) {
        EnhancedResume resume = enhancedResumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("简历不存在"));

        if (!resume.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此简历");
        }

        enhancedResumeRepository.delete(resume);
    }

    /**
     * 异步优化简历内容
     */
    private void optimizeResumeContent(EnhancedResume resume, JobRequirement jobRequirement) {
        // 这里应该调用AI服务进行优化
        // 暂时实现基本的匹配度计算
        double matchScore = calculateMatchScore(resume, jobRequirement);

        resume.setMatchScore(matchScore);
        resume.setOptimizationStatus("completed");
        resume.setOptimizedAt(LocalDateTime.now());

        // 设置优化指标
        resume.setOptimizationMetrics(Map.of(
                "keywordMatch", matchScore * 0.4,
                "skillRelevance", matchScore * 0.3,
                "experienceMatch", matchScore * 0.2,
                "formatScore", matchScore * 0.1
        ));

        enhancedResumeRepository.save(resume);
    }

    /**
     * 计算简历与招聘需求的匹配度
     */
    private double calculateMatchScore(EnhancedResume resume, JobRequirement jobRequirement) {
        // 简化的匹配度计算算法
        double score = 0.0;

        // 技能匹配
        if (resume.getSkills() != null && jobRequirement.getSkills() != null) {
            long matchingSkills = resume.getSkills().getTechnicalSkills().stream()
                    .filter(skill -> jobRequirement.getSkills().contains(skill))
                    .count();
            score += (double) matchingSkills / jobRequirement.getSkills().size() * 0.4;
        }

        // 经验匹配
        if (resume.getWorkExperience() != null && jobRequirement.getExperience() != null) {
            // 简化的经验匹配逻辑
            score += 0.3;
        }

        // 教育背景匹配
        if (resume.getEducation() != null && jobRequirement.getEducation() != null) {
            // 简化的教育匹配逻辑
            score += 0.2;
        }

        // 其他匹配因素
        score += 0.1;

        return Math.min(score, 1.0);
    }

    /**
     * 生成下一个版本号
     */
    private String generateNextVersion(String currentVersion) {
        try {
            double version = Double.parseDouble(currentVersion);
            return String.format("%.1f", version + 0.1);
        } catch (NumberFormatException e) {
            return "1.0";
        }
    }

    /**
     * 对比简历内容
     */
    private Map<String, Object> compareResumeContent(EnhancedResume resume1, EnhancedResume resume2) {
        return Map.of(
                "matchScoreDifference", resume2.getMatchScore() - resume1.getMatchScore(),
                "optimizationImproved", "completed".equals(resume2.getOptimizationStatus()),
                "versionProgress", resume2.getVersion() + " > " + resume1.getVersion()
        );
    }

    // 辅助方法：将Map转换为实体对象
    private EnhancedResume.PersonalInfo mapToPersonalInfo(Map<String, Object> data) {
        EnhancedResume.PersonalInfo personalInfo = new EnhancedResume.PersonalInfo();
        personalInfo.setName((String) data.getOrDefault("name", ""));
        personalInfo.setEmail((String) data.getOrDefault("email", ""));
        personalInfo.setPhone((String) data.getOrDefault("phone", ""));
        personalInfo.setLocation((String) data.getOrDefault("location", ""));
        personalInfo.setSummary((String) data.getOrDefault("summary", ""));
        return personalInfo;
    }

    private EnhancedResume.WorkExperience mapToWorkExperience(List<Map<String, Object>> data) {
        EnhancedResume.WorkExperience workExperience = new EnhancedResume.WorkExperience();
        // 实现工作经验映射逻辑
        return workExperience;
    }

    private EnhancedResume.Education mapToEducation(List<Map<String, Object>> data) {
        EnhancedResume.Education education = new EnhancedResume.Education();
        // 实现教育背景映射逻辑
        return education;
    }

    private EnhancedResume.Skills mapToSkills(Map<String, Object> data) {
        EnhancedResume.Skills skills = new EnhancedResume.Skills();
        // 实现技能映射逻辑
        return skills;
    }

    private EnhancedResume.Projects mapToProjects(List<Map<String, Object>> data) {
        EnhancedResume.Projects projects = new EnhancedResume.Projects();
        // 实现项目经验映射逻辑
        return projects;
    }
}