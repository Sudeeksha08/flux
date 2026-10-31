package com.brightloop.subscription.controller;
import com.brightloop.common.response.ApiMessage;
import com.brightloop.subscription.dto.*;
import com.brightloop.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/subscriptions") public class SubscriptionController{
 private final SubscriptionService service;
 public SubscriptionController(SubscriptionService s){service=s;}
 @GetMapping public Page<SubscriptionResponse> list(Pageable page,Authentication auth){return service.list(auth.getName(),privileged(auth),page);}
 @PostMapping @PreAuthorize("hasAnyRole('ADMIN','STAFF','CUSTOMER')") public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody SubscriptionRequest r,Authentication auth){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r,auth.getName(),privileged(auth)));}
 @GetMapping("/{id}") public SubscriptionResponse get(@PathVariable UUID id,Authentication a){return service.get(id,a.getName(),privileged(a));}
 @PostMapping("/{id}/confirm") public SubscriptionResponse confirm(@PathVariable UUID id,Authentication a){return service.confirm(id,a.getName(),privileged(a));}
 @PostMapping("/{id}/activate") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public SubscriptionResponse activate(@PathVariable UUID id,Authentication a){return service.activate(id,a.getName(),true);}
 @PostMapping("/{id}/pause") public SubscriptionResponse pause(@PathVariable UUID id,Authentication a){return service.pause(id,a.getName(),privileged(a));}
 @PostMapping("/{id}/resume") public SubscriptionResponse resume(@PathVariable UUID id,Authentication a){return service.resume(id,a.getName(),privileged(a));}
 @PostMapping("/{id}/renew") public SubscriptionResponse renew(@PathVariable UUID id,Authentication a){return service.renew(id,a.getName(),privileged(a));}
 @PostMapping("/{id}/close") public SubscriptionResponse close(@PathVariable UUID id,Authentication a){return service.close(id,a.getName(),privileged(a));}
 private boolean privileged(Authentication a){return a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADMIN")||x.getAuthority().equals("ROLE_STAFF"));}
}
