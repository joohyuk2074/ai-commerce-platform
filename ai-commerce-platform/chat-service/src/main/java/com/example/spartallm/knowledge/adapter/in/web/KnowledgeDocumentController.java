package com.example.spartallm.knowledge.adapter.in.web;

import com.example.spartallm.knowledge.adapter.in.web.dto.response.UploadKnowledgeDocumentResponse;
import com.example.spartallm.knowledge.application.dto.command.UploadDocumentCommand;
import com.example.spartallm.knowledge.application.dto.result.UploadDocumentResult;
import com.example.spartallm.knowledge.domain.port.in.UploadDocumentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class KnowledgeDocumentController {

    private final UploadDocumentUseCase uploadDocumentUseCase;

    @PostMapping("/knowledge/{knowledgeId}/documents/upload")
    public ResponseEntity<UploadKnowledgeDocumentResponse> uploadFile(
        @PathVariable
        Long knowledgeId,

        @RequestHeader(
            value = "X-User-Id",
            required = false,
            defaultValue = "663cfa58-b1b7-490c-b11a-5a6247491039"
        ) Long userId,

        @RequestPart("file")
        MultipartFile file
    ) throws IOException {
        log.info("File upload request received. filename: {}, size: {}, userId: {}",
            file.getOriginalFilename(), file.getSize(), userId);

        UploadDocumentCommand command = new UploadDocumentCommand(
            knowledgeId,
            userId,
            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize(),
            file.getBytes()
        );

        UploadDocumentResult result = uploadDocumentUseCase.execute(command);

        UploadKnowledgeDocumentResponse response = UploadKnowledgeDocumentResponse.from(result);

        log.info("File uploaded successfully. documentId: {}, chunks: {}",
            response.getDocumentId(), response.getTotalChunks());

        return ResponseEntity.ok(response);
    }
}