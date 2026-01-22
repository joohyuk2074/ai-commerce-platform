package com.example.spartallm.knowledge.application.dto.command;

public record SearchCommand(
    String query,
    int topK
) {
    public static SearchCommand of(String query, int topK) {
        return new SearchCommand(query, topK);
    }

    public static SearchCommand of(String query) {
        return new SearchCommand(query, 5); // 기본값 5개
    }
}