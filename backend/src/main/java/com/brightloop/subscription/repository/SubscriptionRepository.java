package com.brightloop.subscription.repository;
import com.brightloop.subscription.entity.Subscription;
import com.brightloop.common.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public interface SubscriptionRepository extends JpaRepository<Subscription,UUID>{
 long countByStatus(SubscriptionStatus status);
 @EntityGraph(attributePaths={"customer","plan"}) Page<Subscription> findAllByOrderByCreatedAtDesc(Pageable page);
 @EntityGraph(attributePaths={"customer","plan"}) Page<Subscription> findByCustomer_User_EmailOrderByCreatedAtDesc(String email,Pageable page);
 @EntityGraph(attributePaths={"customer","plan","items"}) java.util.Optional<Subscription> findById(UUID id);
 @EntityGraph(attributePaths={"plan"}) List<Subscription> findByStatusAndNextBillingDateLessThanEqual(SubscriptionStatus status,LocalDate date,Pageable page);
 @EntityGraph(attributePaths={"plan"}) List<Subscription> findByStatusAndExpiryDateLessThanEqual(SubscriptionStatus status,LocalDate date,Pageable page);
 boolean existsByNumber(String number);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @EntityGraph(attributePaths={"customer","plan","items"}) @Query("select s from Subscription s where s.id=:id") java.util.Optional<Subscription> findLocked(@Param("id") UUID id);
}
