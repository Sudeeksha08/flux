package com.brightloop.subscription.dto;
import com.brightloop.common.enums.SubscriptionStatus;
import com.brightloop.common.enums.BillingFrequency;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public record SubscriptionResponse(UUID id,String number,UUID customerId,String customerName,UUID planId,String planName,
 UUID quotationId,LocalDate startDate,LocalDate expiryDate,LocalDate nextBillingDate,String paymentTerms,
 BigDecimal totalAmount,SubscriptionStatus status,Instant createdAt,List<Item> items){
 public record Item(UUID id,String productId,String productName,String variantName,int quantity,BigDecimal unitPrice,BigDecimal lineTotal){}
}
