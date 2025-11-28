package com.example.spartallm.knowledge.domain.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentMeta {

    private String name;
    private String contentType;
    private Long size;
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    public static DocumentMeta of(String name, String contentType, Long size) {
        return DocumentMeta.builder()
            .name(name)
            .contentType(contentType)
            .size(size)
            .data(new HashMap<>())
            .build();
    }

    public static DocumentMeta defaultMeta() {
        return DocumentMeta.builder()
            .data(new HashMap<>())
            .build();
    }
}