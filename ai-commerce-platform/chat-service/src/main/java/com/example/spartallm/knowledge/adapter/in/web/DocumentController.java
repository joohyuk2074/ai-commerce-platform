package com.example.spartallm.knowledge.adapter.in.web;

import com.example.spartallm.knowledge.application.dto.command.UploadDocumentCommand;
import com.example.spartallm.knowledge.application.dto.result.UploadDocumentResult;
import com.example.spartallm.knowledge.adapter.in.web.dto.response.FileUploadResponse;
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
public class DocumentController {

    private final UploadDocumentUseCase uploadDocumentUseCase;

    @PostMapping("/knowledge/{knowledgeId}/documents/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
        @PathVariable
        Long knowledgeId,

        @RequestHeader(
            value = "X-User-Id",
            required = false,
            defaultValue = "663cfa58-b1b7-490c-b11a-5a6247491039"
        ) Long userId,

        @RequestParam("file")
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

        FileUploadResponse response = FileUploadResponse.from(result);

        log.info("File uploaded successfully. fileId: {}, path: {}", response.getId(), response.getPath());

        return ResponseEntity.ok(response);
    }
}