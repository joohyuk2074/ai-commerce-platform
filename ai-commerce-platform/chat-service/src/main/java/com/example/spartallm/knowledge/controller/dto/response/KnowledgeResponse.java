package com.example.spartallm.knowledge.controller.dto.response;

import com.example.spartallm.knowledge.domain.entity.Knowledge;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static java.time.ZoneId.systemDefault;

public record KnowledgeResponse(
    Long id,

    @JsonProperty("user_id")
    Long userId,

    String name,

    String description,

    KnowledgeDataResponse data,

    KnowledgeMetaResponse meta,

    @JsonProperty("access_control")
    AccessControlResponse accessControl,

    @JsonProperty("created_at")
    Long createdAt,

    @JsonProperty("updated_at")
    Long updatedAt,

    UserResponse user,

    List<FileResponse> files
) {

    public static KnowledgeResponse from(Knowledge knowledge) {
        return new KnowledgeResponse(
            knowledge.getId(),
            knowledge.getUserId(),
            knowledge.getName(),
            knowledge.getDescription(),
            new KnowledgeDataResponse(List.of()),
            new KnowledgeMetaResponse(),
            mapAccessControl(knowledge.getAccessControl()),
            knowledge.getCreatedAt() != null
                ? knowledge.getCreatedAt().atZone(systemDefault()).toEpochSecond()
                : null,
            knowledge.getUpdatedAt() != null
                ? knowledge.getUpdatedAt().atZone(systemDefault()).toEpochSecond()
                : null,
            null,
            List.of()
        );
    }

    private static AccessControlResponse mapAccessControl(com.example.spartallm.knowledge.domain.entity.AccessControl accessControl) {
        if (accessControl == null) {
            return null;
        }

        return new AccessControlResponse(
            new AccessControlResponse.AccessPermissionResponse(
                accessControl.getRead() != null ? accessControl.getRead().getGroupIds() : List.of(),
                accessControl.getRead() != null ? accessControl.getRead().getUserIds() : List.of()
            ),
            new AccessControlResponse.AccessPermissionResponse(
                accessControl.getWrite() != null ? accessControl.getWrite().getGroupIds() : List.of(),
                accessControl.getWrite() != null ? accessControl.getWrite().getUserIds() : List.of()
            )
        );
    }

    public record KnowledgeDataResponse(
        @JsonProperty("file_ids") List<String> fileIds
    ) {}

    public record KnowledgeMetaResponse() {}

    public record AccessControlResponse(
        AccessPermissionResponse read,
        AccessPermissionResponse write
    ) {
        public record AccessPermissionResponse(
            @JsonProperty("group_ids") List<Long> groupIds,
            @JsonProperty("user_ids") List<Long> userIds
        ) {}
    }

    public record UserResponse(
        String id,
        String name,
        String email,
        String role,
        @JsonProperty("profile_image_url") String profileImageUrl
    ) {}

    public record FileResponse(
        String id,
        String hash,
        FileMetaResponse meta,
        @JsonProperty("created_at") Long createdAt,
        @JsonProperty("updated_at") Long updatedAt
    ) {
        public record FileMetaResponse(
            String name,
            @JsonProperty("content_type") String contentType,
            Long size,
            Object data,
            @JsonProperty("collection_name") String collectionName
        ) {}
    }
}