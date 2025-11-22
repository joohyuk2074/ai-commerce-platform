package com.example.spartallm.chat.application;

import com.example.spartallm.chat.application.dto.query.ChatQuery;
import com.example.spartallm.chat.application.dto.result.ChatResult;
import com.example.spartallm.chat.application.dto.result.LlmModelListResult;
import com.example.spartallm.chat.domain.model.LlmModelInfo;
import com.example.spartallm.chat.domain.model.LlmRequestMessage;
import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import com.example.spartallm.chat.domain.port.LlmChatPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final List<LlmChatPort> llmChatPorts;

    public ChatResult chatSync(ChatQuery chatQuery) {
        LlmChatPort selectedPort = selectLlmChatPort(chatQuery.model());

        LlmRequestMessage llmRequestMessage = chatQuery.toChatMessage();

        LlmResponseMessage llmResponseMessage = selectedPort.chat(llmRequestMessage);

        log.info("응답 완료 - 모델: {}", llmResponseMessage.getModel());

        return ChatResult.of(llmResponseMessage);
    }

    public Flux<ChatResult> chatStream(ChatQuery chatQuery) {
        LlmChatPort selectedPort = selectLlmChatPort(chatQuery.model());

        LlmRequestMessage llmRequestMessage = chatQuery.toChatMessage();

        Flux<LlmResponseMessage> llmResponseMessageFlux = selectedPort.chatStream(llmRequestMessage);

        return llmResponseMessageFlux.map(ChatResult::of);
    }

    public LlmModelListResult getLlmList() {
        List<LlmModelInfo> allModels = llmChatPorts.stream()
            .flatMap(port -> port.getSupportedModels().stream())
            .toList();

        log.info("지원 모델 수: {}", allModels.size());

        return LlmModelListResult.of(allModels);
    }

    private LlmChatPort selectLlmChatPort(String modelName) {
        for (LlmChatPort llmChatPort : llmChatPorts) {
            if (llmChatPort.supports(modelName)) {
                return llmChatPort;
            }
        }

        throw new IllegalArgumentException("Invalid model name: " + modelName);
    }
}

