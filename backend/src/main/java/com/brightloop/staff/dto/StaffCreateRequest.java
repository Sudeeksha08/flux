package com.brightloop.staff.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StaffCreateRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 9, max = 72)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9\\s])(?=.{9,72}$).*$",
                message = "Password must be at least 9 characters and include upper, lower, and special characters")
        String password,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 40) String phone) { }
