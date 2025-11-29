package com.example.spartallm.knowledge.domain.port.out;

import com.example.spartallm.chat.domain.model.DocumentSource;
import com.example.spartallm.knowledge.domain.model.KnowledgeDocumentChunk;
import com.example.spartallm.knowledge.application.dto.command.SearchCommand;

import java.util.List;

public interface VectorStorePort {

    void saveChunks(List<KnowledgeDocumentChunk> chunks);

    List<DocumentSource> similaritySearch(SearchCommand searchCommand);

    void deleteByDocumentId(Long documentId);
}