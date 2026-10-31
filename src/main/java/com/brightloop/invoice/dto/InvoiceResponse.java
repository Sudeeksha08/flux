package com.brightloop.invoice.dto;
import com.brightloop.common.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public record InvoiceResponse(UUID id,String number,UUID customerId,UUID subscriptionId,LocalDate periodStart,LocalDate periodEnd,
 LocalDate invoiceDate,LocalDate dueDate,String customerName,String customerEmail,String companyName,String customerPhone,String customerAddress,
 BigDecimal subtotal,BigDecimal discountAmount,BigDecimal taxAmount,BigDecimal totalAmount,BigDecimal paidAmount,BigDecimal outstandingAmount,
 InvoiceStatus status,String discountName,List<Item> items){public record Item(UUID id,String productId,String productName,String variantName,int quantity,BigDecimal unitPrice,BigDecimal subtotal,BigDecimal discountAmount,BigDecimal taxAmount,BigDecimal totalAmount,String taxDetailsJson){}}
