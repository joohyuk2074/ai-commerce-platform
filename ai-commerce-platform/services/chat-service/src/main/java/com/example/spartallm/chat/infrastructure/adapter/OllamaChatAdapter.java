package com.example.spartallm.chat.infrastructure.adapter;

import com.example.spartallm.chat.domain.model.ChatOptions;
import com.example.spartallm.chat.infrastructure.adapter.mapper.LlmResponseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component("ollamaChatAdapter")
public class OllamaChatAdapter extends AbstractLlmChatAdapter {

    public OllamaChatAdapter(
        List<OllamaChatModel> ollamaChatModels,
        LlmResponseMapper responseMapper
    ) {
        super(
            initializeChatClients(ollamaChatModels),
            responseMapper
        );
        log.info("OllamaChatAdapter initialized with {} models", chatClients.size());
    }

    private static Map<String, ChatClient> initializeChatClients(List<OllamaChatModel> models) {
        return models.stream()
            .collect(Collectors.toMap(
                model -> model.getDefaultOptions().getModel(),
                ChatClient::create,
                (m1, m2) -> m1
            ));
    }

    @Override
    protected OllamaChatOptions buildChatOptions(ChatOptions options) {
        return OllamaChatOptions.builder()
            .temperature(options.temperature())
            .numPredict(options.maxTokens())
            .topK(options.topK())
            .topP(options.topP())
            .build();
    }

    @Override
    protected String getProviderName() {
        return "ollama";
    }
}