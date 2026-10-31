package com.brightloop.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductResponse(UUID id,String name,String productType,BigDecimal salesPrice,BigDecimal costPrice,
        String notes,boolean active,Instant createdAt,Instant updatedAt,List<VariantResponse> variants) {
    public record VariantResponse(UUID id,String name,BigDecimal additionalPrice,BigDecimal finalPrice) { }
}
