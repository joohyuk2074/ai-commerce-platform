package com.example.spartallm.knowledge.adapter.out.persistence.repository;

import com.example.spartallm.knowledge.domain.model.FileMetadata;
import com.example.spartallm.knowledge.domain.repository.FileMetadataRepository;
import com.example.spartallm.knowledge.adapter.out.persistence.entity.FileMetadataJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FileMetadataRepositoryImpl implements FileMetadataRepository {

    private final FileMetadataJpaRepository fileMetadataJpaRepository;

    @Override
    public FileMetadata save(FileMetadata fileMetadata) {
        FileMetadataJpaEntity entity = FileMetadataJpaEntity.from(fileMetadata);
        FileMetadataJpaEntity saved = fileMetadataJpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<FileMetadata> findById(String id) {
        return fileMetadataJpaRepository.findById(id)
            .map(FileMetadataJpaEntity::toDomain);
    }

    @Override
    public Optional<FileMetadata> findByUserId(String userId) {
        return fileMetadataJpaRepository.findByUserId(userId)
            .map(FileMetadataJpaEntity::toDomain);
    }
}