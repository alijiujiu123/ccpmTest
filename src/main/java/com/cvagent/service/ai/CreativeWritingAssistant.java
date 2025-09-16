package com.cvagent.service.ai;

/**
 * 创意写作接口
 */
public interface CreativeWritingAssistant {
    String generateCoverLetter(String resumeContent, String jobDescription, String companyInfo);
    String generateProjectDescription(String projectInfo);
    String generateSelfIntroduction(String resumeContent, String context);
}
