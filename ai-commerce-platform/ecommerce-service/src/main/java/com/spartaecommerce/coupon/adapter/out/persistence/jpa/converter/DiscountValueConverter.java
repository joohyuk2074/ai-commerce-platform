package com.spartaecommerce.coupon.adapter.out.persistence.jpa.converter;

import com.spartaecommerce.coupon.domain.value.DiscountValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * DiscountValue를 데이터베이스 컬럼으로 변환하는 JPA Converter
 * 형식: "type:value" (예: "PERCENT:10", "FIXED:10000")
 */
@Converter(autoApply = true)
public class DiscountValueConverter implements AttributeConverter<DiscountValue, String> {

    @Override
    public String convertToDatabaseColumn(DiscountValue attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.toStorageString();
    }

    @Override
    public DiscountValue convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return DiscountValue.fromStorageString(dbData);
    }
}
