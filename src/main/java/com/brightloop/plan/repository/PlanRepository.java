package com.brightloop.plan.repository;

import com.brightloop.plan.entity.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan,UUID> {
    Page<Plan> findByActiveTrue(Pageable pageable);
    @EntityGraph(attributePaths={"products","products.product"}) Optional<Plan> findByIdAndActiveTrue(UUID id);
}
