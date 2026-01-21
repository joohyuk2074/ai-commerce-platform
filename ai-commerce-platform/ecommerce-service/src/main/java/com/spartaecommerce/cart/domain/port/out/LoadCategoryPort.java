package com.spartaecommerce.cart.domain.port.out;

import com.spartaecommerce.common.domain.category.Category;

import java.util.List;
import java.util.Set;

public interface LoadCategoryPort {

    List<Category> findAllByCategoryIdIn(Set<Long> categoryIds);
}
