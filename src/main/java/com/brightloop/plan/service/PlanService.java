package com.brightloop.plan.service;

import com.brightloop.common.exception.ConflictException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.plan.dto.PlanRequest;
import com.brightloop.plan.dto.PlanResponse;
import com.brightloop.plan.entity.Plan;
import com.brightloop.plan.entity.PlanProduct;
import com.brightloop.plan.repository.PlanRepository;
import com.brightloop.product.entity.Product;
import com.brightloop.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class PlanService {
    private final PlanRepository plans; private final ProductRepository products;
    public PlanService(PlanRepository plans,ProductRepository products){this.plans=plans;this.products=products;}
    @Transactional(readOnly=true) public Page<PlanResponse> list(Pageable pageable){return plans.findByActiveTrue(pageable).map(this::toResponse);}
    @Transactional(readOnly=true) public PlanResponse get(UUID id){return toResponse(find(id));}
    @Transactional public PlanResponse create(PlanRequest r){Plan p=new Plan(r.name(),r.price(),r.billingFrequency(),r.minimumQuantity(),r.availableFrom(),r.availableTo(),r.autoClose(),r.earlyCloseAllowed(),r.pauseAllowed(),r.renewalAllowed());applyProducts(p,r);return toResponse(plans.save(p));}
    @Transactional public PlanResponse update(UUID id,PlanRequest r){Plan p=find(id);p.update(r.name(),r.price(),r.billingFrequency(),r.minimumQuantity(),r.availableFrom(),r.availableTo(),r.autoClose(),r.earlyCloseAllowed(),r.pauseAllowed(),r.renewalAllowed());applyProducts(p,r);return toResponse(p);}
    @Transactional public void archive(UUID id){find(id).archive();}
    @Transactional(readOnly=true) public Plan requireAvailable(UUID id,LocalDate date){Plan p=find(id);try{p.validateAvailable(date);}catch(IllegalStateException e){throw new ConflictException(e.getMessage());}return p;}
    private Plan find(UUID id){return plans.findByIdAndActiveTrue(id).orElseThrow(()->new ResourceNotFoundException("Plan not found"));}
    private void applyProducts(Plan plan,PlanRequest r){Set<UUID> ids=new HashSet<>();List<PlanProduct> items=new ArrayList<>();for(var item:r.products()){if(!ids.add(item.productId()))throw new ConflictException("A product can only appear once in a plan");Product product=products.findByIdAndActiveTrue(item.productId()).orElseThrow(()->new ResourceNotFoundException("Product not found: "+item.productId()));items.add(new PlanProduct(product,item.quantity()));}plan.replaceProducts(items);}
    private PlanResponse toResponse(Plan p){return new PlanResponse(p.getId(),p.getName(),p.getPrice(),p.getBillingFrequency(),p.getMinimumQuantity(),p.getAvailableFrom(),p.getAvailableTo(),p.isAutoClose(),p.isEarlyCloseAllowed(),p.isPauseAllowed(),p.isRenewalAllowed(),p.isActive(),p.getProducts().stream().map(x->new PlanResponse.ProductItem(x.getProduct().getId(),x.getProduct().getName(),x.getQuantity())).toList(),p.getCreatedAt(),p.getUpdatedAt());}
}
