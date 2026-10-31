package com.brightloop.payment.repository;
import com.brightloop.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface PaymentRepository extends JpaRepository<Payment,UUID>{
 @EntityGraph(attributePaths={"recordedBy"}) Page<Payment> findAllByOrderByPaymentDateDesc(Pageable page);
 @EntityGraph(attributePaths={"recordedBy","allocations","allocations.invoice"}) @org.springframework.data.jpa.repository.Query("select p from Payment p where p.id=:id") Optional<Payment> findDetailedById(@org.springframework.data.repository.query.Param("id") UUID id);
}
