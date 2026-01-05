package com.example.spartallm.knowledge.domain.port.in;

import com.example.spartallm.knowledge.application.dto.result.CreateKnowledgeResult;
import com.example.spartallm.knowledge.domain.command.CreateKnowledgeCommand;

public interface CreateKnowledgeUseCase {

    CreateKnowledgeResult execute(CreateKnowledgeCommand command);
}