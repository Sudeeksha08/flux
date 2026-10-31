package com.brightloop.staff.service;

import com.brightloop.auth.dto.RegisterRequest;
import com.brightloop.auth.entity.User;
import com.brightloop.auth.repository.UserRepository;
import com.brightloop.auth.service.AuthService;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.staff.dto.StaffCreateRequest;
import com.brightloop.staff.dto.StaffResponse;
import com.brightloop.staff.dto.StaffUpdateRequest;
import com.brightloop.staff.entity.Staff;
import com.brightloop.staff.mapper.StaffMapper;
import com.brightloop.staff.repository.StaffRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class StaffService {
    private final StaffRepository staffRepository;
    private final UserRepository users;
    private final AuthService authService;

    public StaffService(StaffRepository staffRepository, UserRepository users, AuthService authService) {
        this.staffRepository = staffRepository;
        this.users = users;
        this.authService = authService;
    }

    @Transactional
    public StaffResponse create(StaffCreateRequest request) {
        User user = authService.createStaff(new RegisterRequest(request.email(), request.password()));
        return StaffMapper.toResponse(staffRepository.save(new Staff(user, request.name(), request.phone())));
    }

    @Transactional(readOnly = true)
    public Page<StaffResponse> list(Boolean active, Pageable pageable) {
        return staffRepository.search(active, pageable);
    }

    @Transactional(readOnly = true)
    public StaffResponse get(UUID id) { return StaffMapper.toResponse(find(id)); }

    @Transactional(readOnly = true)
    public StaffResponse getMine(String email) {
        return StaffMapper.toResponse(staffRepository.findByUser_Email(normalize(email))
                .filter(Staff::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found")));
    }

    @Transactional
    public StaffResponse update(UUID id, StaffUpdateRequest request) {
        Staff staff = find(id);
        staff.update(request.name(), request.phone());
        return StaffMapper.toResponse(staff);
    }

    @Transactional
    public void deactivate(UUID id) {
        Staff staff = find(id);
        staff.deactivate();
        staff.getUser().disable();
    }

    private Staff find(UUID id) {
        return staffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));
    }

    private String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
