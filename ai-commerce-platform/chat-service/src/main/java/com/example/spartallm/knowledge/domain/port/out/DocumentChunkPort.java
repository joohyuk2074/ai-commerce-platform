package com.example.spartallm.knowledge.domain.port.out;

import com.example.spartallm.knowledge.domain.model.DocumentChunk;

import java.util.List;

public interface DocumentChunkPort {

    List<DocumentChunk> chunk(String filename, String content);
}
