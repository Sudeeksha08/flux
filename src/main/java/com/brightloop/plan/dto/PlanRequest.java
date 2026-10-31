package com.brightloop.plan.dto;

import com.brightloop.common.enums.BillingFrequency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PlanRequest(@NotBlank @Size(max=160) String name,
        @NotNull @PositiveOrZero BigDecimal price,
        @NotNull BillingFrequency billingFrequency,
        @Min(1) int minimumQuantity,
        LocalDate availableFrom, LocalDate availableTo,
        boolean autoClose, boolean earlyCloseAllowed, boolean pauseAllowed, boolean renewalAllowed,
        @NotNull List<@Valid ProductInput> products) {
    @AssertTrue(message="availableTo must be on or after availableFrom")
    public boolean isAvailabilityRangeValid(){return availableFrom==null||availableTo==null||!availableTo.isBefore(availableFrom);}
    public record ProductInput(@NotNull UUID productId,@Min(1) int quantity) { }
}
