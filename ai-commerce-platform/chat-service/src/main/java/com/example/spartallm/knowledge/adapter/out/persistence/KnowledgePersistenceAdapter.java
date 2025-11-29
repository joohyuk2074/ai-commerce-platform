package com.example.spartallm.knowledge.adapter.out.persistence;

import com.example.spartallm.knowledge.adapter.out.persistence.entity.KnowledgeJpaEntity;
import com.example.spartallm.knowledge.adapter.out.persistence.repository.KnowledgeJpaRepository;
import com.example.spartallm.knowledge.domain.model.Knowledge;
import com.example.spartallm.knowledge.domain.port.out.KnowledgeCommandPort;
import com.example.spartallm.knowledge.domain.port.out.KnowledgeQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class KnowledgePersistenceAdapter implements KnowledgeCommandPort, KnowledgeQueryPort {

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

    @Override
    public boolean existsById(Long id) {
        return knowledgeJpaRepository.existsById(id);
    }

    @Override
    public Optional<Knowledge> findById(Long id) {
        return knowledgeJpaRepository.findById(id)
            .map(KnowledgeJpaEntity::toDomain);
    }

    @Override
    public Knowledge getById(Long id) {
        return knowledgeJpaRepository.findById(id)
            .map(KnowledgeJpaEntity::toDomain)
            .orElseThrow(() -> new IllegalArgumentException("Knowledge not found: " + id));
    }

    @Override
    public List<Knowledge> findAllByUserId(Long userId) {
        return knowledgeJpaRepository.findByUserId(userId).stream()
            .map(KnowledgeJpaEntity::toDomain)
            .toList();
    }
}
