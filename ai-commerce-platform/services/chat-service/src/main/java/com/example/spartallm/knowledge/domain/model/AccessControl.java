package com.example.spartallm.knowledge.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessControl {

    @Builder.Default
    private final AccessPermission read = new AccessPermission();

    @Builder.Default
    private final AccessPermission write = new AccessPermission();

    public static AccessControl createDefault() {
        return AccessControl.builder()
            .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AccessPermission {

        @Builder.Default
        private final List<Long> groupIds = new ArrayList<>();

        @Builder.Default
        private final List<Long> userIds = new ArrayList<>();

        public AccessPermission() {
            this.groupIds = new ArrayList<>();
            this.userIds = new ArrayList<>();
        }
    }
}