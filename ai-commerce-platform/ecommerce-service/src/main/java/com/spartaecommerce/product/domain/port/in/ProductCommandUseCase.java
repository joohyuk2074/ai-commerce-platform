package com.spartaecommerce.product.domain.port.in;

import com.spartaecommerce.product.application.dto.command.ProductRegisterCommand;
import com.spartaecommerce.product.application.dto.command.ProductUpdateCommand;

public interface ProductCommandUseCase {

    Long register(ProductRegisterCommand registerCommand);

    void update(ProductUpdateCommand updateCommand);

    void delete(Long productId);
}
