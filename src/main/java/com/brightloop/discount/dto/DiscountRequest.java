package com.brightloop.discount.dto;
import com.brightloop.common.enums.DiscountType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
public record DiscountRequest(@NotBlank @Size(max=160) String name,@NotNull DiscountType type,@NotNull @Positive BigDecimal value,
 @NotNull @PositiveOrZero BigDecimal minimumPurchase,@Min(0) int minimumQuantity,@NotNull LocalDate startDate,LocalDate endDate,
 @Positive Integer usageLimit,@NotNull Set<UUID> productIds){@AssertTrue public boolean isDateRangeValid(){return endDate==null||startDate==null||!endDate.isBefore(startDate);}}
