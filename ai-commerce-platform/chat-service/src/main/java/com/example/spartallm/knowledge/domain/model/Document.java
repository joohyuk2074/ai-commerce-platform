package com.example.spartallm.knowledge.domain.model;

import com.example.spartallm.knowledge.domain.vo.DocumentStatus;

public class Document {
    private String id;

    private String knowledgeId;  // FK

    private String filename;

    private Long size;

    private String contentType;

    private DocumentStatus status;  // UPLOADING, PROCESSING, COMPLETED, FAILED

    private int totalChunks;

    private int processedChunks;
}
