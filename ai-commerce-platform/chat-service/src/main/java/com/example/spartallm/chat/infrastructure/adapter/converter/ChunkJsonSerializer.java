package com.example.spartallm.chat.infrastructure.adapter.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChunkJsonSerializer {

    private final ObjectMapper objectMapper;

    /**
     * 스트리밍 청크를 JSON 문자열로 직렬화
     *
     * @param id 요청 ID
     * @param created 생성 시간 (epoch)
     * @param model 모델명
     * @param deltaFields delta 객체에 포함될 필드들
     * @param finishReason 종료 이유 (null이면 진행 중)
     * @return JSON 문자열
     */
    public String serialize(
        String id,
        long created,
        String model,
        Map<String, Object> deltaFields,
        String finishReason
    ) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("id", id);
        root.put("object", "chat.completion.chunk");
        root.put("created", created);
        root.put("model", model);

        ArrayNode choices = root.putArray("choices");
        ObjectNode choice = choices.addObject();
        choice.put("index", 0);

        // delta 객체 생성
        ObjectNode delta = choice.putObject("delta");
        deltaFields.forEach((key, value) -> {
            if (value instanceof String s) {
                delta.put(key, s);
            } else if (value == null) {
                delta.putNull(key);
            }
        });

        // finish_reason 설정
        if (finishReason == null) {
            choice.putNull("finish_reason");
        } else {
            choice.put("finish_reason", finishReason);
        }

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize chunk json", e);
            throw new RuntimeException("Failed to serialize chunk json", e);
        }
    }
}