package com.brightloop.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerProfileRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 180) String companyName,
        @Size(max = 40) String phone,
        @Size(max = 500) String address) { }
