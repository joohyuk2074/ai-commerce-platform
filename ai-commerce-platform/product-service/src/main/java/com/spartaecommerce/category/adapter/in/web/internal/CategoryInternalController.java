package com.spartaecommerce.category.adapter.in.web.internal;

import com.spartaecommerce.category.adapter.in.web.internal.dto.CategoryDto;
import com.spartaecommerce.category.domain.entity.Category;
import com.spartaecommerce.category.domain.port.out.LoadCategoryPort;
import com.spartaecommerce.common.domain.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal API for inter-service communication
 */
@RestController
@RequestMapping("/internal/v1/categories")
@RequiredArgsConstructor
public class CategoryInternalController {

    private final LoadCategoryPort loadCategoryPort;

    /**
     * Bulk get categories by IDs
     */
    @PostMapping("/bulk")
    public CommonResponse<List<CategoryDto>> getCategories(
        @RequestBody List<Long> categoryIds
    ) {
        List<Category> categories = loadCategoryPort.findAllByCategoryIdIn(new java.util.HashSet<>(categoryIds));
        List<CategoryDto> categoryDtos = categories.stream()
            .map(CategoryDto::from)
            .toList();
        return CommonResponse.success(categoryDtos);
    }
}
