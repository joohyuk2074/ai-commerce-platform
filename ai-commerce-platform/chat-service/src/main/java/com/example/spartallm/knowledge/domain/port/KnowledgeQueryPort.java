package com.example.spartallm.knowledge.domain.port;

import com.example.spartallm.knowledge.domain.entity.Knowledge;

import java.util.List;
import java.util.Optional;

public interface KnowledgeQueryPort {

    boolean existsById(Long id);

    Optional<Knowledge> findById(Long id);

    Knowledge getById(Long id);

    List<Knowledge> findAllByUserId(Long userId);
}
