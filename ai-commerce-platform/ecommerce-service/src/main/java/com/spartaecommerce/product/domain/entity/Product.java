package com.spartaecommerce.product.domain.entity;

import com.spartaecommerce.common.domain.Money;
import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.product.application.dto.command.ProductUpdateCommand;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {

    private Long productId;

    private String name;

    private String description;

    private Money price;

    private Integer stock;

    private Long categoryId;

    private boolean deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static Product createNew(
        String name,
        String description,
        Money price,
        Integer stock,
        Long categoryId
    ) {
        return Product.builder()
            .name(name)
            .description(description)
            .price(price)
            .stock(stock)
            .categoryId(categoryId)
            .deleted(false)
            .build();
    }

    public void update(
        String name,
        String description,
        Money price,
        Integer stock,
        Long categoryId
    ) {
        if (this.deleted) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Cannot update product. The product has already been deleted. productId: " + this.productId
            );
        }

        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        if (description != null && !description.isBlank()) {
            this.description = description;
        }

        if (price != null) {
            this.price = price;
        }

        if (stock != null) {
            this.stock = stock;
        }

        if (categoryId != null) {
            this.categoryId = categoryId;
        }
    }

    public void deductQuantity(Integer quantity) {
        if (this.stock < quantity) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Available: " + this.stock + ", Requested: " + quantity
            );
        }

        this.stock -= quantity;
    }

    public void restoreQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Restore quantity must be greater than 0"
            );
        }

        this.stock += quantity;
    }

    public void delete() {
        this.deleted = true;
    }
}
