package com.brightloop.quotation.repository;
import com.brightloop.quotation.entity.Quotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.brightloop.common.enums.QuotationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface QuotationRepository extends JpaRepository<Quotation,UUID>{
 @EntityGraph(attributePaths={"customer","template","items"})
 @Query("select q from Quotation q where q.id = :id") Optional<Quotation> findWithDetailsById(@Param("id") UUID id);
 @EntityGraph(attributePaths={"customer"}) Page<Quotation> findAllByOrderByCreatedAtDesc(Pageable page);
 @EntityGraph(attributePaths={"customer"}) Page<Quotation> findByCustomer_User_EmailOrderByCreatedAtDesc(String email,Pageable page);
 boolean existsByNumber(String number);
 Page<Quotation> findByStatusInAndValidityDateBefore(List<QuotationStatus> status,LocalDate date,Pageable page);
}
