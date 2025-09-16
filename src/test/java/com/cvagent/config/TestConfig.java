package com.cvagent.config;

import com.cvagent.service.AiServiceManager;
import com.cvagent.service.AiMonitoringService;
import com.cvagent.service.PromptTemplateService;
import com.cvagent.repository.AiUsageLogRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 测试配置类
 * 提供测试用的模拟Bean
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public AiServiceManager mockAiServiceManager() {
        return Mockito.mock(AiServiceManager.class);
    }

    @Bean
    @Primary
    public PromptTemplateService mockPromptTemplateService() {
        return Mockito.mock(PromptTemplateService.class);
    }

    @Bean
    @Primary
    public AiMonitoringService mockAiMonitoringService() {
        return Mockito.mock(AiMonitoringService.class);
    }

    @Bean
    @Primary
    public AiUsageLogRepository mockAiUsageLogRepository() {
        return Mockito.mock(AiUsageLogRepository.class);
    }
}