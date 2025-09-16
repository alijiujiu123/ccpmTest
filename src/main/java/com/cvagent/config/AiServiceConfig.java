package com.cvagent.config;

import com.cvagent.service.AiServiceManager;
import com.cvagent.service.ai.ChatAssistant;
import com.cvagent.service.ai.CreativeWritingAssistant;
import com.cvagent.service.ai.ResumeOptimizationAssistant;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI服务配置类
 * 配置langchain4j和OpenAI模型
 */
@Configuration
public class AiServiceConfig {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.base-url}")
    private String baseUrl;

    @Value("${openai.model}")
    private String modelName;

    /**
     * 配置OpenAI聊天模型
     */
    @Bean
    public StreamingChatModel chatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(30))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 配置简历优化专用模型（更低的温度，更一致的结果）
     */
    @Bean
    public ChatModel resumeOptimizationModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(OpenAiChatModelName.GPT_4_TURBO_PREVIEW)
                .temperature(0.3)
                .timeout(Duration.ofSeconds(45))
                .maxRetries(3)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 配置创意写作模型（更高的温度，更有创意的结果）
     */
    @Bean
    public ChatModel creativeWritingModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(OpenAiChatModelName.GPT_4_TURBO_PREVIEW)
                .temperature(0.9)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(2)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public ChatAssistant chatAssistant(StreamingChatModel chatLanguageModel) {
        return AiServices.builder(ChatAssistant.class).streamingChatModel(chatLanguageModel).build();
    }

    @Bean
    public ResumeOptimizationAssistant resumeOptimizationAssistant(ChatModel resumeOptimizationModel) {
        return AiServices.builder(ResumeOptimizationAssistant.class).chatModel(resumeOptimizationModel).build();
    }

    @Bean
    public CreativeWritingAssistant creativeWritingAssistant(ChatModel creativeWritingModel) {
        return AiServices.builder(CreativeWritingAssistant.class).chatModel(creativeWritingModel).build();
    }
}