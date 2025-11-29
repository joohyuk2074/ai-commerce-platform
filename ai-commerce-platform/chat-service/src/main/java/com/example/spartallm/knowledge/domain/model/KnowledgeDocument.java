package com.example.spartallm.knowledge.domain.model;

import com.example.spartallm.knowledge.domain.vo.DocumentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeDocument {
    private Long id;

    private Long knowledgeId;  // FK

    private String filename;

    private Long size;

    private String contentType;

    private DocumentStatus status;

    private List<KnowledgeDocumentChunk> chunks;

    public static KnowledgeDocument create(
        Long knowledgeId,
        String filename,
        long size,
        String contentType
    ) {
        return new KnowledgeDocument(
            null,
            knowledgeId,
            filename,
            size,
            contentType,
            DocumentStatus.UPLOADING,
            new ArrayList<>()
        );
    }

    public static KnowledgeDocument reconstitute(
        Long id,
        Long knowledgeId,
        String filename,
        Long size,
        String contentType,
        DocumentStatus status,
        List<KnowledgeDocumentChunk> chunks
    ) {
        return new KnowledgeDocument(
            id,
            knowledgeId,
            filename,
            size,
            contentType,
            status,
            chunks != null ? chunks : new ArrayList<>()
        );
    }

    public void addChunk(String text, Map<String, Object> metadata) {
        validateCanAddChunk();

        KnowledgeDocumentChunk chunk = KnowledgeDocumentChunk.create(
            this.chunks.size(),
            text,
            metadata
        );
        this.chunks.add(chunk);
    }

    public void addChunks(List<KnowledgeDocumentChunk> newChunks) {
        validateCanAddChunk();

        if (newChunks == null || newChunks.isEmpty()) {
            return;
        }

        this.chunks.addAll(newChunks);
    }

    public void startProcessing() {
        if (this.chunks.isEmpty()) {
            throw new IllegalStateException("청크가 없는 문서는 처리할 수 없습니다");
        }
        this.status = DocumentStatus.PROCESSING;
    }

    public void completeProcessing() {
        if (this.status != DocumentStatus.PROCESSING) {
            throw new IllegalStateException("처리 중인 문서만 완료 처리할 수 있습니다");
        }
        this.status = DocumentStatus.COMPLETED;
    }

    public void fail() {
        this.status = DocumentStatus.FAILED;
    }

    private void validateCanAddChunk() {
        if (this.status == DocumentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 문서에는 청크를 추가할 수 없습니다");
        }
        if (this.status == DocumentStatus.FAILED) {
            throw new IllegalStateException("실패한 문서에는 청크를 추가할 수 없습니다");
        }
    }

    public List<KnowledgeDocumentChunk> getChunks() {
        return Collections.unmodifiableList(chunks);
    }

    public int getTotalChunks() {
        return this.chunks.size();
    }
}
