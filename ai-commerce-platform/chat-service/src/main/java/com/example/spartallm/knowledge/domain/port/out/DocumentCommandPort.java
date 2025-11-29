package com.example.spartallm.knowledge.domain.port.out;

import com.example.spartallm.knowledge.domain.model.KnowledgeDocument;

public interface DocumentCommandPort {

    KnowledgeDocument save(KnowledgeDocument document);
}
