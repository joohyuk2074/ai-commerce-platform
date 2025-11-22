package com.example.spartallm.chat.application;

import com.example.spartallm.chat.application.dto.query.ChatQuery;
import com.example.spartallm.chat.application.dto.result.ChatResult;
import com.example.spartallm.chat.application.dto.result.LlmModelListResult;
import com.example.spartallm.chat.domain.port.FakeChatAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ChatService 단위 테스트")
class ChatServiceTest {

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        FakeChatAdapter fakeChatAdapter = new FakeChatAdapter();
        fakeChatAdapter.init();

        chatService = new ChatService(List.of(fakeChatAdapter));
    }

    @Test
    @DisplayName("지원 모델 목록 조회 성공")
    void getLlmList_Success() {
        // when
        LlmModelListResult result = chatService.getLlmList();

        // then
        assertThat(result.models()).isNotEmpty();
        assertThat(result.models()).hasSize(3);
    }

    @Test
    @DisplayName("지원하지 않는 모델로 요청 시 예외 발생")
    void chatSync_WithUnsupportedModel_ThrowsException() {
        // given
        ChatQuery chatQuery = createChatQuery(
            "unsupported-model",
            "system prompt message",
            "Hello"
        );

        // when & then
        assertThatThrownBy(() -> chatService.chatSync(chatQuery))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid model name");
    }

    @Test
    @DisplayName("동기식 질의 요청")
    void chatSync_Success() {
        // given
        ChatQuery chatQuery = createChatQuery(
            "model-1",
            "시스템 프롬프트",
            "안녕하세요"
        );

        // when
        ChatResult chatResult = chatService.chatSync(chatQuery);

        // then
        assertThat(chatResult.id()).isNotBlank();
        assertThat(chatResult.model()).isEqualTo(chatQuery.model());
        assertThat(chatResult.object()).isNotBlank();
    }

    @Test
    @DisplayName("스트리밍 질의 요청 - 청크 개수 검증")
    void chatStream_ReturnsMultipleChunks() {
        // given
        ChatQuery chatQuery = createChatQuery(
            "model-1",
            "시스템 프롬프트",
            "스트리밍 테스트"
        );

        // when
        Flux<ChatResult> resultFlux = chatService.chatStream(chatQuery);

        // then
        StepVerifier.create(resultFlux)
            .expectNextCount(5)
            .verifyComplete();
    }

    @Test
    @DisplayName("스트리밍 질의 요청 - 각 청크 내용 검증")
    void chatStream_ChunksContainCorrectData() {
        // given
        ChatQuery chatQuery = createChatQuery(
            "model-1",
            "시스템 프롬프트",
            "안녕하세요"
        );

        // when
        Flux<ChatResult> resultFlux = chatService.chatStream(chatQuery);

        // then: 각 청크가 ChatResult 타입인지, 모델명이 일치하는지 검증
        StepVerifier.create(resultFlux)
            .expectNextMatches(chunk -> {
                if (chunk instanceof ChatResult result) {
                    return result.model().equals("model-1") && result.object().equals("chat.completion.chunk");
                }
                return false;
            })
            .expectNextMatches(Objects::nonNull)
            .expectNextMatches(Objects::nonNull)
            .expectNextMatches(Objects::nonNull)
            .expectNextMatches(Objects::nonNull)
            .verifyComplete();
    }

    @Test
    @DisplayName("스트리밍 질의 요청 - 타임아웃 검증")
    void chatStream_CompletesWithinTimeout() {
        // given
        ChatQuery chatQuery = createChatQuery(
            "model-1",
            "시스템 프롬프트",
            "타임아웃 테스트"
        );

        // when
        Flux<?> resultFlux = chatService.chatStream(chatQuery);

        // then:
        StepVerifier.create(resultFlux)
            .expectNextCount(5)
            .expectComplete()
            .verify(Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("스트리밍 질의 요청 - 지원하지 않는 모델")
    void chatStream_WithUnsupportedModel_ThrowsException() {
        // given
        ChatQuery chatQuery = createChatQuery(
            "unsupported-model",
            "시스템 프롬프트",
            "Hello"
        );

        // when & then
        assertThatThrownBy(() -> chatService.chatStream(chatQuery))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid model name");
    }

    private ChatQuery createChatQuery(String model, String systemPrompt, String message) {
        return new ChatQuery(
            model,
            List.of(
                new ChatQuery.ChatMessageQuery("system", systemPrompt),
                new ChatQuery.ChatMessageQuery("user", message)
            ),
            0.7,
            500,
            false
        );
    }
}