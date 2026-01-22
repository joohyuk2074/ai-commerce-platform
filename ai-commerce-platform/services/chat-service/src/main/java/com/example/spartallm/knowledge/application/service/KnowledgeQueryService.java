package com.example.spartallm.knowledge.application.service;

import com.example.spartallm.knowledge.adapter.in.web.dto.response.KnowledgeResponse;
import com.example.spartallm.knowledge.domain.model.Knowledge;
import com.example.spartallm.knowledge.domain.port.in.KnowledgeQueryUseCase;
import com.example.spartallm.knowledge.domain.port.out.KnowledgeQueryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeQueryService implements KnowledgeQueryUseCase {

    private final KnowledgeQueryPort knowledgeQueryPort;

    @Override
    public KnowledgeResponse getById(Long id) {
        log.info("Fetching knowledge - id: {}", id);

        Knowledge knowledge = knowledgeQueryPort.getById(id);

        log.info("Knowledge found - id: {}, name: {}", knowledge.getId(), knowledge.getName());

        return KnowledgeResponse.from(knowledge);
    }

    @Override
    public List<KnowledgeResponse> getListByUserId(Long userId) {
        log.info("Fetching knowledge list for userId: {}", userId);

        List<Knowledge> knowledgeList = knowledgeQueryPort.findAllByUserId(userId);

        return knowledgeList.stream()
            .map(KnowledgeResponse::from)
            .toList();
    }
}