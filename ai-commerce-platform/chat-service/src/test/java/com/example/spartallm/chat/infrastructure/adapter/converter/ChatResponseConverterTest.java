package com.example.spartallm.chat.infrastructure.adapter.converter;

import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ChatResponseConverter 단위 테스트")
class ChatResponseConverterTest {

    private ChatResponseConverter chatResponseConverter;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        chatResponseConverter = new ChatResponseConverter(objectMapper);
    }

    @Test
    @DisplayName("ChatResponse를 choices ArrayNode로 변환")
    void convertChoices_Success() {
        // given
        AssistantMessage assistantMessage = new AssistantMessage("안녕하세요");
        ChatGenerationMetadata metadata = ChatGenerationMetadata.builder()
            .finishReason("stop")
            .build();

        Generation generation = new Generation(assistantMessage, metadata);
        ChatResponse chatResponse = new ChatResponse(List.of(generation));

        // when
        ArrayNode choices = chatResponseConverter.convertChoices(chatResponse);

        // then
        assertThat(choices).isNotNull();
        assertThat(choices).hasSize(1);

        assertThat(choices.get(0).get("index").asInt()).isEqualTo(0);
        assertThat(choices.get(0).get("finish_reason").asText()).isEqualTo("stop");
        assertThat(choices.get(0).get("message").get("role").asText()).isEqualTo("ASSISTANT");
        assertThat(choices.get(0).get("message").get("content").asText()).isEqualTo("안녕하세요");
    }

    @Test
    @DisplayName("여러 Generation을 choices로 변환")
    void convertChoices_WithMultipleGenerations_Success() {
        // given
        Generation gen1 = new Generation(
            new AssistantMessage("First response"),
            ChatGenerationMetadata.builder().finishReason("stop").build()
        );
        Generation gen2 = new Generation(
            new AssistantMessage("Second response"),
            ChatGenerationMetadata.builder().finishReason("stop").build()
        );

        ChatResponse chatResponse = new ChatResponse(List.of(gen1, gen2));

        // when
        ArrayNode choices = chatResponseConverter.convertChoices(chatResponse);

        // then
        assertThat(choices).hasSize(2);

        assertThat(choices.get(0).get("index").asInt()).isEqualTo(0);
        assertThat(choices.get(0).get("message").get("content").asText()).isEqualTo("First response");

        assertThat(choices.get(1).get("index").asInt()).isEqualTo(1);
        assertThat(choices.get(1).get("message").get("content").asText()).isEqualTo("Second response");
    }

    @Test
    @DisplayName("ChatResponse Usage를 LlmUsage로 변환")
    void convertUsage_Success() {
        // given
        Usage usage = mock(Usage.class);
        when(usage.getPromptTokens()).thenReturn(100);
        when(usage.getCompletionTokens()).thenReturn(50);
        when(usage.getTotalTokens()).thenReturn(150);

        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        when(metadata.getUsage()).thenReturn(usage);

        ChatResponse chatResponse = mock(ChatResponse.class);
        when(chatResponse.getMetadata()).thenReturn(metadata);

        // when
        LlmResponseMessage.LlmUsage llmUsage = chatResponseConverter.convertUsage(chatResponse);

        // then
        assertThat(llmUsage.promptTokens()).isEqualTo(100);
        assertThat(llmUsage.completionTokens()).isEqualTo(50);
        assertThat(llmUsage.totalTokens()).isEqualTo(150);
    }

    @Test
    @DisplayName("빈 Generation 리스트 처리")
    void convertChoices_WithEmptyGenerations_ReturnsEmptyArray() {
        // given
        ChatResponse chatResponse = new ChatResponse(List.of());

        // when
        ArrayNode choices = chatResponseConverter.convertChoices(chatResponse);

        // then
        assertThat(choices).isNotNull();
        assertThat(choices).isEmpty();
    }

    @Test
    @DisplayName("다양한 finish_reason 처리")
    void convertChoices_WithDifferentFinishReasons_Success() {
        // given
        Generation gen1 = new Generation(
            new AssistantMessage("Response 1"),
            ChatGenerationMetadata.builder().finishReason("stop").build()
        );
        Generation gen2 = new Generation(
            new AssistantMessage("Response 2"),
            ChatGenerationMetadata.builder().finishReason("length").build()
        );

        ChatResponse chatResponse = new ChatResponse(List.of(gen1, gen2));

        // when
        ArrayNode choices = chatResponseConverter.convertChoices(chatResponse);

        // then
        assertThat(choices.get(0).get("finish_reason").asText()).isEqualTo("stop");
        assertThat(choices.get(1).get("finish_reason").asText()).isEqualTo("length");
    }
}