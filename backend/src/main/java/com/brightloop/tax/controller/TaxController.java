package com.brightloop.tax.controller;
import com.brightloop.common.response.ApiMessage;
import com.brightloop.tax.dto.*;
import com.brightloop.tax.service.TaxService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/taxes") public class TaxController{
 private final TaxService service;public TaxController(TaxService s){service=s;}
 @GetMapping @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public List<TaxRuleResponse> list(){return service.list();}
 @PostMapping @PreAuthorize("hasRole('ADMIN')") public TaxRuleResponse create(@Valid @RequestBody TaxRuleRequest r){return service.create(r);}
 @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public TaxRuleResponse update(@PathVariable UUID id,@Valid @RequestBody TaxRuleRequest r){return service.update(id,r);}
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiMessage archive(@PathVariable UUID id){service.archive(id);return new ApiMessage("Tax rule archived");}
}
