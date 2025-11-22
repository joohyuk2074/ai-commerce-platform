package com.example.spartallm.chat.infrastructure.adapter.converter;

import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonConverter {

    private final ObjectMapper objectMapper;

    public LlmResponseMessage parseResponse(String json) {
        try {
            return objectMapper.readValue(json, LlmResponseMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse LlmResponseMessage from json: {}", json, e);
            throw new RuntimeException("Failed to parse LlmResponseMessage", e);
        }
    }
}