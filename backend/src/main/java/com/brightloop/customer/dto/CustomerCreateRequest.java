package com.brightloop.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerCreateRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 180) String companyName,
        @Size(max = 40) String phone,
        @Size(max = 500) String address) { }
