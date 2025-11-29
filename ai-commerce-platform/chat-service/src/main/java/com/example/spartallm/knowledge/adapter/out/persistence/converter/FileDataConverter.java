package com.example.spartallm.knowledge.adapter.out.persistence.converter;

import com.example.spartallm.knowledge.domain.vo.FileData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class FileDataConverter implements AttributeConverter<FileData, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(FileData attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert FileData to JSON", e);
        }
    }

    @Override
    public FileData convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, FileData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to FileData", e);
        }
    }
}