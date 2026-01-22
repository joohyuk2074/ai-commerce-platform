package com.example.spartallm.knowledge.adapter.out.persistence.mapper;

import com.example.spartallm.knowledge.adapter.out.persistence.entity.KnowledgeDocumentChunkJpaEntity;
import com.example.spartallm.knowledge.adapter.out.persistence.entity.KnowledgeDocumentJpaEntity;
import com.example.spartallm.knowledge.domain.model.KnowledgeDocument;
import com.example.spartallm.knowledge.domain.model.KnowledgeDocumentChunk;
import com.example.spartallm.knowledge.domain.vo.ChunkMetadata;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KnowledgeDocumentMapper {

    public KnowledgeDocumentJpaEntity toJpaEntity(KnowledgeDocument domain) {
        KnowledgeDocumentJpaEntity entity = KnowledgeDocumentJpaEntity.builder()
            .id(domain.getId())
            .knowledgeId(domain.getKnowledgeId())
            .filename(domain.getFilename())
            .size(domain.getSize())
            .contentType(domain.getContentType())
            .status(domain.getStatus())
            .build();

        // 청크 변환 및 양방향 관계 설정
        domain.getChunks().forEach(chunk -> {
            KnowledgeDocumentChunkJpaEntity chunkEntity = toChunkJpaEntity(chunk);
            entity.addChunk(chunkEntity);
        });

        return entity;
    }

    public KnowledgeDocument toDomain(KnowledgeDocumentJpaEntity entity) {
        List<KnowledgeDocumentChunk> chunks = entity.getChunks().stream()
            .map(this::toChunkDomain)
            .toList();

        return KnowledgeDocument.reconstitute(
            entity.getId(),
            entity.getKnowledgeId(),
            entity.getFilename(),
            entity.getSize(),
            entity.getContentType(),
            entity.getStatus(),
            chunks
        );
    }

    private KnowledgeDocumentChunkJpaEntity toChunkJpaEntity(KnowledgeDocumentChunk chunk) {
        return KnowledgeDocumentChunkJpaEntity.builder()
            .id(chunk.getId())
            .chunkIndex(chunk.getChunkIndex())
            .text(chunk.getText())
            .metadata(chunk.getMetadata().metadata())
            .build();
    }

    private KnowledgeDocumentChunk toChunkDomain(
        KnowledgeDocumentChunkJpaEntity entity
    ) {
        return KnowledgeDocumentChunk.reconstitute(
            entity.getId(),
            entity.getDocument() != null ? entity.getDocument().getId() : null,
            entity.getChunkIndex(),
            entity.getText(),
            ChunkMetadata.from(entity.getMetadata())
        );
    }
}