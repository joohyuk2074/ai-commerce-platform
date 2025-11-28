package com.example.spartallm.knowledge.domain.model;

import com.example.spartallm.knowledge.domain.vo.ChunkMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class KnowledgeDocumentChunk {
    private Long id;

    private Long documentId;

    private int chunkIndex;

    private String text;

    private ChunkMetadata metadata;

    public static KnowledgeDocumentChunk create(
        int chunkIndex,
        String text,
        Map<String, Object> metadata
    ) {
        return new KnowledgeDocumentChunk(
            null,
            null,
            chunkIndex,
            text,
            ChunkMetadata.from(metadata == null ? Map.of() : new HashMap<>(metadata))
        );
    }

    void assignDocument(Long documentId) {
        if (this.documentId != null) {
            throw new IllegalStateException("이미 문서가 할당된 청크입니다");
        }
        this.documentId = documentId;
    }

    public static KnowledgeDocumentChunk reconstitute(
        Long id,
        Long documentId,
        int chunkIndex,
        String text,
        ChunkMetadata metadata
    ) {
        return new KnowledgeDocumentChunk(
            id,
            documentId,
            chunkIndex,
            text,
            metadata
        );
    }
}
