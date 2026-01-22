package com.example.spartallm.knowledge.application.dto.command;

public record UploadDocumentCommand(
    Long knowledgeId,
    Long userId,
    String originalFileName,
    String contentType,
    long size,
    byte[] content
) {
}