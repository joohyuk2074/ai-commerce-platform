package com.example.spartallm.embedding.controller;

import com.example.spartallm.embedding.application.EmbeddingService;
import com.example.spartallm.embedding.application.dto.query.EmbeddingQuery;
import com.example.spartallm.embedding.application.dto.result.EmbeddingResult;
import com.example.spartallm.embedding.controller.dto.request.EmbeddingRequest;
import com.example.spartallm.embedding.controller.dto.response.EmbeddingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    @PostMapping("/embeddings")
    public ResponseEntity<EmbeddingResponse> createEmbedding(
        @RequestBody @Valid EmbeddingRequest embeddingRequest
    ) {
        EmbeddingQuery embeddingQuery = embeddingRequest.toEmbeddingQuery();

        log.info("임베딩 요청 - 모델: {}, 입력 수: {}",
            embeddingQuery.model(), embeddingQuery.input().size());

        EmbeddingResult embeddingResult = embeddingService.embed(embeddingQuery);

        return ResponseEntity.ok(EmbeddingResponse.of(embeddingResult));
    }
}