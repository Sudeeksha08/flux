package com.brightloop.staff.repository;

import com.brightloop.staff.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.brightloop.staff.dto.StaffResponse;

import java.util.Optional;
import java.util.UUID;

public interface StaffRepository extends JpaRepository<Staff, UUID>, JpaSpecificationExecutor<Staff> {
    @Query(value = "select new com.brightloop.staff.dto.StaffResponse(s.id, u.id, s.displayName, u.email, s.phone, s.active, s.createdAt) " +
            "from Staff s join s.user u where (:active is null or s.active = :active)",
            countQuery = "select count(s) from Staff s where (:active is null or s.active = :active)")
    Page<StaffResponse> search(@Param("active") Boolean active, Pageable pageable);

    Optional<Staff> findByUser_Email(String email);
    boolean existsByUser_Email(String email);
}
