package com.example.spartallm.knowledge.domain.vo;

import java.util.HashMap;
import java.util.Map;

public record ChunkMetadata(
    Map<String, Object> metadata
) {

    public static ChunkMetadata from(Map<String, Object> chunkMetadata) {
        return new ChunkMetadata(new HashMap<>(chunkMetadata));
    }
}
