package com.example.spartallm.knowledge.domain.entity;

import com.example.spartallm.knowledge.domain.vo.KnowledgeMeta;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Knowledge {

    private Long id;

    private Long userId;

    private String name;

    private String description;

    private String data;

    private KnowledgeMeta meta;

    private AccessControl accessControl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static Knowledge createNew(
        Long userId,
        String name,
        String description,
        AccessControl accessControl
    ) {
        return Knowledge.builder()
            .userId(userId)
            .name(name)
            .description(description)
            .accessControl(accessControl != null ? accessControl : AccessControl.createDefault())
            .meta(KnowledgeMeta.defaultMeta())
            .build();
    }

    public void update(String name, String description) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        if (description != null && !description.isBlank()) {
            this.description = description;
        }
    }

    public void updateData(String data) {
        this.data = data;
    }

    public void updateMeta(KnowledgeMeta meta) {
        this.meta = meta;
    }
}