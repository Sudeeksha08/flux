package com.brightloop.tax.repository;
import com.brightloop.tax.entity.TaxRule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface TaxRuleRepository extends JpaRepository<TaxRule,UUID>{
 @EntityGraph(attributePaths="products") List<TaxRule> findByActiveTrueOrderByNameAsc();
 @EntityGraph(attributePaths="products") Optional<TaxRule> findByIdAndActiveTrue(UUID id);
}
