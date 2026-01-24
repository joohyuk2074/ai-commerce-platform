package com.spartaecommerce.order.domain.port.out;

import com.spartaecommerce.order.domain.entity.Category;
import java.util.Collection;
import java.util.List;

public interface LoadCategoryPort {

  List<Category> getCategories(Collection<Long> categoryIds);
}
