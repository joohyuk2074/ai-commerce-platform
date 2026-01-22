package com.example.spartallm.knowledge.adapter.out.persistence;

import com.example.spartallm.knowledge.adapter.out.persistence.entity.KnowledgeDocumentJpaEntity;
import com.example.spartallm.knowledge.adapter.out.persistence.mapper.KnowledgeDocumentMapper;
import com.example.spartallm.knowledge.adapter.out.persistence.repository.KnowledgeDocumentJpaRepository;
import com.example.spartallm.knowledge.domain.model.KnowledgeDocument;
import com.example.spartallm.knowledge.domain.port.out.DocumentCommandPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentPersistenceAdapter implements DocumentCommandPort {

    private final KnowledgeDocumentJpaRepository documentRepository;
    private final KnowledgeDocumentMapper mapper;

    @Override
    public KnowledgeDocument save(KnowledgeDocument document) {
        log.debug("문서 애그리게이트 저장: filename={}, chunks={}",
            document.getFilename(), document.getTotalChunks());

        KnowledgeDocumentJpaEntity jpaEntity = mapper.toJpaEntity(document);

        KnowledgeDocumentJpaEntity savedEntity = documentRepository.save(jpaEntity);

        return mapper.toDomain(savedEntity);
    }
}