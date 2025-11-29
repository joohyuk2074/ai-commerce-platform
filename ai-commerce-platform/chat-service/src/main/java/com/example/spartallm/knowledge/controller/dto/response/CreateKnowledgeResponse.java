package com.example.spartallm.knowledge.controller.dto.response;

import com.example.spartallm.knowledge.application.dto.result.CreateKnowledgeResult;
import com.example.spartallm.knowledge.domain.entity.AccessControl;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.ZoneOffset;
import java.util.List;

@Builder
public record CreateKnowledgeResponse(
    Long id,

    @JsonProperty("user_id")
    Long userId,

    String name,

    String description,

    String data,

    Object meta,

    @JsonProperty("access_control")
    AccessControlDto accessControl,

    @JsonProperty("created_at")
    Long createdAt,

    @JsonProperty("updated_at")
    Long updatedAt,

    Object files
) {
    public static CreateKnowledgeResponse from(CreateKnowledgeResult result) {
        return CreateKnowledgeResponse.builder()
            .id(result.id())
            .userId(result.userId())
            .name(result.name())
            .description(result.description())
            .data(result.data())
            .meta(result.meta())
            .accessControl(AccessControlDto.from(result.accessControl()))
            .createdAt(result.createdAt() != null
                ? result.createdAt().toEpochSecond(ZoneOffset.UTC)
                : null)
            .updatedAt(result.updatedAt() != null
                ? result.updatedAt().toEpochSecond(ZoneOffset.UTC)
                : null)
            .files(null)
            .build();
    }

    public record AccessControlDto(
        AccessPermissionDto read,
        AccessPermissionDto write
    ) {
        public static AccessControlDto from(AccessControl accessControl) {
            if (accessControl == null) {
                return null;
            }

            return new AccessControlDto(
                AccessPermissionDto.from(accessControl.getRead()),
                AccessPermissionDto.from(accessControl.getWrite())
            );
        }
    }

    public record AccessPermissionDto(
        @JsonProperty("group_ids")
        List<Long> groupIds,

        @JsonProperty("user_ids")
        List<Long> userIds
    ) {
        public static AccessPermissionDto from(AccessControl.AccessPermission permission) {
            if (permission == null) {
                return new AccessPermissionDto(List.of(), List.of());
            }

            return new AccessPermissionDto(
                permission.getGroupIds(),
                permission.getUserIds()
            );
        }
    }
}