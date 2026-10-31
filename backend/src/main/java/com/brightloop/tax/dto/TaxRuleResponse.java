package com.brightloop.tax.dto;
import com.brightloop.common.enums.TaxType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
public record TaxRuleResponse(UUID id,String name,BigDecimal percentage,TaxType type,LocalDate effectiveFrom,LocalDate effectiveTo,boolean active,Set<UUID> productIds){}
