package com.example.spartallm.knowledge.adapter.out.persistence.entity;

import com.example.spartallm.knowledge.domain.model.AccessControl;
import com.example.spartallm.knowledge.domain.model.FileMetadata;
import com.example.spartallm.knowledge.domain.vo.FileData;
import com.example.spartallm.knowledge.domain.vo.DocumentMeta;
import com.example.spartallm.knowledge.adapter.out.persistence.converter.AccessControlConverter;
import com.example.spartallm.knowledge.adapter.out.persistence.converter.FileDataConverter;
import com.example.spartallm.knowledge.adapter.out.persistence.converter.FileMetaConverter;
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
@Table(name = "file_metadata")
public class FileMetadataJpaEntity {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(length = 64)
    private String hash;

    @Column(nullable = false, length = 500)
    private String filename;

    @Convert(converter = FileDataConverter.class)
    @Column(columnDefinition = "TEXT")
    private FileData data;

    @Convert(converter = FileMetaConverter.class)
    @Column(columnDefinition = "TEXT")
    private DocumentMeta meta;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean status;

    @Column(nullable = false, length = 1000)
    private String path;

    @Convert(converter = AccessControlConverter.class)
    @Column(name = "access_control", columnDefinition = "TEXT")
    private AccessControl accessControl;

    public static FileMetadataJpaEntity from(FileMetadata fileMetadata) {
        return new FileMetadataJpaEntity(
            fileMetadata.getId(),
            fileMetadata.getUserId(),
            fileMetadata.getHash(),
            fileMetadata.getFilename(),
            fileMetadata.getData(),
            fileMetadata.getMeta(),
            fileMetadata.getCreatedAt(),
            fileMetadata.getUpdatedAt(),
            fileMetadata.getStatus(),
            fileMetadata.getPath(),
            fileMetadata.getAccessControl()
        );
    }

    public FileMetadata toDomain() {
        return FileMetadata.builder()
            .id(this.id)
            .userId(this.userId)
            .hash(this.hash)
            .filename(this.filename)
            .data(this.data)
            .meta(this.meta)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .status(this.status)
            .path(this.path)
            .accessControl(this.accessControl)
            .build();
    }
}