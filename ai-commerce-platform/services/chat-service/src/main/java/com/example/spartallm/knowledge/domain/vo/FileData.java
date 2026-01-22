package com.example.spartallm.knowledge.domain.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileData {

    @Builder.Default
    private FileStatus status = FileStatus.PENDING;

    public static FileData pending() {
        return FileData.builder()
            .status(FileStatus.PENDING)
            .build();
    }

    public static FileData processing() {
        return FileData.builder()
            .status(FileStatus.PROCESSING)
            .build();
    }

    public static FileData completed() {
        return FileData.builder()
            .status(FileStatus.COMPLETED)
            .build();
    }

    public static FileData failed() {
        return FileData.builder()
            .status(FileStatus.FAILED)
            .build();
    }
}