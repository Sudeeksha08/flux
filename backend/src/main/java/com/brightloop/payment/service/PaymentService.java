package com.brightloop.payment.service;
import com.brightloop.auth.repository.UserRepository;
import com.brightloop.common.exception.*;
import com.brightloop.invoice.dto.InvoiceResponse;
import com.brightloop.invoice.entity.Invoice;
import com.brightloop.invoice.service.InvoiceService;
import com.brightloop.payment.dto.*;
import com.brightloop.payment.entity.*;
import com.brightloop.payment.repository.PaymentRepository;
import com.brightloop.notification.NotificationService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
@Service public class PaymentService{
 private final PaymentRepository payments;private final UserRepository users;private final InvoiceService invoices;private final NotificationService notifications;
 public PaymentService(PaymentRepository p,UserRepository u,InvoiceService i,NotificationService n){payments=p;users=u;invoices=i;notifications=n;}
 @Transactional(readOnly=true) public Page<PaymentResponse> list(Pageable page){return payments.findAllByOrderByPaymentDateDesc(page).map(this::map);}
 @Transactional(readOnly=true) public PaymentResponse get(UUID id){return map(payments.findDetailedById(id).orElseThrow(()->new ResourceNotFoundException("Payment not found")));}
 @Transactional public PaymentResponse record(String email,PaymentRequest r){BigDecimal allocations=r.allocations().stream().map(PaymentRequest.AllocationInput::amount).reduce(BigDecimal.ZERO,BigDecimal::add);if(allocations.compareTo(r.amount())>0)throw new ConflictException("Payment allocations cannot exceed the recorded payment amount");Set<UUID> invoiceIds=new HashSet<>();if(r.allocations().stream().anyMatch(a->!invoiceIds.add(a.invoiceId())))throw new ConflictException("Each invoice can appear only once in a payment");InvoiceResponse first=invoices.paymentTarget(r.allocations().getFirst().invoiceId());UUID customerId=first.customerId();var recorder=users.findByEmail(email.toLowerCase(Locale.ROOT)).orElseThrow(()->new ResourceNotFoundException("User not found"));Payment payment=payments.save(new Payment(recorder,r.method(),r.amount(),r.notes(),r.reference()));for(var allocation:r.allocations().stream().sorted(Comparator.comparing(a->a.invoiceId().toString())).toList()){Invoice invoice=invoices.allocatePayment(allocation.invoiceId(),allocation.amount(),customerId);payment.addAllocation(new PaymentAllocation(invoice,allocation.amount()));}payment.refreshStatus();payments.flush();notifications.enqueue(first.customerEmail(),"PAYMENT","Payment recorded", "A payment of "+r.amount()+" was recorded. Allocation status: "+payment.getStatus()+".");return map(payments.findDetailedById(payment.getId()).orElseThrow());}
 private PaymentResponse map(Payment p){return new PaymentResponse(p.getId(),p.getPaymentDate(),p.getMethod(),p.getAmount(),p.getNotes(),p.getReference(),p.getStatus(),p.getRecordedBy().getEmail(),p.getAllocations().stream().map(a->new PaymentResponse.Allocation(a.getInvoice().getId(),a.getInvoice().getNumber(),a.getAmount())).toList());}
}
