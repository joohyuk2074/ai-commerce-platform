package com.example.spartallm.chat.controller.dto.request;

import com.example.spartallm.chat.application.dto.query.ChatQuery;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ChatRequest(
    @NotBlank
    String model,

    @NotEmpty
    List<ChatMessageRequest> messages,

    Double temperature,

    Integer maxTokens,

    Boolean stream
) {
    public ChatRequest {
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

    public ChatQuery toChatQuery() {
        List<ChatQuery.ChatMessageQuery> messages = this.messages.stream()
            .map(m -> new ChatQuery.ChatMessageQuery(m.role(), m.content()))
            .toList();

        double temperature = this.temperature != null ? this.temperature : 0.7;
        int maxTokens = this.maxTokens != null ? this.maxTokens : 500;
        boolean stream = this.stream != null ? this.stream : false;

        return new ChatQuery(
            model,
            messages,
            temperature,
            maxTokens,
            stream
        );
    }

    public record ChatMessageRequest(
        @NotBlank
        String role,

        @NotBlank
        String content
    ) {}
}
