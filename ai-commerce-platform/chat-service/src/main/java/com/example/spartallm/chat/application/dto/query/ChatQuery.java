package com.example.spartallm.chat.application.dto.query;

import com.example.spartallm.chat.domain.model.LlmRequestMessage;

import java.util.List;
import java.util.UUID;

public record ChatQuery(
    String model,

    List<ChatMessageQuery> messages,

    Double temperature,

    Integer maxTokens,

    Boolean stream
) {
    public ChatQuery {
        if (model == null) {
            throw new IllegalArgumentException("model cannot be null");
        }

        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages cannot be null or empty");
        }

        if (temperature == null) {
            temperature = 0.7;
        }

        if (maxTokens == null) {
            maxTokens = 500;
        }

        if (stream == null) {
            stream = false;
        }
    }

    public LlmRequestMessage toChatMessage() {
        String requestId = UUID.randomUUID().toString();

        String systemPrompt = this.messages.stream()
            .filter(m -> "system".equals(m.role()))
            .map(ChatMessageQuery::content)
            .reduce((a, b) -> a + "\n" + b)
            .orElse("");

        String userMessage = this.messages.stream()
            .filter(m -> "user".equals(m.role()))
            .map(ChatMessageQuery::content)
            .reduce((a, b) -> a + "\n" + b)
            .orElseThrow(() -> new IllegalArgumentException("사용자 메시지가 없습니다"));

        return LlmRequestMessage.of(requestId, model, systemPrompt, userMessage, temperature, maxTokens);
    }

    public record ChatMessageQuery(
        String role,

        String content
    ) {
        public ChatMessageQuery {
            if (role == null) {
                throw new IllegalArgumentException("role cannot be null");
            }

            if (content == null) {
                throw new IllegalArgumentException("content cannot be null");
            }
        }
    }
}
