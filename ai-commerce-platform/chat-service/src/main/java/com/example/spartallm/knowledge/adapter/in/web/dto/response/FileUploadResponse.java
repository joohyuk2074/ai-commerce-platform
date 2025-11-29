package com.example.spartallm.knowledge.adapter.in.web.dto.response;

import com.example.spartallm.knowledge.application.dto.result.UploadDocumentResult;
import com.example.spartallm.knowledge.domain.model.AccessControl;
import com.example.spartallm.knowledge.domain.vo.FileData;
import com.example.spartallm.knowledge.domain.vo.DocumentMeta;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZoneOffset;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUploadResponse {

    private String id;

    @JsonProperty("user_id")
    private String userId;

    private String hash;

    private String filename;

    private FileData data;

    private DocumentMeta meta;

    @JsonProperty("created_at")
    private Long createdAt;

    @JsonProperty("updated_at")
    private Long updatedAt;

    private Boolean status;

    private String path;

    @JsonProperty("access_control")
    private AccessControl accessControl;

    public static FileUploadResponse from(UploadDocumentResult result) {
        return FileUploadResponse.builder()
            .id(result.getId())
            .userId(result.getUserId())
            .hash(result.getHash())
            .filename(result.getFilename())
            .data(result.getData())
            .meta(result.getMeta())
            .createdAt(result.getCreatedAt() != null ?
                result.getCreatedAt().toEpochSecond(ZoneOffset.UTC) : null)
            .updatedAt(result.getUpdatedAt() != null ?
                result.getUpdatedAt().toEpochSecond(ZoneOffset.UTC) : null)
            .status(result.getStatus())
            .path(result.getPath())
            .accessControl(result.getAccessControl())
            .build();
    }
}