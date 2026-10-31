package com.brightloop.staff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StaffUpdateRequest(@NotBlank @Size(max = 160) String name,
                                @Size(max = 40) String phone) { }
