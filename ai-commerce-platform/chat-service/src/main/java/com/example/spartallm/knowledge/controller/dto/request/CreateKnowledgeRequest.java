package com.example.spartallm.knowledge.controller.dto.request;

import com.example.spartallm.knowledge.domain.command.CreateKnowledgeCommand;
import com.example.spartallm.knowledge.domain.entity.AccessControl;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateKnowledgeRequest(
    @NotBlank(message = "Name is required")
    String name,

    String description,

    @JsonProperty("access_control")
    AccessControlDto accessControl
) {
    public CreateKnowledgeCommand toCommand(Long userId) {
        AccessControl accessControlDomain = accessControl != null
            ? accessControl.toDomain()
            : AccessControl.createDefault();

        return new CreateKnowledgeCommand(
            userId,
            name,
            description,
            accessControlDomain
        );
    }

    public record AccessControlDto(
        AccessPermissionDto read,

        AccessPermissionDto write
    ) {
        public AccessControl toDomain() {
            AccessControl.AccessPermission readPermission = read != null
                ? read.toDomain()
                : new AccessControl.AccessPermission();

            AccessControl.AccessPermission writePermission = write != null
                ? write.toDomain()
                : new AccessControl.AccessPermission();

            return AccessControl.builder()
                .read(readPermission)
                .write(writePermission)
                .build();
        }
    }

    public record AccessPermissionDto(
        @JsonProperty("group_ids")
        List<Long> groupIds,

        @JsonProperty("user_ids")
        List<Long> userIds
    ) {
        public AccessControl.AccessPermission toDomain() {
            return AccessControl.AccessPermission.builder()
                .groupIds(groupIds != null ? groupIds : List.of())
                .userIds(userIds != null ? userIds : List.of())
                .build();
        }
    }
}