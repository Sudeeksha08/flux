package com.brightloop.invoice.controller;
import com.brightloop.common.response.ApiMessage;
import com.brightloop.invoice.dto.*;
import com.brightloop.invoice.service.*;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/invoices") public class InvoiceController{
 private final InvoiceService service;private final InvoicePdfService pdf;
 public InvoiceController(InvoiceService s,InvoicePdfService p){service=s;pdf=p;}
 @GetMapping public Page<InvoiceResponse> list(Pageable page,Authentication a){return service.list(a.getName(),privileged(a),page);}
 @PostMapping("/generate") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public ResponseEntity<InvoiceResponse> generate(@Valid @RequestBody InvoiceGenerateRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.generate(r.subscriptionId(),r.billingPeriodStart()));}
 @GetMapping("/overdue") public Page<InvoiceResponse> overdue(Pageable page,Authentication a){return service.overdue(a.getName(),privileged(a),page);}
 @GetMapping("/{id}") public InvoiceResponse get(@PathVariable UUID id,Authentication a){return service.get(id,a.getName(),privileged(a));}
 @GetMapping("/{id}/pdf") public ResponseEntity<ByteArrayResource> pdf(@PathVariable UUID id,Authentication a){byte[] bytes=pdf.render(service.get(id,a.getName(),privileged(a)));return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"invoice-"+id+".pdf\"").body(new ByteArrayResource(bytes));}
 @PostMapping("/{id}/confirm") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public InvoiceResponse confirm(@PathVariable UUID id){return service.confirm(id);}
 @PostMapping("/{id}/cancel") @PreAuthorize("hasRole('ADMIN')") public ApiMessage cancel(@PathVariable UUID id){service.cancel(id);return new ApiMessage("Invoice cancelled");}
 private boolean privileged(Authentication a){return a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")||x.getAuthority().equals("ROLE_STAFF"));}
}
