package com.brightloop.plan.controller;

import com.brightloop.common.response.ApiMessage;
import com.brightloop.plan.dto.PlanRequest;
import com.brightloop.plan.dto.PlanResponse;
import com.brightloop.plan.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/plans")
public class PlanController {
    private final PlanService service;
    public PlanController(PlanService service){this.service=service;}
    @GetMapping public Page<PlanResponse> list(Pageable pageable){return service.list(pageable);}
    @GetMapping("/{id}") public PlanResponse get(@PathVariable UUID id){return service.get(id);}
    @PostMapping @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<PlanResponse> create(@Valid @RequestBody PlanRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));}
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public PlanResponse update(@PathVariable UUID id,@Valid @RequestBody PlanRequest request){return service.update(id,request);}
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiMessage archive(@PathVariable UUID id){service.archive(id);return new ApiMessage("Plan archived");}
}
