package com.brightloop.discount.repository;
import com.brightloop.discount.entity.DiscountUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface DiscountUsageRepository extends JpaRepository<DiscountUsage,UUID>{
 Optional<DiscountUsage> findFirstByBillingReference(String reference);
}
