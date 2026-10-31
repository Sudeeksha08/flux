package com.brightloop.quotation.dto;
import com.brightloop.common.enums.QuotationStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
public record QuotationResponse(UUID id,String number,UUID customerId,String customerName,UUID templateId,
 LocalDate validityDate,QuotationStatus status,BigDecimal subtotal,BigDecimal discountAmount,BigDecimal taxAmount,
 BigDecimal totalAmount,Instant createdAt,List<Item> items){
 public record Item(UUID id,String productId,String productName,String variantName,int quantity,BigDecimal unitPrice,BigDecimal lineTotal){}
}
