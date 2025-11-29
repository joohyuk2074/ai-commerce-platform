package com.example.spartallm.knowledge.domain.vo;

public enum DocumentStatus {
    UPLOADING,    // 업로드 중
    PROCESSING,   // 처리 중 (청킹, 임베딩 등)
    COMPLETED,    // 완료
    FAILED        // 실패
}
