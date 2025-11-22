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

    private Long id;
    private String name;
    private String description;
    private Long parentId;

    @Builder.Default
    private List<CategoryTreeNodeResult> children = new ArrayList<>();

    public static CategoryTreeNodeResult from(Category node) {
        return CategoryTreeNodeResult.builder()
            .id(node.getCategoryId())
            .name(node.getName())
            .description(node.getDescription())
            .parentId(node.getParentCategoryId())
            .build();
    }

    public void addAllChildren(List<CategoryTreeNodeResult> children) {
        this.children.addAll(children);

    }
}