package com.brightloop.quotation.controller;
import com.brightloop.common.response.ApiMessage;
import com.brightloop.quotation.dto.*;
import com.brightloop.quotation.service.QuotationTemplateService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/quotation-templates") public class QuotationTemplateController{
 private final QuotationTemplateService service;
 public QuotationTemplateController(QuotationTemplateService service){this.service=service;}
 @GetMapping public List<TemplateResponse> list(){return service.list();}
 @PostMapping @PreAuthorize("hasRole('ADMIN')") public TemplateResponse create(@Valid @RequestBody TemplateRequest r){return service.create(r);}
 @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public TemplateResponse update(@PathVariable UUID id,@Valid @RequestBody TemplateRequest r){return service.update(id,r);}
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiMessage archive(@PathVariable UUID id){service.archive(id);return new ApiMessage("Quotation template archived");}
}
