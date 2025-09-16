package com.cvagent.service.ai;

/**
 * 代码审查接口
 */
public interface CodeReviewAssistant {
    String reviewCode(String code, String language);
    String suggestImprovements(String code, String requirements);
}
