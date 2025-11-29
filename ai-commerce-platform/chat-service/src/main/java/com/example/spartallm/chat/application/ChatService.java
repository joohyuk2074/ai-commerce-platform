package com.example.spartallm.chat.application;

import com.example.spartallm.chat.application.dto.query.ChatQuery;
import com.example.spartallm.chat.application.dto.result.ChatResult;
import com.example.spartallm.chat.application.dto.result.LlmModelListResult;
import com.example.spartallm.chat.domain.model.DocumentSource;
import com.example.spartallm.chat.domain.model.LlmModelInfo;
import com.example.spartallm.chat.domain.model.LlmRequestMessage;
import com.example.spartallm.chat.domain.model.LlmResponseMessage;
import com.example.spartallm.chat.domain.port.LlmChatPort;
import com.example.spartallm.knowledge.application.dto.command.SearchCommand;
import com.example.spartallm.knowledge.domain.port.out.VectorStorePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final List<LlmChatPort> llmChatPorts;
    private final VectorStorePort vectorStorePort;

    public ChatResult chatSync(ChatQuery chatQuery) {
        LlmChatPort selectedPort = selectLlmChatPort(chatQuery.model());

        String userQuery = extractUserMessage(chatQuery);

        List<DocumentSource> relevantDocuments = searchRelevantDocuments(userQuery);

        LlmRequestMessage llmRequestMessage = buildRequestWithContext(chatQuery, relevantDocuments);

        LlmResponseMessage llmResponseMessage = selectedPort.chat(llmRequestMessage);

        log.info("응답 완료 - 모델: {}, 참조 문서 수: {}",
            llmResponseMessage.getModel(), relevantDocuments.size());

        return ChatResult.of(llmResponseMessage, relevantDocuments);
    }

    public Flux<ChatResult> chatStream(ChatQuery chatQuery) {
        LlmChatPort selectedPort = selectLlmChatPort(chatQuery.model());

        String userQuery = extractUserMessage(chatQuery);

        List<DocumentSource> relevantDocuments = searchRelevantDocuments(userQuery);

        LlmRequestMessage llmRequestMessage = buildRequestWithContext(chatQuery, relevantDocuments);

        Flux<LlmResponseMessage> llmResponseMessageFlux = selectedPort.chatStream(llmRequestMessage);

        return llmResponseMessageFlux.map(response -> ChatResult.of(response, relevantDocuments));
    }

    public LlmModelListResult getLlmList() {
        List<LlmModelInfo> allModels = llmChatPorts.stream()
            .flatMap(port -> port.getSupportedModels().stream())
            .toList();

        log.info("지원 모델 수: {}", allModels.size());

        return LlmModelListResult.of(allModels);
    }

    private LlmChatPort selectLlmChatPort(String modelName) {
        for (LlmChatPort llmChatPort : llmChatPorts) {
            if (llmChatPort.supports(modelName)) {
                return llmChatPort;
            }
        }

        throw new IllegalArgumentException("Invalid model name: " + modelName);
    }

    private String extractUserMessage(ChatQuery chatQuery) {
        return chatQuery.messages().stream()
            .filter(m -> "user".equals(m.role()))
            .map(ChatQuery.ChatMessageQuery::content)
            .reduce((a, b) -> a + "\n" + b)
            .orElseThrow(() -> new IllegalArgumentException("사용자 메시지가 없습니다"));
    }

    private List<DocumentSource> searchRelevantDocuments(String query) {
        try {
            SearchCommand searchCommand = SearchCommand.of(query, 5);
            return vectorStorePort.similaritySearch(searchCommand);
        } catch (Exception e) {
            log.warn("벡터 검색 실패, 빈 컨텍스트로 진행: {}", e.getMessage());
            return List.of();
        }
    }

    private LlmRequestMessage buildRequestWithContext(ChatQuery chatQuery, List<DocumentSource> documents) {
        if (documents.isEmpty()) {
            return chatQuery.toChatMessage();
        }

        // 기존 메시지에서 시스템 프롬프트와 사용자 메시지 추출
        String systemPrompt = chatQuery.messages().stream()
            .filter(m -> "system".equals(m.role()))
            .map(ChatQuery.ChatMessageQuery::content)
            .reduce((a, b) -> a + "\n" + b)
            .orElse("");

        String userMessage = chatQuery.messages().stream()
            .filter(m -> "user".equals(m.role()))
            .map(ChatQuery.ChatMessageQuery::content)
            .reduce((a, b) -> a + "\n" + b)
            .orElseThrow(() -> new IllegalArgumentException("사용자 메시지가 없습니다"));

        // 문서 컨텍스트 구성
        String contextPrompt = buildContextPrompt(documents);

        // 기존 systemPrompt와 컨텍스트 결합
        String enhancedSystemPrompt = systemPrompt.isEmpty()
            ? contextPrompt
            : systemPrompt + "\n\n" + contextPrompt;

        return LlmRequestMessage.of(
            java.util.UUID.randomUUID().toString(),
            chatQuery.model(),
            enhancedSystemPrompt,
            userMessage,
            chatQuery.temperature(),
            chatQuery.maxTokens()
        );
    }

    /**
     * 문서들로부터 컨텍스트 프롬프트 생성
     */
    private String buildContextPrompt(List<DocumentSource> documents) {
        String context = documents.stream()
            .map(doc -> String.format("문서 ID: %d, 청크: %d\n내용: %s",
                doc.knowledgeDocumentId(),
                doc.chunkIndex(),
                doc.content()))
            .collect(Collectors.joining("\n\n---\n\n"));

        return "다음은 참고할 수 있는 관련 문서입니다:\n\n" + context +
               "\n\n위 문서를 참고하여 사용자의 질문에 답변해주세요. " +
               "답변 시 어떤 문서를 참고했는지 명시해주세요.";
    }
}

