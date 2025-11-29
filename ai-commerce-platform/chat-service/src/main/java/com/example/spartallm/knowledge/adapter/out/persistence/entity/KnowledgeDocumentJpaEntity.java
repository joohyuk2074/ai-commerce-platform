package com.example.spartallm.knowledge.adapter.out.persistence.entity;

import com.example.spartallm.knowledge.domain.vo.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "knowledge_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class KnowledgeDocumentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long knowledgeId;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<KnowledgeDocumentChunkJpaEntity> chunks = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * 양방향 관계 편의 메서드
     */
    public void addChunk(KnowledgeDocumentChunkJpaEntity chunk) {
        this.chunks.add(chunk);
        chunk.setDocument(this);
    }

    /**
     * 상태 변경
     */
    public void changeStatus(DocumentStatus newStatus) {
        this.status = newStatus;
    }
}