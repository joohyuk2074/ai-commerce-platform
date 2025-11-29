package com.example.spartallm.knowledge.controller;

import com.example.spartallm.knowledge.application.KnowledgeService;
import com.example.spartallm.knowledge.application.dto.result.CreateKnowledgeResult;
import com.example.spartallm.knowledge.controller.dto.request.CreateKnowledgeRequest;
import com.example.spartallm.knowledge.controller.dto.response.CreateKnowledgeResponse;
import com.example.spartallm.knowledge.controller.dto.response.KnowledgeResponse;
import com.example.spartallm.knowledge.domain.command.CreateKnowledgeCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/create")
    public ResponseEntity<CreateKnowledgeResponse> createKnowledge(
        @RequestBody @Valid CreateKnowledgeRequest request
    ) {
        // TODO: Replace with authenticated user ID when authentication is implemented
        Long userId = 1L;
        log.info("Creating knowledge - name: {}, userId: {}", request.name(), userId);

        CreateKnowledgeCommand command = request.toCommand(userId);
        CreateKnowledgeResult result = knowledgeService.createKnowledge(command);
        CreateKnowledgeResponse response = CreateKnowledgeResponse.from(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeResponse> getKnowledge(@PathVariable Long id) {
        log.info("Fetching knowledge - id: {}", id);

        KnowledgeResponse knowledge = knowledgeService.getKnowledge(id);

        return ResponseEntity.ok(knowledge);
    }

    @GetMapping("/list")
    public ResponseEntity<List<KnowledgeResponse>> getKnowledgeList() {
        log.info("Fetching knowledge list");
        // TODO: User Id 헤더값으로 입력받도록 수정
        Long userId = 1L;

        List<KnowledgeResponse> knowledgeList = knowledgeService.getKnowledgeList(userId);

        return ResponseEntity.ok(knowledgeList);
    }
}