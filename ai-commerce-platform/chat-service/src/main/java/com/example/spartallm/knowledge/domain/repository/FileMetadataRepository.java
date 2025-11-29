package com.example.spartallm.knowledge.domain.repository;

import com.example.spartallm.knowledge.domain.model.FileMetadata;

import java.util.Optional;

public interface FileMetadataRepository {

    FileMetadata save(FileMetadata fileMetadata);

    Optional<FileMetadata> findById(String id);

    Optional<FileMetadata> findByUserId(String userId);
}