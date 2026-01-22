package com.spartaecommerce.category.application.dto.result;

import com.spartaecommerce.category.domain.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeNodeResult {

    private Long categoryId;
    private String name;
    private String description;
    private Long parentCategoryId;

    @Builder.Default
    private List<CategoryTreeNodeResult> children = new ArrayList<>();

    public static CategoryTreeNodeResult from(Category node) {
        return CategoryTreeNodeResult.builder()
            .categoryId(node.getCategoryId())
            .name(node.getName())
            .description(node.getDescription())
            .parentCategoryId(node.getParentCategoryId())
            .build();
    }

    public void addAllChildren(List<CategoryTreeNodeResult> children) {
        this.children.addAll(children);

    }
}