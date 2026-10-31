package com.brightloop.quotation.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
public record QuotationRequest(@NotNull UUID customerId,UUID templateId,@NotNull List<@Valid ItemInput> items){
 public record ItemInput(@NotNull UUID productId,UUID variantId,@Min(1) int quantity){}
}
