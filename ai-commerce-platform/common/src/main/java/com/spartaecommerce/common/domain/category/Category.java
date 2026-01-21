package com.spartaecommerce.common.domain.category;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Category {

    private Long categoryId;

    private String name;

    private String description;

    private Long parentCategoryId;

    private List<Long> childrenCategoryIds;

    private boolean deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static Category createNew(String name, String description, Long parentCategoryId) {
        return Category.builder()
            .name(name)
            .description(description)
            .parentCategoryId(parentCategoryId)
            .childrenCategoryIds(new ArrayList<>())
            .deleted(false)
            .build();
    }

    public void update(String name, String description) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        if (description != null && !description.isBlank()) {
            this.description = description;
        }
    }

    public void delete() {
        this.deleted = true;
    }
}
