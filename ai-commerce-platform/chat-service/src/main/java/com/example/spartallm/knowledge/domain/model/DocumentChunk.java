package com.example.spartallm.knowledge.domain.model;

import com.example.spartallm.knowledge.domain.vo.ChunkMetadata;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DocumentChunk {
    private Long id;

    private String documentId;

    private int chunkIndex;

    private String text;

    private ChunkMetadata metadata;

    public static DocumentChunk of(
        int chunkIndex,
        String text,
        ChunkMetadata metadata
    ) {
        return new DocumentChunk(
            null,
            null,
            chunkIndex,
            text,
            metadata
        );
    }
}
