package com.brightloop.quotation.service;

import com.brightloop.common.enums.QuotationStatus;
import com.brightloop.common.exception.ConflictException;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.customer.entity.Customer;
import com.brightloop.customer.repository.CustomerRepository;
import com.brightloop.product.entity.Product;
import com.brightloop.product.entity.ProductVariant;
import com.brightloop.product.repository.ProductRepository;
import com.brightloop.quotation.dto.*;
import com.brightloop.quotation.entity.*;
import com.brightloop.quotation.repository.*;
import com.brightloop.notification.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class QuotationService {
 private final QuotationRepository quotations; private final QuotationTemplateRepository templates; private final CustomerRepository customers; private final ProductRepository products; private final NotificationService notifications;
 public QuotationService(QuotationRepository q,QuotationTemplateRepository t,CustomerRepository c,ProductRepository p,NotificationService n){quotations=q;templates=t;customers=c;products=p;notifications=n;}
 @Transactional(readOnly=true) public Page<QuotationResponse> list(String email,boolean privileged,Pageable page){Page<Quotation> result=privileged?quotations.findAllByOrderByCreatedAtDesc(page):quotations.findByCustomer_User_EmailOrderByCreatedAtDesc(email,page);return result.map(this::map);}
 @Transactional(readOnly=true) public QuotationResponse get(UUID id,String email,boolean privileged){return map(require(id,email,privileged));}
 @Transactional public QuotationResponse create(QuotationRequest request){Customer customer=customers.findById(request.customerId()).filter(c->!c.isArchived()).orElseThrow(()->new ResourceNotFoundException("Customer not found"));QuotationTemplate template=request.templateId()==null?null:templates.findByIdAndActiveTrue(request.templateId()).orElseThrow(()->new ResourceNotFoundException("Quotation template not found"));int days=template==null?30:template.getValidityDays();Quotation quote=new Quotation(newNumber(),customer,template,LocalDate.now().plusDays(days));quote.replaceItems(buildItems(request.items()));return map(quotations.save(quote));}
 @Transactional public QuotationResponse update(UUID id,QuotationRequest request){Quotation quote=quotations.findWithDetailsById(id).orElseThrow(()->new ResourceNotFoundException("Quotation not found"));if(quote.getStatus()!=QuotationStatus.DRAFT)throw new ConflictException("Only draft quotations can be changed");Customer customer=customers.findById(request.customerId()).filter(c->!c.isArchived()).orElseThrow(()->new ResourceNotFoundException("Customer not found"));QuotationTemplate template=request.templateId()==null?null:templates.findByIdAndActiveTrue(request.templateId()).orElseThrow(()->new ResourceNotFoundException("Quotation template not found"));quote.revise(customer,template,LocalDate.now().plusDays(template==null?30:template.getValidityDays()),buildItems(request.items()));return map(quote);}
 @Transactional public QuotationResponse send(UUID id){Quotation q=quotations.findWithDetailsById(id).orElseThrow(()->new ResourceNotFoundException("Quotation not found"));try{q.send();}catch(IllegalStateException e){throw new ConflictException(e.getMessage());}notifications.enqueue(q.getCustomer().getEmail(),"QUOTATION","Quotation "+q.getNumber(),"Your quotation is ready. Valid until "+q.getValidityDate()+". Total: "+q.getTotalAmount());return map(q);}
 @Transactional public QuotationResponse confirm(UUID id,String email,boolean privileged){Quotation q=require(id,email,privileged);try{q.confirm();}catch(IllegalStateException e){throw new ConflictException(e.getMessage());}return map(q);}
 @Transactional(readOnly=true) public Quotation requireConfirmed(UUID id,UUID customerId){Quotation q=quotations.findWithDetailsById(id).orElseThrow(()->new ResourceNotFoundException("Quotation not found"));if(q.getStatus()!=QuotationStatus.CONFIRMED)throw new ConflictException("Quotation must be confirmed before it can create a subscription");if(!q.getCustomer().getId().equals(customerId))throw new ConflictException("Quotation belongs to a different customer");return q;}
 @Transactional public int expireDue(){int expired=0;List<QuotationStatus> statuses=List.of(QuotationStatus.DRAFT,QuotationStatus.QUOTATION);Pageable batch=org.springframework.data.domain.PageRequest.of(0,500);while(true){List<Quotation> due=quotations.findByStatusInAndValidityDateBefore(statuses,LocalDate.now(),batch).getContent();if(due.isEmpty())break;due.forEach(Quotation::expire);expired+=due.size();}return expired;}
 private Quotation require(UUID id,String email,boolean privileged){Quotation q=quotations.findWithDetailsById(id).orElseThrow(()->new ResourceNotFoundException("Quotation not found"));if(!privileged&&!q.getCustomer().getEmail().equalsIgnoreCase(email))throw new AccessDeniedException("You cannot access this quotation");return q;}
 private List<QuotationItem> buildItems(List<QuotationRequest.ItemInput> inputs){if(inputs.isEmpty())throw new ConflictException("A quotation must contain at least one item");List<QuotationItem> result=new ArrayList<>();for(var line:inputs){Product p=products.findByIdAndActiveTrue(line.productId()).orElseThrow(()->new ResourceNotFoundException("Product not found"));String variantName=null;var price=p.getSalesPrice();if(line.variantId()!=null){ProductVariant v=p.getVariants().stream().filter(x->x.getId().equals(line.variantId())).findFirst().orElseThrow(()->new ConflictException("Variant does not belong to the selected product"));variantName=v.getName();price=v.finalPrice(p);}result.add(new QuotationItem(p.getId().toString(),p.getName(),variantName,line.quantity(),price));}return result;}
 private String newNumber(){String number;do{number="QT-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(Locale.ROOT);}while(quotations.existsByNumber(number));return number;}
 private QuotationResponse map(Quotation q){return new QuotationResponse(q.getId(),q.getNumber(),q.getCustomer().getId(),q.getCustomer().getName(),q.getTemplate()==null?null:q.getTemplate().getId(),q.getValidityDate(),q.getStatus(),q.getSubtotal(),q.getDiscountAmount(),q.getTaxAmount(),q.getTotalAmount(),q.getCreatedAt(),q.getItems().stream().map(i->new QuotationResponse.Item(i.getId(),i.getProductId(),i.getProductName(),i.getVariantName(),i.getQuantity(),i.getUnitPrice(),i.getLineTotal())).toList());}
}
