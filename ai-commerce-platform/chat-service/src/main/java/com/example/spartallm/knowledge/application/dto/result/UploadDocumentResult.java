package com.example.spartallm.knowledge.application.dto.result;

import com.example.spartallm.knowledge.domain.model.KnowledgeDocument;
import com.example.spartallm.knowledge.domain.vo.DocumentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UploadDocumentResult {

    private Long documentId;
    private Long knowledgeId;
    private String filename;
    private Long size;
    private String contentType;
    private DocumentStatus status;
    private int totalChunks;

    public static UploadDocumentResult from(KnowledgeDocument document) {
        return UploadDocumentResult.builder()
            .documentId(document.getId())
            .knowledgeId(document.getKnowledgeId())
            .filename(document.getFilename())
            .size(document.getSize())
            .contentType(document.getContentType())
            .status(document.getStatus())
            .totalChunks(document.getTotalChunks())
            .build();
    }
}