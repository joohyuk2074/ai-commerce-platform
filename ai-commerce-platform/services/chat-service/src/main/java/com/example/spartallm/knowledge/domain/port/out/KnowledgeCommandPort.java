package com.example.spartallm.knowledge.domain.port.out;

import com.example.spartallm.knowledge.domain.model.Knowledge;

public interface KnowledgeCommandPort {

    Knowledge save(Knowledge knowledge);

    void deleteById(Long id);
}