package com.example.spartallm.knowledge.domain.command;

import com.example.spartallm.knowledge.domain.entity.AccessControl;

public record CreateKnowledgeCommand(
    Long userId,
    String name,
    String description,
    AccessControl accessControl
) {
}