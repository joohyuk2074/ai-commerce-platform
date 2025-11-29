package com.example.spartallm.knowledge.infrastructure.client;

import com.example.spartallm.knowledge.controller.dto.response.KnowledgeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenWebUIClient {

    private final WebClient openWebUIWebClient;

    public List<KnowledgeResponse> getKnowledgeList() {
        log.info("Fetching knowledge list from OpenWebUI");

        return openWebUIWebClient
            .get()
            .uri("/api/v1/knowledge/list")
            .retrieve()
            .bodyToFlux(KnowledgeResponse.class)
            .collectList()
            .onErrorResume(error -> {
                log.error("Error fetching knowledge list from OpenWebUI", error);
                return Mono.just(List.of());
            })
            .block();
    }
}