package com.brightloop.discount.repository;
import com.brightloop.discount.entity.Discount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface DiscountRepository extends JpaRepository<Discount,UUID>{
 @EntityGraph(attributePaths="products") List<Discount> findByActiveTrueOrderByNameAsc();
 @EntityGraph(attributePaths="products") Optional<Discount> findByIdAndActiveTrue(UUID id);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @EntityGraph(attributePaths="products")
 @Query("select d from Discount d where d.active = true order by d.id") List<Discount> lockActiveDiscounts();
}
