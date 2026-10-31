package com.brightloop.staff.dto;

import java.time.Instant;
import java.util.UUID;

public record StaffResponse(UUID id, UUID userId, String name, String email,
                            String phone, boolean active, Instant createdAt) { }
