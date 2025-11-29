package com.example.spartallm.knowledge.domain.port;

import com.example.spartallm.knowledge.domain.entity.Knowledge;

public interface KnowledgeCommandPort {

    Knowledge save(Knowledge knowledge);

    void deleteById(Long id);
}