package com.brightloop.customer.controller;

import com.brightloop.common.response.ApiMessage;
import com.brightloop.customer.dto.CustomerCreateRequest;
import com.brightloop.customer.dto.CustomerProfileRequest;
import com.brightloop.customer.dto.CustomerResponse;
import com.brightloop.customer.service.CustomerService;
import com.brightloop.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final SubscriptionService subscriptionService;

    public CustomerController(CustomerService customerService, SubscriptionService subscriptionService) { this.customerService = customerService; this.subscriptionService=subscriptionService; }

    @GetMapping("/{id}/subscriptions")
    public Page<com.brightloop.subscription.dto.SubscriptionResponse> subscriptionHistory(
            @PathVariable UUID id, @AuthenticationPrincipal String email,
            org.springframework.security.core.Authentication authentication, Pageable pageable) {
        boolean privileged=authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
        return subscriptionService.history(id,email,privileged,pageable);
    }

    @GetMapping
    public Page<CustomerResponse> list(@Size(max = 120) @RequestParam(required = false) String q,
                                       @RequestParam(defaultValue = "false") boolean archived,
                                       Pageable pageable) {
        return customerService.search(q, archived, pageable);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @GetMapping("/me")
    public CustomerResponse getMine(@AuthenticationPrincipal String email) {
        return customerService.getMine(email);
    }

    @PostMapping("/me")
    public ResponseEntity<CustomerResponse> createMine(@AuthenticationPrincipal String email,
                                                       @Valid @RequestBody CustomerProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createMine(email, request));
    }

    @PutMapping("/me")
    public CustomerResponse updateMine(@AuthenticationPrincipal String email,
                                       @Valid @RequestBody CustomerProfileRequest request) {
        return customerService.updateMine(email, request);
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable UUID id, @AuthenticationPrincipal String email,
                                org.springframework.security.core.Authentication authentication) {
        return customerService.getById(id, email, authentication.getAuthorities());
    }

    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable UUID id, @AuthenticationPrincipal String email,
                                   org.springframework.security.core.Authentication authentication,
                                   @Valid @RequestBody CustomerProfileRequest request) {
        return customerService.update(id, email, authentication.getAuthorities(), request);
    }

    @PatchMapping("/{id}/archive")
    public ApiMessage archive(@PathVariable UUID id) {
        customerService.archive(id);
        return new ApiMessage("Customer archived");
    }
}
