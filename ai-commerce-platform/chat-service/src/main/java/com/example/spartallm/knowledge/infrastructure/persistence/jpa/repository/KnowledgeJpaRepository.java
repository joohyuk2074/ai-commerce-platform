package com.example.spartallm.knowledge.infrastructure.persistence.jpa.repository;

import com.example.spartallm.knowledge.infrastructure.persistence.jpa.entity.KnowledgeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeJpaRepository extends JpaRepository<KnowledgeJpaEntity, Long> {

    List<KnowledgeJpaEntity> findByUserId(Long userId);
}