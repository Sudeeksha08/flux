package com.brightloop.product.repository;

import com.brightloop.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product,UUID> {
    Page<Product> findByActiveTrue(Pageable pageable);
    @EntityGraph(attributePaths="variants") Optional<Product> findByIdAndActiveTrue(UUID id);
}
