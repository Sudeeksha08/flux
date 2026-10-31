package com.brightloop.quotation.controller;
import com.brightloop.common.response.ApiMessage;
import com.brightloop.quotation.dto.*;
import com.brightloop.quotation.service.QuotationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/quotations") public class QuotationController{
 private final QuotationService service;
 public QuotationController(QuotationService service){this.service=service;}
 @GetMapping public Page<QuotationResponse> list(Pageable page,Authentication auth){return service.list(auth.getName(),privileged(auth),page);}
 @PostMapping @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public ResponseEntity<QuotationResponse> create(@Valid @RequestBody QuotationRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));}
 @GetMapping("/{id}") public QuotationResponse get(@PathVariable UUID id,Authentication auth){return service.get(id,auth.getName(),privileged(auth));}
 @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public QuotationResponse update(@PathVariable UUID id,@Valid @RequestBody QuotationRequest r){return service.update(id,r);}
 @PostMapping("/{id}/send") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public QuotationResponse send(@PathVariable UUID id){return service.send(id);}
 @PostMapping("/{id}/confirm") public QuotationResponse confirm(@PathVariable UUID id,Authentication auth){return service.confirm(id,auth.getName(),privileged(auth));}
 private boolean privileged(Authentication a){return a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")||x.getAuthority().equals("ROLE_STAFF"));}
}
