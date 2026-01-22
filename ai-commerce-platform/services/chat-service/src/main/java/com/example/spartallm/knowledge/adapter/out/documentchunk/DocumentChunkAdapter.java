package com.example.spartallm.knowledge.adapter.out.documentchunk;

import com.example.spartallm.knowledge.domain.model.KnowledgeDocumentChunk;
import com.example.spartallm.knowledge.domain.port.out.DocumentChunkPort;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class DocumentChunkAdapter implements DocumentChunkPort {

    @Override
    public List<KnowledgeDocumentChunk> chunk(String filename, String content) {
        List<Document> documents = splitDocument(filename, content);

        return IntStream.range(0, documents.size())
            .mapToObj(chunkIndex -> {
                Document document = documents.get(chunkIndex);
                return KnowledgeDocumentChunk.create(
                    chunkIndex,
                    document.getText(),
                    document.getMetadata()
                );
            })
            .toList();
    }

    private List<Document> splitDocument(String filename, String content) {
        TokenTextSplitter splitter = new TokenTextSplitter(
            500,    // defaultChunkSize: 기본 청크 크기
            100,    // minChunkSizeChars: 최소 청크 크기 (문자 단위)
            5,      // minChunkLengthToEmbed: 임베딩할 최소 청크 길이
            1,      // maxNumChunks: 최대 청크 수 (무제한은 Integer.MAX_VALUE)
            true    // keepSeparator: 구분자 유지 여부
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", filename);
        metadata.put("source", "user_upload");

        Document document = new Document(content, metadata);

        return splitter.split(document);
    }
}
