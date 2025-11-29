package com.example.spartallm.knowledge.application.dto.result;

import com.example.spartallm.knowledge.domain.entity.AccessControl;
import com.example.spartallm.knowledge.domain.entity.Knowledge;
import com.example.spartallm.knowledge.domain.vo.KnowledgeMeta;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CreateKnowledgeResult(
    Long id,
    Long userId,
    String name,
    String description,
    String data,
    KnowledgeMeta meta,
    AccessControl accessControl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CreateKnowledgeResult from(Knowledge knowledge) {
        return CreateKnowledgeResult.builder()
            .id(knowledge.getId())
            .userId(knowledge.getUserId())
            .name(knowledge.getName())
            .description(knowledge.getDescription())
            .data(knowledge.getData())
            .meta(knowledge.getMeta())
            .accessControl(knowledge.getAccessControl())
            .createdAt(knowledge.getCreatedAt())
            .updatedAt(knowledge.getUpdatedAt())
            .build();
    }
}