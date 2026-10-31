package com.brightloop.subscription.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;
public record SubscriptionRequest(@NotNull UUID customerId,@NotNull UUID planId,UUID quotationId,LocalDate startDate,@Size(max=80) String paymentTerms){}
