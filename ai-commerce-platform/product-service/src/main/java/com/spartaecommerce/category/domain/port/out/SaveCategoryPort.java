package com.spartaecommerce.category.domain.port.out;

import com.spartaecommerce.category.domain.entity.Category;

public interface SaveCategoryPort {

    Long save(Category category);
}
