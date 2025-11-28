package com.example.spartallm.knowledge.domain.port.out;

import com.example.spartallm.knowledge.domain.model.KnowledgeDocumentChunk;

import java.util.List;

public interface DocumentChunkPort {

    List<KnowledgeDocumentChunk> chunk(String filename, String content);
}
