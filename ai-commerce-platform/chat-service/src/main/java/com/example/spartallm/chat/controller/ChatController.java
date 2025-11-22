package com.example.spartallm.chat.controller;

import com.example.spartallm.chat.application.ChatService;
import com.example.spartallm.chat.application.dto.query.ChatQuery;
import com.example.spartallm.chat.application.dto.result.ChatResult;
import com.example.spartallm.chat.application.dto.result.LlmModelListResult;
import com.example.spartallm.chat.controller.dto.request.ChatRequest;
import com.example.spartallm.chat.controller.dto.response.LlmModelListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/completions")
    public ResponseEntity<?> chat(@RequestBody @Valid ChatRequest chatRequest) {
        ChatQuery chatQuery = chatRequest.toChatQuery();

        log.info("채팅 요청 - 모델: {}, 메시지 수: {}, temp: {}, maxTokens: {}",
            chatQuery.model(), chatQuery.messages().size(),
            chatQuery.temperature(), chatQuery.maxTokens());

        if (chatRequest.stream()) {
            Flux<ChatResult> stream = chatService.chatStream(chatQuery);

            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(stream);
        } else {
            ChatResult chatResult = chatService.chatSync(chatQuery);
            return ResponseEntity.ok(chatResult);
        }
    }

    @GetMapping("/completions")
    public ResponseEntity<LlmModelListResponse> llmList() {
        LlmModelListResult llmModeListResult =  chatService.getLlmList();
        return ResponseEntity.ok(LlmModelListResponse.of(llmModeListResult));
    }
}