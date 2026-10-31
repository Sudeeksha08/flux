package com.brightloop.discount.dto;
import com.brightloop.common.enums.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
public record DiscountResponse(UUID id,String name,DiscountType type,BigDecimal value,BigDecimal minimumPurchase,int minimumQuantity,
 LocalDate startDate,LocalDate endDate,Integer usageLimit,int usageCount,boolean active,Set<UUID> productIds){}
