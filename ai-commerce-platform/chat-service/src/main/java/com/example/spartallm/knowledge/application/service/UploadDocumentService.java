package com.example.spartallm.knowledge.application.service;

import com.example.spartallm.knowledge.application.dto.command.UploadDocumentCommand;
import com.example.spartallm.knowledge.application.dto.result.UploadDocumentResult;
import com.example.spartallm.knowledge.domain.model.DocumentChunk;
import com.example.spartallm.knowledge.domain.port.in.UploadDocumentUseCase;
import com.example.spartallm.knowledge.domain.port.out.DocumentChunkPort;
import com.example.spartallm.knowledge.domain.port.out.DocumentCommandPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadDocumentService implements UploadDocumentUseCase {

    private final VectorStore vectorStore;
    private final DocumentChunkPort documentChunkPort;
    private final DocumentCommandPort documentCommandPort;

    @Override
    public UploadDocumentResult execute(UploadDocumentCommand command) {
        // 1. 파일 내용 읽기
        String content = new String(command.content(), StandardCharsets.UTF_8);

        // 2. 파일 청킹
        List<DocumentChunk> documentChunks = documentChunkPort.chunk(
            command.originalFileName(),
            content
        );

        // 3. 파일 메타정보 & 청킹 데이터 저장
        // TODO: 구현 필요
        return null;
    }
}