package com.example.spartallm.knowledge.infrastructure.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ConfigurationProperties(prefix = "spring.ai.vectorstore")
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(
        DataSource dataSource,
        @Qualifier("customOllamaEmbedding") EmbeddingModel embeddingModel
    ) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // PgVectorStore 생성
        // mxbai-embed-large 모델의 임베딩 차원: 1024
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
            .dimensions(1024)
            .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
            .indexType(PgVectorStore.PgIndexType.HNSW)
            .initializeSchema(false)
            .removeExistingVectorStoreTable(false)
            .vectorTableValidationsEnabled(true)
            .schemaName("public")
            .vectorTableName("vector_store")
            .build();
    }
}