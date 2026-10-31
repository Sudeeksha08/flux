package com.brightloop.invoice.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record InvoicePdfView(String number,LocalDate invoiceDate,LocalDate dueDate,String customerName,String customerEmail,String address,
 BigDecimal subtotal,BigDecimal discountAmount,BigDecimal taxAmount,BigDecimal totalAmount,BigDecimal paidAmount,BigDecimal outstandingAmount,
 List<Row> items){public record Row(String name,int quantity,BigDecimal unitPrice,BigDecimal total){}}
