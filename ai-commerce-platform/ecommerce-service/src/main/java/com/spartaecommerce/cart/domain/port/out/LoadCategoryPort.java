package com.spartaecommerce.cart.domain.port.out;

import com.spartaecommerce.category.domain.entity.Category;

import java.util.List;
import java.util.Set;

public interface LoadCategoryPort {

    List<Category> findAllByCategoryIdIn(Set<Long> categoryIds);
}
