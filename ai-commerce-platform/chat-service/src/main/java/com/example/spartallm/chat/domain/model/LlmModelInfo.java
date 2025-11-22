package com.example.spartallm.chat.domain.model;

public record LlmModelInfo(
    String id,
    String ownedBy,
    long created
) {
    public static LlmModelInfo of(String id, String ownedBy) {
        return new LlmModelInfo(id, ownedBy, System.currentTimeMillis());
    }

    public static LlmModelInfo of(String id, String ownedBy, long created) {
        return new LlmModelInfo(id, ownedBy, created);
    }
}