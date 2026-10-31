package com.brightloop.product.service;

import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.product.dto.ProductRequest;
import com.brightloop.product.dto.ProductResponse;
import com.brightloop.product.entity.Product;
import com.brightloop.product.entity.ProductVariant;
import com.brightloop.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository products;
    public ProductService(ProductRepository products){this.products=products;}
    @Transactional(readOnly=true) public Page<ProductResponse> list(Pageable page){return products.findByActiveTrue(page).map(this::toResponse);}
    @Transactional(readOnly=true) public ProductResponse get(UUID id){return toResponse(find(id));}
    @Transactional public ProductResponse create(ProductRequest request){Product product=new Product(request.name(),request.productType(),request.salesPrice(),request.costPrice(),request.notes());applyVariants(product,request);return toResponse(products.save(product));}
    @Transactional public ProductResponse update(UUID id,ProductRequest request){Product product=find(id);product.update(request.name(),request.productType(),request.salesPrice(),request.costPrice(),request.notes());applyVariants(product,request);return toResponse(product);}
    @Transactional public void archive(UUID id){find(id).archive();}
    private Product find(UUID id){return products.findByIdAndActiveTrue(id).orElseThrow(()->new ResourceNotFoundException("Product not found"));}
    private void applyVariants(Product p,ProductRequest r){p.replaceVariants(r.variants().stream().map(v->new ProductVariant(v.name(),v.additionalPrice())).toList());}
    private ProductResponse toResponse(Product p){return new ProductResponse(p.getId(),p.getName(),p.getProductType(),p.getSalesPrice(),p.getCostPrice(),p.getNotes(),p.isActive(),p.getCreatedAt(),p.getUpdatedAt(),p.getVariants().stream().map(v->new ProductResponse.VariantResponse(v.getId(),v.getName(),v.getAdditionalPrice(),v.finalPrice(p))).toList());}
}
