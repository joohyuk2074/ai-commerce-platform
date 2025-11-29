package com.example.spartallm.knowledge.adapter.out.persistence.converter;

import com.example.spartallm.knowledge.domain.vo.DocumentMeta;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class FileMetaConverter implements AttributeConverter<DocumentMeta, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(DocumentMeta attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert FileMeta to JSON", e);
        }
    }

    @Override
    public DocumentMeta convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, DocumentMeta.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to FileMeta", e);
        }
    }
}