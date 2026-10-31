package com.brightloop.invoice.dto;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
public record InvoiceGenerateRequest(@NotNull UUID subscriptionId,LocalDate billingPeriodStart){}
