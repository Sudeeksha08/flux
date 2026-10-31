package com.brightloop.plan.dto;

import com.brightloop.common.enums.BillingFrequency;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PlanResponse(UUID id,String name,BigDecimal price,BillingFrequency billingFrequency,
        int minimumQuantity,LocalDate availableFrom,LocalDate availableTo,boolean autoClose,
        boolean earlyCloseAllowed,boolean pauseAllowed,boolean renewalAllowed,boolean active,
        List<ProductItem> products,Instant createdAt,Instant updatedAt) {
    public record ProductItem(UUID productId,String productName,int quantity) { }
}
