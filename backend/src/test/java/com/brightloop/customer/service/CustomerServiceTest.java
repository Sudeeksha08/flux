package com.brightloop.customer.service;

import com.brightloop.auth.entity.Role;
import com.brightloop.auth.entity.User;
import com.brightloop.auth.repository.UserRepository;
import com.brightloop.common.enums.RoleName;
import com.brightloop.customer.entity.Customer;
import com.brightloop.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockMakers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock(mockMaker = MockMakers.SUBCLASS) CustomerRepository customers;
    @Mock(mockMaker = MockMakers.SUBCLASS) UserRepository users;
    private CustomerService service;

    @BeforeEach
    void setUp() { service = new CustomerService(customers, users); }

    @Test
    void customerCanReadTheirOwnProfile() {
        UUID id = UUID.randomUUID();
        User owner = new User("owner@example.com", "hash", Set.of(new Role(RoleName.CUSTOMER)));
        when(customers.findById(id)).thenReturn(Optional.of(new Customer(owner, "Owner", null, null, null)));

        var response = service.getById(id, "owner@example.com",
                Set.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        assertEquals("owner@example.com", response.email());
    }

    @Test
    void customerCannotReadAnotherCustomersProfile() {
        UUID id = UUID.randomUUID();
        User owner = new User("owner@example.com", "hash", Set.of(new Role(RoleName.CUSTOMER)));
        when(customers.findById(id)).thenReturn(Optional.of(new Customer(owner, "Owner", null, null, null)));

        assertThrows(AccessDeniedException.class, () -> service.getById(id, "intruder@example.com",
                Set.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))));
    }
}
