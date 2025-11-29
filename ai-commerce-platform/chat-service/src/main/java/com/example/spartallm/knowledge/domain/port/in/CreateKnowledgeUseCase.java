package com.example.spartallm.knowledge.domain.port.in;

import com.example.spartallm.knowledge.application.dto.result.CreateKnowledgeResult;
import com.example.spartallm.knowledge.domain.command.CreateKnowledgeCommand;

/**
 * 지식 생성 유스케이스
 */
public interface CreateKnowledgeUseCase {

    /**
     * 새로운 지식을 생성합니다.
     *
     * @param command 지식 생성 커맨드
     * @return 생성된 지식 정보
     */
    CreateKnowledgeResult execute(CreateKnowledgeCommand command);
}