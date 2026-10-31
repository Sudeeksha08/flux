package com.brightloop.customer.service;

import com.brightloop.auth.entity.User;
import com.brightloop.auth.repository.UserRepository;
import com.brightloop.common.enums.RoleName;
import com.brightloop.common.exception.ConflictException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.customer.dto.CustomerCreateRequest;
import com.brightloop.customer.dto.CustomerProfileRequest;
import com.brightloop.customer.dto.CustomerResponse;
import com.brightloop.customer.entity.Customer;
import com.brightloop.customer.mapper.CustomerMapper;
import com.brightloop.customer.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customers;
    private final UserRepository users;

    public CustomerService(CustomerRepository customers, UserRepository users) {
        this.customers = customers;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> search(String query, boolean archived, Pageable pageable) {
        return customers.search(query == null ? null : query.trim(), archived, pageable);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(UUID id, String principalEmail, Collection<? extends GrantedAuthority> authorities) {
        Customer customer = getActive(id);
        if (!isPrivileged(authorities) && !customer.getEmail().equalsIgnoreCase(principalEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot access this customer");
        }
        return CustomerMapper.toResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getMine(String principalEmail) {
        return CustomerMapper.toResponse(customers.findByUser_Email(normalize(principalEmail))
                .filter(customer -> !customer.isArchived())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found")));
    }

    @Transactional(readOnly = true)
    public Customer requireActive(UUID id) {
        return customers.findById(id).filter(customer -> !customer.isArchived())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Transactional
    public CustomerResponse createMine(String principalEmail, CustomerProfileRequest request) {
        return create(normalize(principalEmail), request.name(), request.companyName(), request.phone(), request.address());
    }

    @Transactional
    public CustomerResponse create(CustomerCreateRequest request) {
        return create(normalize(request.email()), request.name(), request.companyName(), request.phone(), request.address());
    }

    private CustomerResponse create(String email, String name, String companyName, String phone, String address) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Registered customer account not found"));
        boolean customerRole = user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.CUSTOMER);
        if (!customerRole) throw new ConflictException("A customer profile can only be linked to a CUSTOMER account");
        if (customers.existsByUser_Email(email)) throw new ConflictException("A customer profile already exists for this account");
        return CustomerMapper.toResponse(customers.save(new Customer(user, name, companyName, phone, address)));
    }

    @Transactional
    public CustomerResponse update(UUID id, String principalEmail,
                                  Collection<? extends GrantedAuthority> authorities,
                                  CustomerProfileRequest request) {
        Customer customer = getActive(id);
        if (!isPrivileged(authorities) && !customer.getEmail().equalsIgnoreCase(principalEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot update this customer");
        }
        customer.update(request.name(), request.companyName(), request.phone(), request.address());
        return CustomerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse updateMine(String principalEmail, CustomerProfileRequest request) {
        Customer customer = customers.findByUser_Email(normalize(principalEmail))
                .filter(value -> !value.isArchived())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
        customer.update(request.name(), request.companyName(), request.phone(), request.address());
        return CustomerMapper.toResponse(customer);
    }

    @Transactional
    public void archive(UUID id) {
        Customer customer = customers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customer.archive();
    }

    private Customer getActive(UUID id) {
        return customers.findById(id).filter(customer -> !customer.isArchived())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    private boolean isPrivileged(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                || authority.getAuthority().equals("ROLE_STAFF"));
    }

    private String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
