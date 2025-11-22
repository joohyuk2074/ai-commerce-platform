package com.example.spartallm.chat.infrastructure.adapter;

import com.example.spartallm.chat.domain.model.ChatOptions;
import com.example.spartallm.chat.domain.model.LlmModelInfo;
import com.example.spartallm.chat.domain.model.LlmRequestMessage;
import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import com.example.spartallm.chat.domain.port.LlmChatPort;
import com.example.spartallm.chat.infrastructure.adapter.mapper.LlmResponseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractLlmChatAdapter implements LlmChatPort {

    protected final Map<String, ChatClient> chatClients;
    protected final LlmResponseMapper responseMapper;

    protected AbstractLlmChatAdapter(
        Map<String, ChatClient> chatClients,
        LlmResponseMapper responseMapper
    ) {
        this.chatClients = chatClients;
        this.responseMapper = responseMapper;
    }

    @Override
    public boolean supports(String modelName) {
        return chatClients.containsKey(modelName);
    }

    @Override
    public List<LlmModelInfo> getSupportedModels() {
        return chatClients.keySet().stream()
            .map(modelId -> LlmModelInfo.of(modelId, getProviderName()))
            .toList();
    }

    @Override
    public LlmResponseMessage chat(LlmRequestMessage requestMessage) {
        ChatClient chatClient = getChatClient(requestMessage.modelName());

        log.debug("Executing sync chat for model: {}", requestMessage.modelName());

        return responseMapper.mapSyncResponse(
            requestMessage,
            chatClient,
            buildChatOptions(requestMessage.chatOptions())
        );
    }

    @Override
    public Flux<LlmResponseMessage> chatStream(LlmRequestMessage requestMessage) {
        ChatClient chatClient = getChatClient(requestMessage.modelName());

        log.debug("Executing stream chat for model: {}", requestMessage.modelName());

        return responseMapper.mapStreamResponse(
            requestMessage,
            chatClient,
            buildChatOptions(requestMessage.chatOptions())
        );
    }

    protected ChatClient getChatClient(String modelName) {
        ChatClient chatClient = chatClients.get(modelName);
        if (chatClient == null) {
            throw new IllegalArgumentException("Unsupported model: " + modelName);
        }
        return chatClient;
    }

    /**
     * Provider별 ChatOptions 변환 로직 (Template Method)
     * 하위 클래스에서 구현 필요
     *
     * @param options 도메인 ChatOptions
     * @return Provider별 ChatOptions (OllamaChatOptions, AnthropicChatOptions 등)
     */
    protected abstract Object buildChatOptions(ChatOptions options);

    /**
     * Provider 이름 반환 (Template Method)
     * 하위 클래스에서 구현 필요
     *
     * @return Provider 이름 (예: "ollama", "anthropic")
     */
    protected abstract String getProviderName();
}