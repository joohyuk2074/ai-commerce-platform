package com.example.spartallm.knowledge.application.service;

import com.example.spartallm.knowledge.application.dto.result.CreateKnowledgeResult;
import com.example.spartallm.knowledge.domain.command.CreateKnowledgeCommand;
import com.example.spartallm.knowledge.domain.model.Knowledge;
import com.example.spartallm.knowledge.domain.port.in.CreateKnowledgeUseCase;
import com.example.spartallm.knowledge.domain.port.out.KnowledgeCommandPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateKnowledgeService implements CreateKnowledgeUseCase {

    private final KnowledgeCommandPort knowledgeCommandPort;

    @Override
    @Transactional
    public CreateKnowledgeResult execute(CreateKnowledgeCommand command) {
        log.info("Creating knowledge - name: {}, userId: {}", command.name(), command.userId());

        Knowledge knowledge = Knowledge.createNew(
            command.userId(),
            command.name(),
            command.description(),
            command.accessControl()
        );

        Knowledge savedKnowledge = knowledgeCommandPort.save(knowledge);

        log.info("Knowledge created successfully - id: {}", savedKnowledge.getId());

        return CreateKnowledgeResult.from(savedKnowledge);
    }
}