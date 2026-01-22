package com.example.spartallm.chat.application.dto.result;

import com.example.spartallm.chat.domain.model.DocumentSource;
import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;
import java.util.Map;

public record ChatResult(
    String id,
    String object,
    Long created,
    String model,
    ArrayNode choices,
    UsageResult usage,
    List<DocumentSourceResult> sources
) {
    public static ChatResult of(LlmResponseMessage llmResponseMessage) {
        return of(llmResponseMessage, List.of());
    }

    public static ChatResult of(LlmResponseMessage llmResponseMessage, List<DocumentSource> sources) {
        UsageResult usageResult = UsageResult.from(llmResponseMessage.getUsage());

        List<DocumentSourceResult> sourceResults = sources.stream()
            .map(DocumentSourceResult::from)
            .toList();

        return new ChatResult(
            llmResponseMessage.getId(),
            llmResponseMessage.getObject(),
            llmResponseMessage.getCreated(),
            llmResponseMessage.getModel(),
            llmResponseMessage.getChoices(),
            usageResult,
            sourceResults
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

    public record DocumentSourceResult(
        String content,
        Long knowledgeDocumentId,
        Integer chunkIndex,
        Map<String, Object> metadata
    ) {
        public static DocumentSourceResult from(DocumentSource source) {
            return new DocumentSourceResult(
                source.content(),
                source.knowledgeDocumentId(),
                source.chunkIndex(),
                source.metadata()
            );
        }
    }
}
