package com.example.spartallm.knowledge.adapter.out.persistence.repository;

import com.example.spartallm.knowledge.adapter.out.persistence.entity.KnowledgeDocumentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentJpaRepository extends JpaRepository<KnowledgeDocumentJpaEntity, Long> {
}