package com.spartaecommerce.category.domain.port.in;

import com.spartaecommerce.category.application.dto.commnad.CategoryRegisterCommand;
import com.spartaecommerce.category.application.dto.commnad.CategoryUpdateCommand;

public interface CategoryCommandUseCase {

    Long register(CategoryRegisterCommand registerCommand);

    void update(CategoryUpdateCommand updateCommand);

    void delete(Long categoryId);
}
