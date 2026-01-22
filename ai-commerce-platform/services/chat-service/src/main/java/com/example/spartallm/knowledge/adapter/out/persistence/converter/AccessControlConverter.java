package com.example.spartallm.knowledge.adapter.out.persistence.converter;

import com.example.spartallm.knowledge.domain.model.AccessControl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class AccessControlConverter implements AttributeConverter<AccessControl, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(AccessControl attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert AccessControl to JSON", e);
            throw new IllegalArgumentException("Failed to convert AccessControl to JSON", e);
        }
    }

    @Override
    public AccessControl convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return AccessControl.createDefault();
        }

        try {
            return objectMapper.readValue(dbData, AccessControl.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to AccessControl", e);
            return AccessControl.createDefault();
        }
    }
}