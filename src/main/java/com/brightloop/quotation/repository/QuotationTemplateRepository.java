package com.brightloop.quotation.repository;
import com.brightloop.quotation.entity.QuotationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface QuotationTemplateRepository extends JpaRepository<QuotationTemplate,UUID>{
 List<QuotationTemplate> findByActiveTrueOrderByNameAsc();
 Optional<QuotationTemplate> findByIdAndActiveTrue(UUID id);
}
