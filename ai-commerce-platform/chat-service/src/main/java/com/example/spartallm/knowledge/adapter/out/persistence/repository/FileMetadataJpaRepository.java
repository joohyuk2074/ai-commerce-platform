package com.example.spartallm.knowledge.adapter.out.persistence.repository;

import com.example.spartallm.knowledge.adapter.out.persistence.entity.FileMetadataJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileMetadataJpaRepository extends JpaRepository<FileMetadataJpaEntity, String> {

    Optional<FileMetadataJpaEntity> findByUserId(String userId);
}