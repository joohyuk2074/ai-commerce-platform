package com.example.spartallm.knowledge.domain.port;

import com.example.spartallm.knowledge.domain.entity.Knowledge;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class FakeKnowledgeCommandPort implements KnowledgeCommandPort {

    private final Map<Long, Knowledge> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Knowledge save(Knowledge knowledge) {
        if (knowledge.getId() == null) {
            Long newId = idGenerator.getAndIncrement();
            Knowledge knowledgeWithId = Knowledge.builder()
                .id(newId)
                .userId(knowledge.getUserId())
                .name(knowledge.getName())
                .description(knowledge.getDescription())
                .data(knowledge.getData())
                .meta(knowledge.getMeta())
                .accessControl(knowledge.getAccessControl())
                .createdAt(knowledge.getCreatedAt())
                .updatedAt(knowledge.getUpdatedAt())
                .build();
            storage.put(newId, knowledgeWithId);
            return knowledgeWithId;
        } else {
            storage.put(knowledge.getId(), knowledge);
            return knowledge;
        }
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    public Knowledge findById(Long id) {
        return storage.get(id);
    }

    public void clear() {
        storage.clear();
    }

    public int size() {
        return storage.size();
    }
}