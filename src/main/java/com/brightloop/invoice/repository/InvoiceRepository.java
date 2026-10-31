package com.brightloop.invoice.repository;
import com.brightloop.common.enums.InvoiceStatus;
import com.brightloop.invoice.entity.Invoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;
public interface InvoiceRepository extends JpaRepository<Invoice,UUID>{
 Optional<Invoice> findBySubscription_IdAndPeriodStart(UUID subscriptionId,LocalDate periodStart);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select i from Invoice i where i.id=:id") Optional<Invoice> findLocked(@Param("id") UUID id);
 @EntityGraph(attributePaths={"customer","subscription","items"}) @Query("select i from Invoice i where i.id=:id") Optional<Invoice> findDetailedById(@Param("id") UUID id);
 @EntityGraph(attributePaths={"customer","subscription"}) Page<Invoice> findAllByOrderByInvoiceDateDesc(Pageable page);
 @EntityGraph(attributePaths={"customer","subscription"}) Page<Invoice> findByCustomer_User_EmailOrderByInvoiceDateDesc(String email,Pageable page);
 @EntityGraph(attributePaths={"customer","subscription"}) List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status,LocalDate date,Pageable page);
 @EntityGraph(attributePaths={"customer","subscription"}) List<Invoice> findByStatusInAndDueDateBeforeAndOutstandingAmountGreaterThan(List<InvoiceStatus> statuses,LocalDate date,java.math.BigDecimal amount,Pageable page);
 long countByStatus(InvoiceStatus status);
 @Query("select coalesce(sum(i.totalAmount),0) from Invoice i where i.status <> com.brightloop.common.enums.InvoiceStatus.CANCELLED") java.math.BigDecimal sumInvoicedRevenue();
 @Query("select coalesce(sum(i.paidAmount),0) from Invoice i") java.math.BigDecimal sumCollectedPayments();
 @Query("select coalesce(sum(i.outstandingAmount),0) from Invoice i where i.status <> com.brightloop.common.enums.InvoiceStatus.CANCELLED") java.math.BigDecimal sumOutstanding();
 @Query("select i from Invoice i where i.dueDate < :today and i.outstandingAmount > 0 and i.status not in :excluded order by i.dueDate asc")
 Page<Invoice> findOverdue(@Param("today") LocalDate today,@Param("excluded") Collection<InvoiceStatus> excluded,Pageable page);
 @Query("select i from Invoice i where i.customer.user.email=:email and i.dueDate < :today and i.outstandingAmount > 0 and i.status not in :excluded order by i.dueDate asc")
 Page<Invoice> findOverdueForCustomer(@Param("email") String email,@Param("today") LocalDate today,@Param("excluded") Collection<InvoiceStatus> excluded,Pageable page);
}
