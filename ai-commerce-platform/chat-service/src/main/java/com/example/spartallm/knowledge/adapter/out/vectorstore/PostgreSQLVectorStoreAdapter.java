package com.example.spartallm.knowledge.adapter.out.vectorstore;

import com.example.spartallm.chat.domain.model.DocumentSource;
import com.example.spartallm.knowledge.domain.model.KnowledgeDocumentChunk;
import com.example.spartallm.knowledge.application.dto.command.SearchCommand;
import com.example.spartallm.knowledge.domain.port.out.VectorStorePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PostgreSQL pgvector를 사용하는 VectorStore Adapter
 * Spring AI의 VectorStore를 사용하여 임베딩 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostgreSQLVectorStoreAdapter implements VectorStorePort {

    private final VectorStore vectorStore;

    @Override
    public void saveChunks(List<KnowledgeDocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            log.warn("저장할 청크가 없습니다");
            return;
        }

        List<Document> documents = chunks.stream()
            .map(this::toDocument)
            .toList();


        vectorStore.add(documents);

        log.info("배치 저장 완료: {} 개 청크", documents.size());
    }

    @Override
    public List<DocumentSource> similaritySearch(SearchCommand searchCommand) {
        SearchRequest searchRequest = SearchRequest.builder()
            .query(searchCommand.query())
            .topK(searchCommand.topK())
            .build();

        List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);

        return relevantDocs.stream()
            .map(this::toDocumentSource)
            .toList();
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        if (documentId == null) {
            log.warn("삭제할 documentId가 null입니다");
            return;
        }
//
//        try {
//            // documentId로 필터링하여 검색
//            FilterExpressionBuilder filterBuilder = new FilterExpressionBuilder();
//            Filter filter = filterBuilder.eq("documentId", documentId.toString()).build();
//
//            SearchRequest searchRequest = SearchRequest.query("")
//                .withFilterExpression(filter)
//                .withTopK(1000); // 충분히 큰 값으로 설정
//
//            List<Document> documentsToDelete = vectorStore.similaritySearch(searchRequest);
//
//            // 각 문서 ID로 삭제
//            for (Document doc : documentsToDelete) {
//                vectorStore.delete(List.of(doc.getId()));
//            }
//
//            log.info("Vector Store에서 documentId={} 청크 {} 개 삭제 완료",
//                documentId, documentsToDelete.size());
//
//        } catch (Exception e) {
//            log.error("Vector Store 삭제 실패: documentId={}", documentId, e);
//            throw new RuntimeException("벡터 DB 삭제에 실패했습니다", e);
//        }
    }

    /**
     * KnowledgeDocumentChunk를 Spring AI Document로 변환
     */
    private Document toDocument(KnowledgeDocumentChunk chunk) {
        Map<String, Object> metadata = new HashMap<>();

        // 도메인 정보 추가
        metadata.put("chunkIndex", chunk.getChunkIndex());
        metadata.put("knowledgeDocumentId", chunk.getDocumentId());

        // 기존 메타데이터 병합
        if (chunk.getMetadata() != null && chunk.getMetadata().metadata() != null) {
            metadata.putAll(chunk.getMetadata().metadata());
        }

        // chunk ID를 Document ID로 사용 (재구성 시 필요)
        String documentId = UUID.randomUUID().toString();

        return new Document(documentId, chunk.getText(), metadata);
    }

    /**
     * 임시 청크 ID 생성 (ID가 없는 경우)
     */
    private String generateChunkId(KnowledgeDocumentChunk chunk) {
        return String.format("%d-%d", chunk.getDocumentId(), chunk.getChunkIndex());
    }

    /**
     * Spring AI Document를 DocumentSource로 변환
     */
    private DocumentSource toDocumentSource(Document document) {
        Map<String, Object> metadata = document.getMetadata();

        Long knowledgeDocumentId = null;
        Integer chunkIndex = null;

        if (metadata != null) {
            Object docIdObj = metadata.get("knowledgeDocumentId");
            if (docIdObj != null) {
                knowledgeDocumentId = Long.valueOf(docIdObj.toString());
            }

            Object chunkIdxObj = metadata.get("chunkIndex");
            if (chunkIdxObj != null) {
                chunkIndex = Integer.valueOf(chunkIdxObj.toString());
            }
        }

        return DocumentSource.of(
            document.getText(),
            knowledgeDocumentId,
            chunkIndex,
            metadata
        );
    }
}