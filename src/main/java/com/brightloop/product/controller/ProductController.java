package com.brightloop.product.controller;

import com.brightloop.common.response.ApiMessage;
import com.brightloop.product.dto.ProductRequest;
import com.brightloop.product.dto.ProductResponse;
import com.brightloop.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service){this.service=service;}
    @GetMapping public Page<ProductResponse> list(Pageable pageable){return service.list(pageable);}
    @GetMapping("/{id}") public ProductResponse get(@PathVariable UUID id){return service.get(id);}
    @PostMapping @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));}
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ProductResponse update(@PathVariable UUID id,@Valid @RequestBody ProductRequest request){return service.update(id,request);}
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ApiMessage archive(@PathVariable UUID id){service.archive(id);return new ApiMessage("Product archived");}
}
