package com.brightloop.subscription.service;

import com.brightloop.common.enums.SubscriptionStatus;
import com.brightloop.common.exception.ConflictException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.customer.dto.CustomerResponse;
import com.brightloop.customer.entity.Customer;
import com.brightloop.customer.service.CustomerService;
import com.brightloop.plan.entity.Plan;
import com.brightloop.plan.entity.PlanProduct;
import com.brightloop.plan.service.PlanService;
import com.brightloop.quotation.entity.Quotation;
import com.brightloop.quotation.entity.QuotationItem;
import com.brightloop.quotation.service.QuotationService;
import com.brightloop.subscription.dto.*;
import com.brightloop.subscription.entity.*;
import com.brightloop.subscription.repository.SubscriptionRepository;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service public class SubscriptionService{
 private final SubscriptionRepository repo;private final CustomerService customerService;private final PlanService planService;private final QuotationService quotationService;
 public SubscriptionService(SubscriptionRepository r,CustomerService c,PlanService p,QuotationService q){repo=r;customerService=c;planService=p;quotationService=q;}
 @Transactional(readOnly=true) public Page<SubscriptionResponse> list(String email,boolean privileged,Pageable page){return (privileged?repo.findAllByOrderByCreatedAtDesc(page):repo.findByCustomer_User_EmailOrderByCreatedAtDesc(email,page)).map(this::map);}
 @Transactional(readOnly=true) public Page<SubscriptionResponse> history(UUID customerId,String email,boolean privileged,Pageable page){Customer c=customerService.requireActive(customerId);if(!privileged&&!c.getEmail().equalsIgnoreCase(email))throw new AccessDeniedException("You cannot access this customer history");return repo.findByCustomer_User_EmailOrderByCreatedAtDesc(c.getEmail(),page).map(this::map);}
 @Transactional(readOnly=true) public SubscriptionResponse get(UUID id,String email,boolean privileged){return map(require(id,email,privileged));}
 @Transactional public SubscriptionResponse create(SubscriptionRequest r,String email,boolean privileged){Customer customer;if(privileged)customer=customerService.requireActive(r.customerId());else{CustomerResponse own=customerService.getMine(email);customer=customerService.requireActive(own.id());}LocalDate start=r.startDate()==null?LocalDate.now():r.startDate();Plan plan=planService.requireAvailable(r.planId(),start);Quotation quote=r.quotationId()==null?null:quotationService.requireConfirmed(r.quotationId(),customer.getId());var total=quote==null?plan.getPrice().multiply(java.math.BigDecimal.valueOf(plan.getMinimumQuantity())):quote.getTotalAmount();Subscription s=new Subscription(newNumber(),customer,plan,quote,start,r.paymentTerms(),total);if(quote!=null)s.replaceItems(quote.getItems().stream().map(this::copyQuoteItem).toList());else s.replaceItems(plan.getProducts().stream().map(this::copyPlanProduct).toList());return map(repo.save(s));}
 @Transactional public SubscriptionResponse confirm(UUID id,String email,boolean privileged){Subscription s=require(id,email,privileged);transition(s::confirm);return map(s);}
 @Transactional public SubscriptionResponse activate(UUID id,String email,boolean privileged){Subscription s=require(id,email,privileged);transition(s::activate);return map(s);}
 @Transactional public SubscriptionResponse pause(UUID id,String email,boolean privileged){Subscription s=require(id,email,privileged);transition(s::pause);return map(s);}
 @Transactional public SubscriptionResponse resume(UUID id,String email,boolean privileged){Subscription s=require(id,email,privileged);transition(s::resume);return map(s);}
 @Transactional public SubscriptionResponse renew(UUID id,String email,boolean privileged){Subscription s=require(id,email,privileged);transition(()->s.renew(LocalDate.now()));return map(s);}
 @Transactional public SubscriptionResponse close(UUID id,String email,boolean privileged){Subscription s=require(id,email,privileged);transition(()->s.close(LocalDate.now()));return map(s);}
 @Transactional public List<Subscription> due(LocalDate date,int size){return repo.findByStatusAndNextBillingDateLessThanEqual(SubscriptionStatus.ACTIVE,date,PageRequest.of(0,size));}
 @Transactional public void completeBilling(UUID id){Subscription s=repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Subscription not found"));s.completeBillingCycle();}
 @Transactional public int closeExpired(int size){List<Subscription> due=repo.findByStatusAndExpiryDateLessThanEqual(SubscriptionStatus.ACTIVE,LocalDate.now(),PageRequest.of(0,size));due.forEach(s->{if(s.getPlan().isAutoClose())s.close(s.getExpiryDate());});return due.size();}
 private Subscription require(UUID id,String email,boolean privileged){Subscription s=repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Subscription not found"));if(!privileged&&!s.getCustomer().getEmail().equalsIgnoreCase(email))throw new AccessDeniedException("You cannot access this subscription");return s;}
 private void transition(Runnable action){try{action.run();}catch(IllegalStateException e){throw new ConflictException(e.getMessage());}}
 private SubscriptionItem copyQuoteItem(QuotationItem i){return new SubscriptionItem(i.getProductId(),i.getProductName(),i.getVariantName(),i.getQuantity(),i.getUnitPrice());}
 private SubscriptionItem copyPlanProduct(PlanProduct item){var p=item.getProduct();return new SubscriptionItem(p.getId().toString(),p.getName(),null,item.getQuantity(),p.getSalesPrice());}
 private String newNumber(){return "SUB-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(Locale.ROOT);}
 private SubscriptionResponse map(Subscription s){return new SubscriptionResponse(s.getId(),s.getNumber(),s.getCustomer().getId(),s.getCustomer().getName(),s.getPlan().getId(),s.getPlan().getName(),s.getQuotation()==null?null:s.getQuotation().getId(),s.getStartDate(),s.getExpiryDate(),s.getNextBillingDate(),s.getPaymentTerms(),s.getTotalAmount(),s.getStatus(),s.getCreatedAt(),s.getItems().stream().map(i->new SubscriptionResponse.Item(i.getId(),i.getProductId(),i.getProductName(),i.getVariantName(),i.getQuantity(),i.getUnitPrice(),i.getLineTotal())).toList());}
}
