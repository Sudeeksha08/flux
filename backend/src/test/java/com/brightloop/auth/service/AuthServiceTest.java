package com.brightloop.auth.service;

import com.brightloop.auth.dto.RegisterRequest;
import com.brightloop.auth.entity.Role;
import com.brightloop.auth.entity.User;
import com.brightloop.auth.repository.PasswordResetTokenRepository;
import com.brightloop.auth.repository.RoleRepository;
import com.brightloop.auth.repository.UserRepository;
import com.brightloop.common.enums.RoleName;
import com.brightloop.common.exception.ConflictException;
import com.brightloop.security.JwtService;
import com.brightloop.security.RevokedTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockMakers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock(mockMaker = MockMakers.SUBCLASS) UserRepository users;
    @Mock(mockMaker = MockMakers.SUBCLASS) RoleRepository roles;
    @Mock(mockMaker = MockMakers.SUBCLASS) PasswordResetTokenRepository resetTokens;
    @Mock(mockMaker = MockMakers.SUBCLASS) RevokedTokenRepository revokedTokens;
    @Mock(mockMaker = MockMakers.SUBCLASS) AuthenticationManager authenticationManager;
    @Mock(mockMaker = MockMakers.SUBCLASS) PasswordEncoder passwordEncoder;
    @Mock(mockMaker = MockMakers.SUBCLASS) JwtService jwtService;
    @Mock(mockMaker = MockMakers.SUBCLASS) PasswordResetMailer resetMailer;
    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(users, roles, resetTokens, revokedTokens,
                authenticationManager, passwordEncoder, jwtService, resetMailer);
    }

    @Test
    void registrationAlwaysAssignsCustomerRoleAndHashesPassword() {
        Role customer = new Role(RoleName.CUSTOMER);
        when(users.existsByEmail("person@example.com")).thenReturn(false);
        when(roles.findById(RoleName.CUSTOMER)).thenReturn(Optional.of(customer));
        when(passwordEncoder.encode("Str0ng!Pass")).thenReturn("bcrypt-hash");

        service.registerCustomer(new RegisterRequest(" Person@Example.com ", "Str0ng!Pass"));

        ArgumentCaptor<User> account = ArgumentCaptor.forClass(User.class);
        verify(users).saveAndFlush(account.capture());
        assertEquals("person@example.com", account.getValue().getEmail());
        assertEquals("bcrypt-hash", account.getValue().getPasswordHash());
        assertEquals(RoleName.CUSTOMER, account.getValue().getRoles().iterator().next().getName());
    }

    @Test
    void duplicateEmailIsRejectedBeforeCreatingAccount() {
        when(users.existsByEmail("person@example.com")).thenReturn(true);
        assertThrows(ConflictException.class, () ->
                service.registerCustomer(new RegisterRequest("person@example.com", "Str0ng!Pass")));
        verify(users, never()).saveAndFlush(any());
    }
}
