package com.example.spartallm.knowledge.domain.port.in;

import com.example.spartallm.knowledge.adapter.in.web.dto.response.KnowledgeResponse;

import java.util.List;

public interface KnowledgeQueryUseCase {

    KnowledgeResponse getById(Long id);

    List<KnowledgeResponse> getListByUserId(Long userId);
}