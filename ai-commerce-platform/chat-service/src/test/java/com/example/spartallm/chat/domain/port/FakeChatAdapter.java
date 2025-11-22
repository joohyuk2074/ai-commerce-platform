package com.example.spartallm.chat.domain.port;

import com.example.spartallm.chat.domain.model.LlmModelInfo;
import com.example.spartallm.chat.domain.model.LlmRequestMessage;
import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class FakeChatAdapter implements LlmChatPort {

    private final Map<String, String> modelNameToChatModelMap = new ConcurrentHashMap<>();

    public void init() {
        IntStream.range(0, 3)
            .forEach(i -> {
                modelNameToChatModelMap.put("model-" + i, "model-" + i);
            });
    }

    @Override
    public boolean supports(String modelName) {
        return modelNameToChatModelMap.containsKey(modelName);
    }

    @Override
    public LlmResponseMessage chat(LlmRequestMessage message) {
        LlmResponseMessage.LlmUsage llmUsage = new LlmResponseMessage.LlmUsage(100, 100, 200);

        return LlmResponseMessage.syncResponse(
            message.requestId(),
            "test-object",
            message.modelName(),
            null,
            llmUsage,
            0L
        );
    }

    @Override
    public Flux<LlmResponseMessage> chatStream(LlmRequestMessage message) {
        // Fake: 스트리밍 청크 시뮬레이션
        return Flux.just(
            // 첫 번째 청크 (role)
            createStreamChunk(message.requestId(), message.modelName(), "role", null),
            // 콘텐츠 청크들
            createStreamChunk(message.requestId(), message.modelName(), "content", "안녕"),
            createStreamChunk(message.requestId(), message.modelName(), "content", "하세요"),
            createStreamChunk(message.requestId(), message.modelName(), "content", "!"),
            // 마지막 청크 (finish)
            createStreamChunk(message.requestId(), message.modelName(), "finish", null)
        );
    }

    /**
     * Helper: 스트리밍 청크 생성
     */
    private LlmResponseMessage createStreamChunk(String id, String model, String type, String content) {
        LlmResponseMessage.LlmUsage usage = new LlmResponseMessage.LlmUsage(0, 0, 0);
        return LlmResponseMessage.syncResponse(
            id,
            "chat.completion.chunk",
            model,
            null,
            usage,
            System.currentTimeMillis()
        );
    }

    @Override
    public List<LlmModelInfo> getSupportedModels() {
        return modelNameToChatModelMap.keySet().stream()
            .map(modelName -> new LlmModelInfo(modelName, "joohyuk", 0L))
            .toList();
    }
}