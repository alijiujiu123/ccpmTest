package com.cvagent.service.ai;

/**
 * 简历优化接口
 */
public interface ResumeOptimizationAssistant {
    String optimizeResume(String resumeContent, String jobDescription);
    String improveResumeSection(String sectionContent, String sectionType);
    String generateResumeSummary(String resumeContent);
}
