package com.example.spartallm.knowledge.domain.entity;

import com.example.spartallm.knowledge.domain.vo.ChunkMetadata;

public class DocumentChunk {
    private Long id;
    private String documentId;  // FK
    private int chunkIndex;
    private String text;
    private int tokenCount;
    private String vectorId;  // Vector DB의 ID
    private ChunkMetadata metadata;
}
