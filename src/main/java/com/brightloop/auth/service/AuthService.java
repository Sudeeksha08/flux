package com.brightloop.auth.service;

import com.brightloop.auth.dto.AuthResponse;
import com.brightloop.auth.dto.LoginRequest;
import com.brightloop.auth.dto.RegisterRequest;
import com.brightloop.auth.entity.PasswordResetToken;
import com.brightloop.auth.entity.Role;
import com.brightloop.auth.entity.User;
import com.brightloop.auth.repository.PasswordResetTokenRepository;
import com.brightloop.auth.repository.RoleRepository;
import com.brightloop.auth.repository.UserRepository;
import com.brightloop.common.enums.RoleName;
import com.brightloop.common.exception.ConflictException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.security.JwtService;
import com.brightloop.security.RevokedToken;
import com.brightloop.security.RevokedTokenRepository;
import io.jsonwebtoken.Claims;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordResetTokenRepository resetTokens;
    private final RevokedTokenRepository revokedTokens;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetMailer resetMailer;

    public AuthService(UserRepository users, RoleRepository roles,
                       PasswordResetTokenRepository resetTokens, RevokedTokenRepository revokedTokens,
                       AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder,
                       JwtService jwtService, PasswordResetMailer resetMailer) {
        this.users = users;
        this.roles = roles;
        this.resetTokens = resetTokens;
        this.revokedTokens = revokedTokens;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.resetMailer = resetMailer;
    }

    @Transactional
    public void registerCustomer(RegisterRequest request) { createAccount(request, RoleName.CUSTOMER); }

    @Transactional
    public User createStaff(RegisterRequest request) { return createAccount(request, RoleName.STAFF); }

    private User createAccount(RegisterRequest request, RoleName roleName) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmail(email)) throw new ConflictException("An account with this email already exists");
        Role role = roles.findById(roleName)
                .orElseThrow(() -> new IllegalStateException("Required role is missing from the database"));
        try {
            return users.saveAndFlush(new User(email, passwordEncoder.encode(request.password()), Set.of(role)));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("An account with this email already exists");
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = users.findByEmail(email).orElseThrow();
        JwtService.IssuedToken token = jwtService.issue(user);
        return new AuthResponse(token.value(), "Bearer", token.expiresAt(), user.getId(), user.getEmail(),
                user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toUnmodifiableSet()));
    }

    @Transactional
    public void logout(String bearerToken) {
        Claims claims = jwtService.parse(bearerToken);
        String tokenId = claims.getId();
        if (tokenId != null && !revokedTokens.existsById(tokenId)) {
            revokedTokens.save(new RevokedToken(tokenId, claims.getExpiration().toInstant()));
        }
    }

    @Transactional
    public void requestPasswordReset(String suppliedEmail) {
        String email = normalizeEmail(suppliedEmail);
        users.findByEmail(email).ifPresent(user -> {
            resetTokens.deleteAllByUser(user);
            byte[] random = new byte[32];
            SECURE_RANDOM.nextBytes(random);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
            resetTokens.save(new PasswordResetToken(user, sha256(rawToken), Instant.now().plusSeconds(1800)));
            resetMailer.send(user.getEmail(), rawToken);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = resetTokens.findByTokenHash(sha256(rawToken))
                .orElseThrow(() -> new ResourceNotFoundException("Password reset token is invalid or expired"));
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(Instant.now())) {
            throw new ResourceNotFoundException("Password reset token is invalid or expired");
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        token.markUsed();
    }

    private String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
