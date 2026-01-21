package com.spartaecommerce.category.domain.port.out;

import com.spartaecommerce.common.domain.category.Category;

public interface SaveCategoryPort {

    Long save(Category category);
}
