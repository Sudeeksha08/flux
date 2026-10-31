package com.brightloop.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(@NotBlank @Size(max=180) String name,
        @NotBlank @Size(max=40) String productType,
        @NotNull @PositiveOrZero BigDecimal salesPrice,
        @NotNull @PositiveOrZero BigDecimal costPrice,
        @Size(max=2000) String notes,
        @NotNull List<@Valid VariantInput> variants) {
    public record VariantInput(@NotBlank @Size(max=160) String name,@NotNull @PositiveOrZero BigDecimal additionalPrice) { }
}
