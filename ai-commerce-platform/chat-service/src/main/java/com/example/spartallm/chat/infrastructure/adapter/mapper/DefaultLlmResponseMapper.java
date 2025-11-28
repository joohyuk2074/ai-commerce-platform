package com.example.spartallm.chat.infrastructure.adapter.mapper;

import com.example.spartallm.chat.domain.model.LlmRequestMessage;
import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import com.example.spartallm.chat.infrastructure.adapter.converter.ChatResponseConverter;
import com.example.spartallm.chat.infrastructure.adapter.converter.JsonConverter;
import com.example.spartallm.chat.infrastructure.adapter.streaming.StreamingChunkBuilder;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultLlmResponseMapper implements LlmResponseMapper {

    private final ChatResponseConverter chatResponseConverter;
    private final StreamingChunkBuilder streamingChunkBuilder;
    private final JsonConverter jsonConverter;

    @Override
    public LlmResponseMessage mapSyncResponse(
        LlmRequestMessage requestMessage,
        ChatClient chatClient,
        Object options
    ) {
        log.debug("Mapping sync response for model: {}", requestMessage.modelName());

        // ChatClient 호출
        // options는 Provider별 ChatOptions (OllamaChatOptions, AnthropicChatOptions 등)
        ChatResponse chatResponse = chatClient
            .prompt(requestMessage.systemPrompt())
            .user(requestMessage.userMessage())
            .options((org.springframework.ai.chat.prompt.ChatOptions) options)
            .call()
            .chatResponse();

        // 응답 변환
        ArrayNode choices = chatResponseConverter.convertChoices(chatResponse);
        LlmResponseMessage.LlmUsage usage = chatResponseConverter.convertUsage(chatResponse);

        return LlmResponseMessage.syncResponse(
            requestMessage.requestId(),
            "chat.completion",
            requestMessage.modelName(),
            choices,
            usage,
            System.currentTimeMillis()
        );
    }

    @Override
    public Flux<LlmResponseMessage> mapStreamResponse(
        LlmRequestMessage requestMessage,
        ChatClient chatClient,
        Object options
    ) {
        log.debug("Mapping stream response for model: {}", requestMessage.modelName());

        ChatClient.ChatClientRequestSpec promptSpec = chatClient.prompt();

        if (StringUtils.hasText(requestMessage.systemPrompt())) {
            promptSpec = promptSpec.system(requestMessage.systemPrompt());
        }

        Flux<String> tokenFlux = promptSpec
            .user(requestMessage.userMessage())
            .options((org.springframework.ai.chat.prompt.ChatOptions) options)
            .stream()
            .content();

        // 청크 조립
        Flux<String> chunkJsonFlux = streamingChunkBuilder.buildChunks(
            requestMessage.requestId(),
            requestMessage.modelName(),
            tokenFlux
        );

        // JSON 파싱 및 [DONE] 필터링
        return chunkJsonFlux
            .filter(chunk -> !"[DONE]".equals(chunk))
            .map(jsonConverter::parseResponse);
    }
}