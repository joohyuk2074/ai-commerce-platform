package com.example.spartallm.embedding.application;

import com.example.spartallm.embedding.application.dto.query.EmbeddingQuery;
import com.example.spartallm.embedding.application.dto.result.EmbeddingResult;
import com.example.spartallm.embedding.domain.model.EmbeddingRequestModel;
import com.example.spartallm.embedding.domain.model.EmbeddingResponseModel;
import com.example.spartallm.embedding.domain.port.LlmEmbeddingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final List<LlmEmbeddingPort> llmEmbeddingPorts;

    public EmbeddingResult embed(EmbeddingQuery embeddingQuery) {
        LlmEmbeddingPort selectedPort = selectLlmEmbeddingPort(embeddingQuery.model());

        EmbeddingRequestModel requestModel = EmbeddingRequestModel.of(
            embeddingQuery.input(),
            embeddingQuery.model()
        );

        EmbeddingResponseModel responseModel = selectedPort.embed(requestModel);

        log.info("임베딩 완료 - 모델: {}, 입력 수: {}", responseModel.model(), embeddingQuery.input().size());

        return convertToResult(responseModel);
    }

    private EmbeddingResult convertToResult(EmbeddingResponseModel responseModel) {
        List<EmbeddingResult.EmbeddingData> embeddings = responseModel.embeddings().stream()
            .map(e -> new EmbeddingResult.EmbeddingData(e.vector(), e.index()))
            .toList();

        EmbeddingResult.UsageData usage = new EmbeddingResult.UsageData(
            responseModel.totalTokens(),
            responseModel.totalTokens()
        );

        return new EmbeddingResult(embeddings, responseModel.model(), usage);
    }

    private LlmEmbeddingPort selectLlmEmbeddingPort(String modelName) {
        for (LlmEmbeddingPort port : llmEmbeddingPorts) {
            if (port.supports(modelName)) {
                return port;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 임베딩 모델입니다: " + modelName);
    }
}