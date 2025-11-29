package com.example.spartallm.knowledge.domain.port.in;

import com.example.spartallm.knowledge.adapter.in.web.dto.response.KnowledgeResponse;

import java.util.List;

/**
 * 지식 조회 유스케이스
 * CQRS 패턴의 Query 측면을 담당합니다.
 */
public interface KnowledgeQueryUseCase {

    /**
     * 지식을 ID로 조회합니다.
     *
     * @param id 지식 ID
     * @return 지식 정보
     */
    KnowledgeResponse getById(Long id);

    /**
     * 사용자의 지식 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 지식 목록
     */
    List<KnowledgeResponse> getListByUserId(Long userId);
}