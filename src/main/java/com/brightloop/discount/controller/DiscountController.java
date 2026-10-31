package com.brightloop.discount.controller;
import com.brightloop.common.response.ApiMessage;
import com.brightloop.discount.dto.*;
import com.brightloop.discount.service.DiscountService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/discounts") public class DiscountController{
 private final DiscountService service;public DiscountController(DiscountService s){service=s;}
 @GetMapping @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public List<DiscountResponse> list(){return service.list();}
 @PostMapping @PreAuthorize("hasRole('ADMIN')") public DiscountResponse create(@Valid @RequestBody DiscountRequest r){return service.create(r);}
 @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public DiscountResponse update(@PathVariable UUID id,@Valid @RequestBody DiscountRequest r){return service.update(id,r);}
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiMessage archive(@PathVariable UUID id){service.archive(id);return new ApiMessage("Discount archived");}
}
