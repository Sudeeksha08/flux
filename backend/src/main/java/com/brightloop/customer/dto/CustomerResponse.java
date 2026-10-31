package com.brightloop.customer.dto;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(UUID id, UUID userId, String name, String email,
                               String companyName, String phone, String address,
                               boolean archived, Instant createdAt) { }
