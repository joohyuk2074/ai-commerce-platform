package com.example.spartallm.chat.domain.model;

public record LlmRequestMessage(
    String requestId,
    String modelName,
    String systemPrompt,
    String userMessage,
    String userId,
    ChatOptions chatOptions
) {
    public static LlmRequestMessage of(
        String requestId,
        String modelName,
        String systemPrompt,
        String userMessage,
        Double temperature,
        Integer maxTokens
    ) {
        ChatOptions chatOptions = ChatOptions.of(temperature, maxTokens);
        return new LlmRequestMessage(requestId, modelName, systemPrompt, userMessage, null, chatOptions);
    }
}