package com.example.spartallm.knowledge.application.dto.result;

import com.example.spartallm.knowledge.domain.model.AccessControl;
import com.example.spartallm.knowledge.domain.model.FileMetadata;
import com.example.spartallm.knowledge.domain.vo.FileData;
import com.example.spartallm.knowledge.domain.vo.DocumentMeta;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UploadDocumentResult {

    private String id;
    private String userId;
    private String hash;
    private String filename;
    private FileData data;
    private DocumentMeta meta;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean status;
    private String path;
    private AccessControl accessControl;

    public static UploadDocumentResult from(FileMetadata fileMetadata) {
        return UploadDocumentResult.builder()
            .id(fileMetadata.getId())
            .userId(fileMetadata.getUserId())
            .hash(fileMetadata.getHash())
            .filename(fileMetadata.getFilename())
            .data(fileMetadata.getData())
            .meta(fileMetadata.getMeta())
            .createdAt(fileMetadata.getCreatedAt())
            .updatedAt(fileMetadata.getUpdatedAt())
            .status(fileMetadata.getStatus())
            .path(fileMetadata.getPath())
            .accessControl(fileMetadata.getAccessControl())
            .build();
    }
}