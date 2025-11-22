package com.example.spartallm.chat.domain.model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponseMessage {
    private String id;

    private String object;

    private String model;

    private ArrayNode choices;

    private LlmUsage usage;

    private Long created;

    public static LlmResponseMessage syncResponse(
        String requestId,
        String object,
        String modelName,
        ArrayNode choices,
        LlmUsage llmUsage,
        Long created
    ) {
        return new LlmResponseMessage(
            requestId,
            object,
            modelName,
            choices,
            llmUsage,
            created
        );
    }

    public record LlmUsage(
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens
    ) {
    }
}