package com.example.spartallm.knowledge.domain.port.in;

import com.example.spartallm.knowledge.application.dto.command.UploadDocumentCommand;
import com.example.spartallm.knowledge.application.dto.result.UploadDocumentResult;

public interface UploadDocumentUseCase {

    UploadDocumentResult execute(UploadDocumentCommand command);
}