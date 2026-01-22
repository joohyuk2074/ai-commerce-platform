package com.example.spartallm.chat.infrastructure.adapter;

import com.example.spartallm.chat.domain.model.ChatOptions;
import com.example.spartallm.chat.infrastructure.adapter.mapper.LlmResponseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Claude (Anthropic) LLM Adapter
 * 책임: Anthropic Provider별 ChatOptions 변환
 */
@Slf4j
@Component("claudeChatAdapter")
public class ClaudeChatAdapter extends AbstractLlmChatAdapter {

    public ClaudeChatAdapter(
        List<AnthropicChatModel> anthropicChatModels,
        LlmResponseMapper responseMapper
    ) {
        super(
            initializeChatClients(anthropicChatModels),
            responseMapper
        );
        log.info("ClaudeChatAdapter initialized with {} models", chatClients.size());
    }

    /**
     * AnthropicChatModel 목록으로 ChatClient Map 초기화
     */
    private static Map<String, ChatClient> initializeChatClients(List<AnthropicChatModel> models) {
        return models.stream()
            .collect(Collectors.toMap(
                model -> model.getDefaultOptions().getModel(),
                ChatClient::create,
                (m1, m2) -> {
                    // 중복 모델명이 있을 경우 첫 번째 것 사용
                    return m1;
                }
            ));
    }

    @Override
    protected AnthropicChatOptions buildChatOptions(ChatOptions options) {
        return AnthropicChatOptions.builder()
            .temperature(options.temperature())
            .maxTokens(options.maxTokens())  // Claude는 maxTokens 사용
            .topK(options.topK())
            .topP(options.topP())
            .build();
    }

    @Override
    protected String getProviderName() {
        return "anthropic";
    }
}