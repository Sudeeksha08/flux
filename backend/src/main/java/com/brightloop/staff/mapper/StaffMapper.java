package com.brightloop.staff.mapper;

import com.brightloop.staff.dto.StaffResponse;
import com.brightloop.staff.entity.Staff;

public final class StaffMapper {
    private StaffMapper() { }

    public static StaffResponse toResponse(Staff staff) {
        return new StaffResponse(staff.getId(), staff.getUser().getId(), staff.getName(), staff.getEmail(),
                staff.getPhone(), staff.isActive(), staff.getCreatedAt());
    }
}
