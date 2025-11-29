package com.example.spartallm.knowledge.infrastructure.persistence.jpa.repository;

import com.example.spartallm.knowledge.domain.entity.Knowledge;
import com.example.spartallm.knowledge.domain.port.KnowledgeCommandPort;
import com.example.spartallm.knowledge.infrastructure.persistence.jpa.entity.KnowledgeJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class KnowledgeRepositoryImpl implements KnowledgeCommandPort {

    private final KnowledgeJpaRepository knowledgeJpaRepository;

    @Override
    public Knowledge save(Knowledge knowledge) {
        KnowledgeJpaEntity entity = KnowledgeJpaEntity.from(knowledge);
        KnowledgeJpaEntity savedEntity = knowledgeJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public void deleteById(Long id) {
        knowledgeJpaRepository.deleteById(id);
    }

    // Query Port methods (not in KnowledgeCommandPort interface)
    public Optional<Knowledge> findById(Long id) {
        return knowledgeJpaRepository.findById(id)
            .map(KnowledgeJpaEntity::toDomain);
    }

    public Knowledge getById(Long id) {
        return knowledgeJpaRepository.findById(id)
            .map(KnowledgeJpaEntity::toDomain)
            .orElseThrow(() -> new IllegalArgumentException("Knowledge not found: " + id));
    }

    public List<Knowledge> findByUserId(Long userId) {
        return knowledgeJpaRepository.findByUserId(userId).stream()
            .map(KnowledgeJpaEntity::toDomain)
            .toList();
    }

    public boolean existsById(Long id) {
        return knowledgeJpaRepository.existsById(id);
    }
}