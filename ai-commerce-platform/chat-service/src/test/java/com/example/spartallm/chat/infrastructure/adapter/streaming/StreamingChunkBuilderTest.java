package com.example.spartallm.chat.infrastructure.adapter.streaming;

import com.example.spartallm.chat.infrastructure.adapter.converter.ChunkJsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StreamingChunkBuilder 단위 테스트")
class StreamingChunkBuilderTest {

    private StreamingChunkBuilder streamingChunkBuilder;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        ChunkJsonSerializer chunkSerializer = new ChunkJsonSerializer(objectMapper);
        streamingChunkBuilder = new StreamingChunkBuilder(chunkSerializer);
    }

    @Test
    @DisplayName("토큰 스트림을 청크로 변환 - 청크 개수 검증")
    void buildChunks_ReturnsCorrectNumberOfChunks() {
        // given
        String requestId = "test-request-id";
        String modelName = "test-model";
        Flux<String> tokenFlux = Flux.just("안녕", "하세요", "!");

        // when
        Flux<String> chunkFlux = streamingChunkBuilder.buildChunks(requestId, modelName, tokenFlux);

        // then: 첫 청크(role) + 토큰 3개 + 마지막 청크(finish) + [DONE] = 6개
        StepVerifier.create(chunkFlux)
            .expectNextCount(6)
            .verifyComplete();
    }

    @Test
    @DisplayName("첫 번째 청크는 role을 포함")
    void buildChunks_FirstChunkContainsRole() {
        // given
        String requestId = "test-request-id";
        String modelName = "test-model";
        Flux<String> tokenFlux = Flux.just("Hello");

        // when
        Flux<String> chunkFlux = streamingChunkBuilder.buildChunks(requestId, modelName, tokenFlux);

        // then
        StepVerifier.create(chunkFlux)
            .assertNext(firstChunk -> {
                assertThat(firstChunk).contains("\"role\":\"assistant\"");
                assertThat(firstChunk).contains("\"object\":\"chat.completion.chunk\"");
                assertThat(firstChunk).contains("test-request-id");
                assertThat(firstChunk).contains("test-model");
            })
            .expectNextCount(3)  // 나머지 청크들
            .verifyComplete();
    }

    @Test
    @DisplayName("중간 청크들은 content를 포함")
    void buildChunks_MiddleChunksContainContent() {
        // given
        String requestId = "test-request-id";
        String modelName = "test-model";
        Flux<String> tokenFlux = Flux.just("안녕", "하세요");

        // when
        Flux<String> chunkFlux = streamingChunkBuilder.buildChunks(requestId, modelName, tokenFlux);

        List<String> chunks = chunkFlux.collectList().block();

        // then
        assertThat(chunks).isNotNull();
        assertThat(chunks).hasSize(5);  // role + 2개 content + finish + [DONE]

        // 두 번째 청크 (첫 번째 content)
        assertThat(chunks.get(1)).contains("\"content\":\"안녕\"");

        // 세 번째 청크 (두 번째 content)
        assertThat(chunks.get(2)).contains("\"content\":\"하세요\"");
    }

    @Test
    @DisplayName("마지막 청크는 finish_reason을 포함")
    void buildChunks_LastChunkContainsFinishReason() {
        // given
        String requestId = "test-request-id";
        String modelName = "test-model";
        Flux<String> tokenFlux = Flux.just("Hello");

        // when
        Flux<String> chunkFlux = streamingChunkBuilder.buildChunks(requestId, modelName, tokenFlux);

        List<String> chunks = chunkFlux.collectList().block();

        // then
        assertThat(chunks).isNotNull();
        assertThat(chunks).hasSize(4);  // role + content + finish + [DONE]

        // finish_reason 청크 (마지막에서 두 번째)
        String finishChunk = chunks.get(chunks.size() - 2);
        assertThat(finishChunk).contains("\"finish_reason\":\"stop\"");
    }

    @Test
    @DisplayName("최종 청크는 [DONE] 마커")
    void buildChunks_EndsWithDoneMarker() {
        // given
        String requestId = "test-request-id";
        String modelName = "test-model";
        Flux<String> tokenFlux = Flux.just("Hello");

        // when
        Flux<String> chunkFlux = streamingChunkBuilder.buildChunks(requestId, modelName, tokenFlux);

        // then
        StepVerifier.create(chunkFlux)
            .expectNextCount(3)  // role, content, finish
            .expectNext("[DONE]")
            .verifyComplete();
    }

    @Test
    @DisplayName("빈 토큰 스트림 처리")
    void buildChunks_WithEmptyTokenFlux_ReturnsRoleAndFinishChunks() {
        // given
        String requestId = "test-request-id";
        String modelName = "test-model";
        Flux<String> tokenFlux = Flux.empty();

        // when
        Flux<String> chunkFlux = streamingChunkBuilder.buildChunks(requestId, modelName, tokenFlux);

        // then: role + finish + [DONE] = 3개
        StepVerifier.create(chunkFlux)
            .expectNextCount(3)
            .verifyComplete();
    }

    @Test
    @DisplayName("스트리밍 완료 타임아웃 검증")
    void buildChunks_CompletesWithinTimeout() {
        // given
        String requestId = "test-request-id";
        String modelName = "test-model";
        Flux<String> tokenFlux = Flux.just("1", "2", "3", "4", "5");

        // when
        Flux<String> chunkFlux = streamingChunkBuilder.buildChunks(requestId, modelName, tokenFlux);

        // then: 1초 내에 완료되어야 함
        StepVerifier.create(chunkFlux)
            .expectNextCount(8)  // role + 5 content + finish + [DONE]
            .expectComplete()
            .verify(Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("모든 청크가 동일한 requestId와 modelName을 포함")
    void buildChunks_AllChunksContainSameRequestIdAndModel() {
        // given
        String requestId = "unique-request-123";
        String modelName = "gpt-4";
        Flux<String> tokenFlux = Flux.just("A", "B");

        // when
        Flux<String> chunkFlux = streamingChunkBuilder.buildChunks(requestId, modelName, tokenFlux);

        List<String> chunks = chunkFlux.collectList().block();

        // then
        assertThat(chunks).isNotNull();

        // [DONE]을 제외한 모든 청크
        chunks.stream()
            .filter(chunk -> !"[DONE]".equals(chunk))
            .forEach(chunk -> {
                assertThat(chunk).contains("unique-request-123");
                assertThat(chunk).contains("gpt-4");
            });
    }
}