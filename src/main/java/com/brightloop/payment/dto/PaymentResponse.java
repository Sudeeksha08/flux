package com.brightloop.payment.dto;
import com.brightloop.common.enums.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
public record PaymentResponse(UUID id,Instant paymentDate,PaymentMethod method,BigDecimal amount,String notes,String reference,
 PaymentStatus status,String recordedBy,List<Allocation> allocations){public record Allocation(UUID invoiceId,String invoiceNumber,BigDecimal amount){} }
