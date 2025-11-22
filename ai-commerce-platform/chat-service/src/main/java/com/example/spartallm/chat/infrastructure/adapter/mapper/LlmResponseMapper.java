package com.example.spartallm.chat.infrastructure.adapter.mapper;

import com.example.spartallm.chat.domain.model.LlmRequestMessage;
import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

public interface LlmResponseMapper {

    /**
     * 동기 응답 변환
     *
     * @param requestMessage LLM 요청 메시지
     * @param chatClient Spring AI ChatClient
     * @param options Provider별 옵션 (OllamaChatOptions, AnthropicChatOptions 등)
     * @return 도메인 응답 메시지
     */
    LlmResponseMessage mapSyncResponse(
        LlmRequestMessage requestMessage,
        ChatClient chatClient,
        Object options
    );

    /**
     * 스트리밍 응답 변환
     *
     * @param requestMessage LLM 요청 메시지
     * @param chatClient Spring AI ChatClient
     * @param options Provider별 옵션 (OllamaChatOptions, AnthropicChatOptions 등)
     * @return 도메인 응답 메시지 스트림
     */
    Flux<LlmResponseMessage> mapStreamResponse(
        LlmRequestMessage requestMessage,
        ChatClient chatClient,
        Object options
    );
}