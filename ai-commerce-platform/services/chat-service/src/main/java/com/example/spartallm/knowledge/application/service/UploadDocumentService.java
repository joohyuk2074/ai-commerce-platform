package com.example.spartallm.knowledge.application.service;

import com.example.spartallm.knowledge.application.dto.command.UploadDocumentCommand;
import com.example.spartallm.knowledge.application.dto.result.UploadDocumentResult;
import com.example.spartallm.knowledge.domain.model.KnowledgeDocument;
import com.example.spartallm.knowledge.domain.model.KnowledgeDocumentChunk;
import com.example.spartallm.knowledge.domain.port.in.UploadDocumentUseCase;
import com.example.spartallm.knowledge.domain.port.out.DocumentChunkPort;
import com.example.spartallm.knowledge.domain.port.out.DocumentCommandPort;
import com.example.spartallm.knowledge.domain.port.out.VectorStorePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 문서 업로드 서비스
 * 애그리게이트 패턴에 따라 KnowledgeDocument를 통해 청크를 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadDocumentService implements UploadDocumentUseCase {

    private final VectorStorePort vectorStorePort;  // 벡터 DB 저장 포트
    private final DocumentChunkPort documentChunkPort;  // 도메인 서비스 (청킹 담당)
    private final DocumentCommandPort documentCommandPort;  // 영속화 포트

    @Override
    public UploadDocumentResult execute(UploadDocumentCommand command) {
        long startTime = System.currentTimeMillis();
        log.info("=== 문서 업로드 시작: {} ({}bytes) ===", command.originalFileName(), command.size());

        try {
            String content = new String(command.content(), StandardCharsets.UTF_8);

            KnowledgeDocument document = KnowledgeDocument.create(
                command.knowledgeId(),
                command.originalFileName(),
                command.size(),
                command.contentType()
            );

            // 1. 청킹
            long chunkStartTime = System.currentTimeMillis();
            List<KnowledgeDocumentChunk> chunks = documentChunkPort.chunk(
                command.originalFileName(),
                content
            );
            long chunkElapsed = System.currentTimeMillis() - chunkStartTime;
            log.info("[1/4] 문서 청킹 완료: {} -> {} chunks ({}ms)",
                command.originalFileName(), chunks.size(), chunkElapsed);

            // 2. 문서 저장
            document.addChunks(chunks);
            document.startProcessing();
            KnowledgeDocument savedDocument = documentCommandPort.save(document);
            log.info("[2/4] 문서 메타데이터 저장 완료: documentId={}", savedDocument.getId());

            // 3. 벡터 저장 (가장 시간이 오래 걸림)
            long vectorStartTime = System.currentTimeMillis();
            log.info("[3/4] VectorStore 저장 시작: {} chunks (임베딩 생성 중...)", savedDocument.getChunks().size());
            vectorStorePort.saveChunks(savedDocument.getChunks());
            long vectorElapsed = System.currentTimeMillis() - vectorStartTime;
            log.info("[3/4] VectorStore 저장 완료: {} chunks ({}ms, 평균 {}/chunk)",
                document.getTotalChunks(), vectorElapsed,
                vectorElapsed / Math.max(1, document.getTotalChunks()) + "ms");

            // 4. 상태 업데이트
            savedDocument.completeProcessing();
            documentCommandPort.save(savedDocument);
            log.info("[4/4] 문서 처리 상태 업데이트 완료");

            long totalElapsed = System.currentTimeMillis() - startTime;
            log.info("=== 문서 업로드 완료: documentId={}, chunks={}, 총 소요시간: {}ms ===",
                savedDocument.getId(), savedDocument.getTotalChunks(), totalElapsed);

            return UploadDocumentResult.from(savedDocument);

        } catch (Exception e) {
            long totalElapsed = System.currentTimeMillis() - startTime;
            log.error("=== 문서 업로드 실패: {} ({}ms 경과) ===", command.originalFileName(), totalElapsed, e);
            // TODO: 실패한 문서는 FAILED 상태로 저장
            throw new RuntimeException("문서 업로드에 실패했습니다", e);
        }
    }
}