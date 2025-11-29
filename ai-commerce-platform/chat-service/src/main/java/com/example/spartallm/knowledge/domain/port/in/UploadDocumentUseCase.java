package com.example.spartallm.knowledge.domain.port.in;

import com.example.spartallm.knowledge.application.dto.command.UploadDocumentCommand;
import com.example.spartallm.knowledge.application.dto.result.UploadDocumentResult;

/**
 * 문서 업로드 유스케이스
 */
public interface UploadDocumentUseCase {

    /**
     * 파일을 업로드하고 문서로 처리합니다.
     *
     * @param command 문서 업로드 커맨드
     * @return 업로드 결과
     */
    UploadDocumentResult execute(UploadDocumentCommand command);
}