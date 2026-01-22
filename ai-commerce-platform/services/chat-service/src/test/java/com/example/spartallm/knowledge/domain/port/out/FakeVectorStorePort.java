package com.example.spartallm.knowledge.domain.port.out;

import com.example.spartallm.chat.domain.model.DocumentSource;
import com.example.spartallm.knowledge.application.dto.command.SearchCommand;
import com.example.spartallm.knowledge.domain.model.KnowledgeDocumentChunk;

import java.util.List;

public class FakeVectorStorePort implements VectorStorePort {

    @Override
    public void saveChunks(List<KnowledgeDocumentChunk> chunks) {

    }

    @Override
    public List<DocumentSource> similaritySearch(SearchCommand searchCommand) {
        return List.of();
    }

    @Override
    public void deleteByDocumentId(Long documentId) {

    }
}