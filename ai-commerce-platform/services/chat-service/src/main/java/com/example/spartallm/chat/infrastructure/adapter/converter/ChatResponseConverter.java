package com.example.spartallm.chat.infrastructure.adapter.converter;

import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatResponseConverter {

    private final ObjectMapper objectMapper;

    public ArrayNode convertChoices(ChatResponse chatResponse) {
        List<Generation> results = chatResponse.getResults();

        log.debug("Converting {} generations to choices", results.size());

        ArrayNode choices = objectMapper.createArrayNode();

        for (int index = 0; index < results.size(); index++) {
            Generation generation = results.get(index);

            ObjectNode choice = objectMapper.createObjectNode();
            choice.put("index", index);

            ChatGenerationMetadata metadata = generation.getMetadata();
            String finishReason = metadata.getFinishReason();
            choice.put("finish_reason", finishReason);

            // message 생성
            ObjectNode message = createMessage(generation);
            choice.set("message", message);

            choices.add(choice);
        }

        return choices;
    }

    public LlmResponseMessage.LlmUsage convertUsage(ChatResponse chatResponse) {
        ChatResponseMetadata metadata = chatResponse.getMetadata();

        log.debug("Converting usage metadata: {}", metadata);

        Usage usage = metadata.getUsage();
        return new LlmResponseMessage.LlmUsage(
            usage.getPromptTokens(),
            usage.getCompletionTokens(),
            usage.getTotalTokens()
        );
    }

    private ObjectNode createMessage(Generation generation) {
        ObjectNode message = objectMapper.createObjectNode();

        AssistantMessage assistantMessage = generation.getOutput();

        String role = assistantMessage.getMessageType().name();
        message.put("role", role);

        String content = assistantMessage.getText();
        message.put("content", content);

        return message;
    }
}