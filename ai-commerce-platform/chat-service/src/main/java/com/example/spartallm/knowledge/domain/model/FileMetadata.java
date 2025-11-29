package com.example.spartallm.knowledge.domain.model;

import com.example.spartallm.knowledge.domain.vo.FileData;
import com.example.spartallm.knowledge.domain.vo.DocumentMeta;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileMetadata {

    private String id;

    private String userId;

    private String hash;

    private String filename;

    private FileData data;

    private DocumentMeta meta;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean status;

    private String path;

    private AccessControl accessControl;

    public static FileMetadata createNew(
        String id,
        String userId,
        String filename,
        DocumentMeta meta,
        String path
    ) {
        return FileMetadata.builder()
            .id(id)
            .userId(userId)
            .filename(filename)
            .data(FileData.pending())
            .meta(meta)
            .path(path)
            .status(true)
            .build();
    }

    public void updateStatus(FileData data) {
        this.data = data;
    }

    public void updateHash(String hash) {
        this.hash = hash;
    }
}