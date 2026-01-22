package com.example.spartallm.knowledge.domain.vo;

import java.util.Map;

public record KnowledgeMeta(
    Map<String, Object> attributes
) {

    public static KnowledgeMeta defaultMeta() {
        return new KnowledgeMeta(Map.of());
    }
}
