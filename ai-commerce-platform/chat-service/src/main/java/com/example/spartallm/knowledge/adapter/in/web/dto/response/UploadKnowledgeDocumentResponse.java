package com.example.spartallm.knowledge.adapter.in.web.dto.response;

import com.example.spartallm.knowledge.application.dto.result.UploadDocumentResult;
import com.example.spartallm.knowledge.domain.vo.DocumentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UploadKnowledgeDocumentResponse {

    @JsonProperty("document_id")
    private Long documentId;

    @JsonProperty("knowledge_id")
    private Long knowledgeId;

    private String filename;

    private Long size;

    @JsonProperty("content_type")
    private String contentType;

    private DocumentStatus status;

    @JsonProperty("total_chunks")
    private Integer totalChunks;

    public static UploadKnowledgeDocumentResponse from(UploadDocumentResult result) {
        return UploadKnowledgeDocumentResponse.builder()
            .documentId(result.getDocumentId())
            .knowledgeId(result.getKnowledgeId())
            .filename(result.getFilename())
            .size(result.getSize())
            .contentType(result.getContentType())
            .status(result.getStatus())
            .totalChunks(result.getTotalChunks())
            .build();
    }
}