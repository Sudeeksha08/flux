package com.brightloop.auth.controller;

import com.brightloop.auth.dto.AuthResponse;
import com.brightloop.auth.dto.ForgotPasswordRequest;
import com.brightloop.auth.dto.LoginRequest;
import com.brightloop.auth.dto.RegisterRequest;
import com.brightloop.auth.dto.ResetPasswordRequest;
import com.brightloop.auth.service.AuthService;
import com.brightloop.common.response.ApiMessage;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<ApiMessage> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiMessage("Customer account created"));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public ApiMessage logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        authService.logout(authorization.substring("Bearer ".length()));
        return new ApiMessage("Logged out");
    }

    @PostMapping("/forgot-password")
    public ApiMessage forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.email());
        return new ApiMessage("If the account exists, password reset instructions will be sent");
    }

    @PostMapping("/reset-password")
    public ApiMessage resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return new ApiMessage("Password reset successfully");
    }
}
