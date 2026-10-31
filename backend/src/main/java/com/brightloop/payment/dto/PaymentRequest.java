package com.brightloop.payment.dto;
import com.brightloop.common.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public record PaymentRequest(@NotNull PaymentMethod method,@NotNull @Positive BigDecimal amount,@Size(max=1000) String notes,@Size(max=120) String reference,
 @NotEmpty @Size(max=20) List<@Valid AllocationInput> allocations){public record AllocationInput(@NotNull UUID invoiceId,@NotNull @Positive BigDecimal amount){}}
