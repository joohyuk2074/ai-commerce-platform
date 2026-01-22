package com.example.spartallm.chat.infrastructure.adapter.converter;

import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JsonConverter 단위 테스트")
class JsonConverterTest {

    private JsonConverter jsonConverter;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        jsonConverter = new JsonConverter(objectMapper);
    }

    @Test
    @DisplayName("유효한 JSON을 LlmResponseMessage로 파싱")
    void parseResponse_WithValidJson_Success() {
        // given
        String json = """
            {
                "id": "test-id",
                "object": "chat.completion",
                "created": 1234567890,
                "model": "gpt-4",
                "choices": [
                    {
                        "index": 0,
                        "message": {
                            "role": "assistant",
                            "content": "Hello"
                        },
                        "finishReason": "stop"
                    }
                ],
                "usage": {
                    "promptTokens": 10,
                    "completionTokens": 5,
                    "totalTokens": 15
                }
            }
            """;

        // when
        LlmResponseMessage response = jsonConverter.parseResponse(json);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("test-id");
        assertThat(response.getObject()).isEqualTo("chat.completion");
        assertThat(response.getCreated()).isEqualTo(1234567890L);
        assertThat(response.getModel()).isEqualTo("gpt-4");
        assertThat(response.getUsage().promptTokens()).isEqualTo(10);
        assertThat(response.getUsage().completionTokens()).isEqualTo(5);
        assertThat(response.getUsage().totalTokens()).isEqualTo(15);
    }

    @Test
    @DisplayName("스트리밍 청크 JSON 파싱")
    void parseResponse_WithStreamingChunk_Success() {
        // given
        String chunkJson = """
            {
                "id": "chunk-id",
                "object": "chat.completion.chunk",
                "created": 1234567890,
                "model": "gpt-4",
                "choices": [
                    {
                        "index": 0,
                        "delta": {
                            "content": "안녕"
                        },
                        "finish_reason": null
                    }
                ]
            }
            """;

        // when
        LlmResponseMessage response = jsonConverter.parseResponse(chunkJson);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("chunk-id");
        assertThat(response.getObject()).isEqualTo("chat.completion.chunk");
    }

    @Test
    @DisplayName("잘못된 JSON 형식은 예외 발생")
    void parseResponse_WithInvalidJson_ThrowsException() {
        // given
        String invalidJson = "{ invalid json }";

        // when & then
        assertThatThrownBy(() -> jsonConverter.parseResponse(invalidJson))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to parse LlmResponseMessage");
    }

    @Test
    @DisplayName("빈 JSON은 예외 발생")
    void parseResponse_WithEmptyString_ThrowsException() {
        // given
        String emptyJson = "";

        // when & then
        assertThatThrownBy(() -> jsonConverter.parseResponse(emptyJson))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("null JSON은 예외 발생")
    void parseResponse_WithNull_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> jsonConverter.parseResponse(null))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("필드 누락 시에도 null로 처리되어 파싱 성공")
    void parseResponse_WithMissingFields_ParsesWithNullValues() {
        // given - usage 필드 누락
        String jsonWithoutUsage = """
            {
                "id": "test-id",
                "object": "chat.completion",
                "created": 1234567890,
                "model": "gpt-4"
            }
            """;

        // when
        LlmResponseMessage response = jsonConverter.parseResponse(jsonWithoutUsage);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("test-id");
        assertThat(response.getUsage()).isNull();  // 누락된 필드는 null
    }
}