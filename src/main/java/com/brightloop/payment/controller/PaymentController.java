package com.brightloop.payment.controller;
import com.brightloop.payment.dto.*;
import com.brightloop.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/payments") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public class PaymentController{
 private final PaymentService service;public PaymentController(PaymentService s){service=s;}
 @GetMapping public Page<PaymentResponse> list(Pageable page){return service.list(page);}
 @PostMapping public ResponseEntity<PaymentResponse> record(@Valid @RequestBody PaymentRequest r,Authentication a){return ResponseEntity.status(HttpStatus.CREATED).body(service.record(a.getName(),r));}
 @GetMapping("/{id}") public PaymentResponse get(@PathVariable UUID id){return service.get(id);}
}
