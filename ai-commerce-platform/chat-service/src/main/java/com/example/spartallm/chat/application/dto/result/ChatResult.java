package com.example.spartallm.chat.application.dto.result;

import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import com.fasterxml.jackson.databind.node.ArrayNode;

public record ChatResult(
    String id,
    String object,
    Long created,
    String model,
    ArrayNode choices,
    UsageResult usage
) {
    public static ChatResult of(LlmResponseMessage llmResponseMessage) {

        UsageResult usageResult = UsageResult.from(llmResponseMessage.getUsage());

        return new ChatResult(
            llmResponseMessage.getId(),
            llmResponseMessage.getObject(),
            llmResponseMessage.getCreated(),
            llmResponseMessage.getModel(),
            llmResponseMessage.getChoices(),
            usageResult
        );
    }

    public record UsageResult(
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens
    ) {
        public static UsageResult from(LlmResponseMessage.LlmUsage llmUsage) {
            if (llmUsage == null) {
                return null;
            }

            return new UsageResult(
                llmUsage.promptTokens(),
                llmUsage.completionTokens(),
                llmUsage.totalTokens()
            );
        }
    }
}
