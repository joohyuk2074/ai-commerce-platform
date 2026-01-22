package com.example.spartallm.knowledge.domain.command;

import com.example.spartallm.knowledge.domain.model.AccessControl;

public record CreateKnowledgeCommand(
    Long userId,
    String name,
    String description,
    AccessControl accessControl
) {
}