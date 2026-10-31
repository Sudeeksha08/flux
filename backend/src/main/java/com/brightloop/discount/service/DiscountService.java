package com.brightloop.discount.service;
import com.brightloop.common.exception.*;
import com.brightloop.discount.dto.*;
import com.brightloop.discount.entity.*;
import com.brightloop.discount.repository.*;
import com.brightloop.product.entity.Product;
import com.brightloop.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
@Service public class DiscountService{
 private final DiscountRepository discounts;private final DiscountUsageRepository usages;private final ProductRepository products;
 public DiscountService(DiscountRepository d,DiscountUsageRepository u,ProductRepository p){discounts=d;usages=u;products=p;}
 @Transactional(readOnly=true) public List<DiscountResponse> list(){return discounts.findByActiveTrueOrderByNameAsc().stream().map(this::map).toList();}
 @Transactional public DiscountResponse create(DiscountRequest r){return map(discounts.save(new Discount(r.name(),r.type(),r.value(),r.minimumPurchase(),r.minimumQuantity(),r.startDate(),r.endDate(),r.usageLimit(),resolve(r.productIds()))));}
 @Transactional public DiscountResponse update(UUID id,DiscountRequest r){Discount d=discounts.findByIdAndActiveTrue(id).orElseThrow(()->new ResourceNotFoundException("Discount not found"));d.update(r.name(),r.type(),r.value(),r.minimumPurchase(),r.minimumQuantity(),r.startDate(),r.endDate(),r.usageLimit(),resolve(r.productIds()));return map(d);}
 @Transactional public void archive(UUID id){discounts.findByIdAndActiveTrue(id).orElseThrow(()->new ResourceNotFoundException("Discount not found")).archive();}
 @Transactional(readOnly=true) public DiscountApplication preview(BigDecimal subtotal,Map<UUID,BigDecimal> productBases,int quantity,LocalDate date){return best(discounts.findByActiveTrueOrderByNameAsc(),subtotal,productBases,quantity,date);}
 @Transactional public DiscountApplication applyBest(String reference,BigDecimal subtotal,Map<UUID,BigDecimal> productBases,int quantity,LocalDate date){Optional<DiscountUsage> prior=usages.findFirstByBillingReference(reference);if(prior.isPresent()){DiscountUsage u=prior.get();return application(u.getDiscount(),u.getAmount());}List<Discount> locked=discounts.lockActiveDiscounts();prior=usages.findFirstByBillingReference(reference);if(prior.isPresent()){DiscountUsage u=prior.get();return application(u.getDiscount(),u.getAmount());}DiscountApplication chosen=best(locked,subtotal,productBases,quantity,date);if(chosen.discountId()!=null){Discount d=locked.stream().filter(x->x.getId().equals(chosen.discountId())).findFirst().orElseThrow();d.incrementUsage();usages.save(new DiscountUsage(d,reference,chosen.amount()));}return chosen;}
 @Transactional public void linkInvoice(String reference,UUID invoiceId){usages.findFirstByBillingReference(reference).ifPresent(usage->usage.linkInvoice(invoiceId));}
 private DiscountApplication best(List<Discount> candidates,BigDecimal subtotal,Map<UUID,BigDecimal> perProduct,int quantity,LocalDate date){Set<UUID> ids=perProduct.keySet();Discount best=null;BigDecimal bestAmount=BigDecimal.ZERO;for(Discount d:candidates){if(!d.eligible(subtotal,quantity,ids,date))continue;BigDecimal eligibleBase=d.getProducts().isEmpty()?subtotal:d.getProducts().stream().filter(p->perProduct.containsKey(p.getId())).map(p->perProduct.get(p.getId())).reduce(BigDecimal.ZERO,BigDecimal::add);BigDecimal amount=d.calculate(eligibleBase);if(amount.compareTo(bestAmount)>0){best=d;bestAmount=amount;}}return best==null?DiscountApplication.none():application(best,bestAmount);}
 private DiscountApplication application(Discount d,BigDecimal amount){return new DiscountApplication(d.getId(),d.getName(),amount,d.getProducts().stream().map(Product::getId).collect(Collectors.toUnmodifiableSet()));}
 private Set<Product> resolve(Set<UUID> ids){if(ids==null||ids.isEmpty())return Set.of();List<Product> found=products.findAllById(ids);if(found.size()!=ids.size()||found.stream().anyMatch(p->!p.isActive()))throw new ResourceNotFoundException("One or more applicable products were not found");return new HashSet<>(found);}
 private DiscountResponse map(Discount d){return new DiscountResponse(d.getId(),d.getName(),d.getType(),d.getValue(),d.getMinimumPurchase(),d.getMinimumQuantity(),d.getStartDate(),d.getEndDate(),d.getUsageLimit(),d.getUsageCount(),d.isActive(),d.getProducts().stream().map(Product::getId).collect(Collectors.toUnmodifiableSet()));}
 public record DiscountApplication(UUID discountId,String name,BigDecimal amount,Set<UUID> applicableProductIds){public static DiscountApplication none(){return new DiscountApplication(null,null,BigDecimal.ZERO,Set.of());}}
}
