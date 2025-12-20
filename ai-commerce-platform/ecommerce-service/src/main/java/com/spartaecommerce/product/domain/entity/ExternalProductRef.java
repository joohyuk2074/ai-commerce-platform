package com.spartaecommerce.product.domain.entity;

public record ExternalProductRef(
    String vendor,
    String externalId
) {

    public ExternalProductRef {
        if (vendor == null || vendor.isBlank())  {
            throw new IllegalArgumentException("vendor required");
        }

        if (externalId == null || externalId.isBlank())  {
            throw new IllegalArgumentException("externalId required");
        }
    }

    public static ExternalProductRef of(String vendor, Long id) {
        return new ExternalProductRef(vendor, String.valueOf(id));
    }
}
