package com.example.spartallm.knowledge.adapter.out.persistence.entity;

import com.example.spartallm.knowledge.domain.model.AccessControl;
import com.example.spartallm.knowledge.domain.model.Knowledge;
import com.example.spartallm.knowledge.domain.vo.KnowledgeMeta;
import com.example.spartallm.knowledge.adapter.out.persistence.converter.AccessControlConverter;
import com.example.spartallm.knowledge.adapter.out.persistence.converter.KnowledgeMetaConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@DynamicInsert
@DynamicUpdate
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "knowledge")
public class KnowledgeJpaEntity {

    @Id
    @Column(length = 36, nullable = false)
    private Long id;

    @Column(name = "user_id", length = 36, nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String data;

    @Convert(converter = KnowledgeMetaConverter.class)
    @Column(columnDefinition = "TEXT")
    private KnowledgeMeta meta;

    @Convert(converter = AccessControlConverter.class)
    @Column(name = "access_control", columnDefinition = "TEXT")
    private AccessControl accessControl;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static KnowledgeJpaEntity from(Knowledge knowledge) {
        return new KnowledgeJpaEntity(
            knowledge.getId(),
            knowledge.getUserId(),
            knowledge.getName(),
            knowledge.getDescription(),
            knowledge.getData(),
            knowledge.getMeta(),
            knowledge.getAccessControl(),
            knowledge.getCreatedAt(),
            knowledge.getUpdatedAt()
        );
    }

    public Knowledge toDomain() {
        return Knowledge.builder()
            .id(this.id)
            .userId(this.userId)
            .name(this.name)
            .description(this.description)
            .data(this.data)
            .meta(this.meta)
            .accessControl(this.accessControl)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}