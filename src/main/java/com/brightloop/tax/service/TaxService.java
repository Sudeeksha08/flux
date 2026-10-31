package com.brightloop.tax.service;
import com.brightloop.common.enums.TaxType;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.product.entity.Product;
import com.brightloop.product.repository.ProductRepository;
import com.brightloop.tax.dto.*;
import com.brightloop.tax.entity.TaxRule;
import com.brightloop.tax.repository.TaxRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.LocalDate;
import java.util.*;
@Service public class TaxService{
 private final TaxRuleRepository rules;private final ProductRepository products;
 public TaxService(TaxRuleRepository r,ProductRepository p){rules=r;products=p;}
 @Transactional(readOnly=true) public List<TaxRuleResponse> list(){return rules.findByActiveTrueOrderByNameAsc().stream().map(this::map).toList();}
 @Transactional public TaxRuleResponse create(TaxRuleRequest r){return map(rules.save(new TaxRule(r.name(),r.percentage(),r.type(),r.effectiveFrom(),r.effectiveTo(),resolve(r.productIds()))));}
 @Transactional public TaxRuleResponse update(UUID id,TaxRuleRequest r){TaxRule t=rules.findByIdAndActiveTrue(id).orElseThrow(()->new ResourceNotFoundException("Tax rule not found"));t.update(r.name(),r.percentage(),r.type(),r.effectiveFrom(),r.effectiveTo(),resolve(r.productIds()));return map(t);}
 @Transactional public void archive(UUID id){rules.findByIdAndActiveTrue(id).orElseThrow(()->new ResourceNotFoundException("Tax rule not found")).archive();}
 @Transactional(readOnly=true) public TaxCalculation calculate(UUID productId,BigDecimal base,LocalDate date){List<TaxSnapshot> applied=new ArrayList<>();BigDecimal total=BigDecimal.ZERO,added=BigDecimal.ZERO;for(TaxRule r:rules.findByActiveTrueOrderByNameAsc()){if(r.appliesTo(productId,date)){BigDecimal amount=r.getType()==TaxType.INCLUSIVE?base.multiply(r.getPercentage()).divide(new BigDecimal("100").add(r.getPercentage()),4,RoundingMode.HALF_UP):base.multiply(r.getPercentage()).divide(new BigDecimal("100"),4,RoundingMode.HALF_UP);applied.add(new TaxSnapshot(r.getId(),r.getName(),r.getPercentage(),r.getType(),amount));total=total.add(amount);if(r.getType()==TaxType.EXCLUSIVE)added=added.add(amount);}}return new TaxCalculation(total,added,applied);}
 @Transactional(readOnly=true) public TaxCalculation calculate(BigDecimal base,Set<UUID> productIds,LocalDate date){BigDecimal total=BigDecimal.ZERO,added=BigDecimal.ZERO;List<TaxSnapshot> all=new ArrayList<>();for(UUID id:productIds){var result=calculate(id,base,date);total=total.add(result.amount());added=added.add(result.addedAmount());all.addAll(result.rules());}return new TaxCalculation(total,added,all);}
 private Set<Product> resolve(Set<UUID> ids){if(ids==null||ids.isEmpty())return Set.of();List<Product> found=products.findAllById(ids);if(found.size()!=ids.size()||found.stream().anyMatch(p->!p.isActive()))throw new ResourceNotFoundException("One or more applicable products were not found");return new HashSet<>(found);}
 private TaxRuleResponse map(TaxRule t){return new TaxRuleResponse(t.getId(),t.getName(),t.getPercentage(),t.getType(),t.getEffectiveFrom(),t.getEffectiveTo(),t.isActive(),t.getProducts().stream().map(Product::getId).collect(java.util.stream.Collectors.toUnmodifiableSet()));}
 public record TaxSnapshot(UUID id,String name,BigDecimal percentage,TaxType type,BigDecimal amount){}
 public record TaxCalculation(BigDecimal amount,BigDecimal addedAmount,List<TaxSnapshot> rules){}
}
