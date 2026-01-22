package com.example.spartallm.chat.domain.model;

import java.util.Map;

/**
 * RAG에서 사용된 문서 출처 정보
 */
public record DocumentSource(
    String content,
    Long knowledgeDocumentId,
    Integer chunkIndex,
    Map<String, Object> metadata
) {
    public static DocumentSource of(
        String content,
        Long knowledgeDocumentId,
        Integer chunkIndex,
        Map<String, Object> metadata
    ) {
        return new DocumentSource(content, knowledgeDocumentId, chunkIndex, metadata);
    }
}