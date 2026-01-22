package com.example.spartallm.chat.infrastructure.adapter.streaming;

import com.example.spartallm.chat.infrastructure.adapter.converter.ChunkJsonSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreamingChunkBuilder {

    private final ChunkJsonSerializer chunkSerializer;

    /**
     * 토큰 스트림을 청크 JSON 스트림으로 변환
     *
     * @param requestId 요청 ID
     * @param modelName 모델명
     * @param tokenFlux 토큰 스트림
     * @return 청크 JSON 스트림
     */
    public Flux<String> buildChunks(
        String requestId,
        String modelName,
        Flux<String> tokenFlux
    ) {
        long createdAt = System.currentTimeMillis();

        log.debug("Building streaming chunks for request: {}, model: {}", requestId, modelName);

        // 1) 첫 청크: role만 담긴 delta
        Flux<String> firstChunk = Flux.just(
            chunkSerializer.serialize(
                requestId,
                createdAt,
                modelName,
                Map.of("role", "assistant"),
                null
            )
        );

        // 2) 중간 청크들: delta.content에 토큰 조각
        Flux<String> contentChunks = tokenFlux.map(token ->
            chunkSerializer.serialize(
                requestId,
                createdAt,
                modelName,
                Map.of("content", token),
                null
            )
        );

        // 3) 마지막 청크: finish_reason = "stop"
        Flux<String> lastChunk = Flux.just(
            chunkSerializer.serialize(
                requestId,
                createdAt,
                modelName,
                Map.of(),
                "stop"
            ),
            "[DONE]"  // OpenAI 형식 호환
        );

        return Flux.concat(firstChunk, contentChunks, lastChunk);
    }
}