package com.example.spartallm.knowledge.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class DocumentJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String contentType;  // PDF, DOCX, TXT

    @CreationTimestamp
    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    private Integer chunkCount;  // 이 문서가 몇 개의 청크로 나뉘었는지

    @Column(columnDefinition = "TEXT")
    private String metadata;  // JSON 형태의 메타데이터

    @Transient
    @Builder.Default
    private boolean isNew = true;
}
