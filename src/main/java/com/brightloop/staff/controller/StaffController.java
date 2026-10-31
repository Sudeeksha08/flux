package com.brightloop.staff.controller;

import com.brightloop.common.response.ApiMessage;
import com.brightloop.staff.dto.StaffCreateRequest;
import com.brightloop.staff.dto.StaffResponse;
import com.brightloop.staff.dto.StaffUpdateRequest;
import com.brightloop.staff.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {
    private final StaffService staffService;

    public StaffController(StaffService staffService) { this.staffService = staffService; }

    @GetMapping
    public Page<StaffResponse> list(@RequestParam(required = false) Boolean active, Pageable pageable) {
        return staffService.list(active, pageable);
    }

    @PostMapping
    public ResponseEntity<StaffResponse> create(@Valid @RequestBody StaffCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.create(request));
    }

    @GetMapping("/me")
    public StaffResponse getMine(@AuthenticationPrincipal String email) { return staffService.getMine(email); }

    @GetMapping("/{id}")
    public StaffResponse get(@PathVariable UUID id) { return staffService.get(id); }

    @PutMapping("/{id}")
    public StaffResponse update(@PathVariable UUID id, @Valid @RequestBody StaffUpdateRequest request) {
        return staffService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ApiMessage deactivate(@PathVariable UUID id) {
        staffService.deactivate(id);
        return new ApiMessage("Staff account deactivated");
    }
}
