package com.example.spartallm.knowledge.infrastructure.persistence.jpa.converter;

import com.example.spartallm.knowledge.domain.vo.KnowledgeMeta;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class KnowledgeMetaConverter implements AttributeConverter<KnowledgeMeta, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(KnowledgeMeta attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert KnowledgeMeta to JSON", e);
            throw new IllegalArgumentException("Failed to convert KnowledgeMeta to JSON", e);
        }
    }

    @Override
    public KnowledgeMeta convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return KnowledgeMeta.defaultMeta();
        }

        try {
            return objectMapper.readValue(dbData, KnowledgeMeta.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to KnowledgeMeta", e);
            return KnowledgeMeta.defaultMeta();
        }
    }
}