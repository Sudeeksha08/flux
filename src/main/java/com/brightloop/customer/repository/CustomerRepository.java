package com.brightloop.customer.repository;

import com.brightloop.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.brightloop.customer.dto.CustomerResponse;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {
    @Query(value = "select new com.brightloop.customer.dto.CustomerResponse(c.id, u.id, c.name, u.email, c.companyName, c.phone, c.address, c.archived, c.createdAt) " +
            "from Customer c join c.user u where c.archived = :archived and " +
            "(:query is null or trim(:query) = '' or lower(c.name) like lower(concat('%', :query, '%')) " +
            "or lower(c.companyName) like lower(concat('%', :query, '%')) or lower(u.email) like lower(concat('%', :query, '%')))" ,
            countQuery = "select count(c) from Customer c join c.user u where c.archived = :archived and " +
                    "(:query is null or trim(:query) = '' or lower(c.name) like lower(concat('%', :query, '%')) " +
                    "or lower(c.companyName) like lower(concat('%', :query, '%')) or lower(u.email) like lower(concat('%', :query, '%')))")
    Page<CustomerResponse> search(@Param("query") String query, @Param("archived") boolean archived, Pageable pageable);

    Optional<Customer> findByUser_Email(String email);
    boolean existsByUser_Email(String email);
}
