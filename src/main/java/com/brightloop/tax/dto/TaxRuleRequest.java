package com.brightloop.tax.dto;
import com.brightloop.common.enums.TaxType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
public record TaxRuleRequest(@NotBlank @Size(max=160) String name,@NotNull @DecimalMin("0.0001") @DecimalMax("100.0000") BigDecimal percentage,@NotNull TaxType type,@NotNull LocalDate effectiveFrom,LocalDate effectiveTo,@NotNull Set<UUID> productIds){@AssertTrue public boolean isDateRangeValid(){return effectiveTo==null||effectiveFrom==null||!effectiveTo.isBefore(effectiveFrom);}}
