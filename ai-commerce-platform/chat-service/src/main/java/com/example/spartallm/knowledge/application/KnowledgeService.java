package com.example.spartallm.knowledge.application;

import com.example.spartallm.knowledge.application.dto.result.CreateKnowledgeResult;
import com.example.spartallm.knowledge.controller.dto.response.KnowledgeResponse;
import com.example.spartallm.knowledge.domain.command.CreateKnowledgeCommand;
import com.example.spartallm.knowledge.domain.entity.Knowledge;
import com.example.spartallm.knowledge.domain.port.KnowledgeCommandPort;
import com.example.spartallm.knowledge.domain.port.KnowledgeQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeCommandPort knowledgeCommandPort;
    private final KnowledgeQueryPort knowledgeQueryPort;

    @Transactional
    public CreateKnowledgeResult createKnowledge(CreateKnowledgeCommand command) {
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

    @Transactional(readOnly = true)
    public KnowledgeResponse getKnowledge(Long id) {
        log.info("Fetching knowledge - id: {}", id);

        Knowledge knowledge = knowledgeQueryPort.getById(id);

        log.info("Knowledge found - id: {}, name: {}", knowledge.getId(), knowledge.getName());

        return KnowledgeResponse.from(knowledge);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeResponse> getKnowledgeList(Long userId) {
        log.info("Fetching knowledge list from external API");

        List<Knowledge> knowledgeList = knowledgeQueryPort.findAllByUserId(userId);

        return knowledgeList.stream()
            .map(KnowledgeResponse::from)
            .toList();
    }
}