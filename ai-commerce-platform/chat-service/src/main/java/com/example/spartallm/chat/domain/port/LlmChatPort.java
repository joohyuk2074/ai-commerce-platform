package com.example.spartallm.chat.domain.port;

import com.example.spartallm.chat.domain.model.LlmModelInfo;
import com.example.spartallm.chat.domain.model.LlmRequestMessage;
import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import reactor.core.publisher.Flux;

import java.util.List;

public interface LlmChatPort {

    boolean supports(String modelName);

    LlmResponseMessage chat(LlmRequestMessage message);

    Flux<LlmResponseMessage> chatStream(LlmRequestMessage message);

    List<LlmModelInfo> getSupportedModels();
}