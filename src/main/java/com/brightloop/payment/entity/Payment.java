package com.brightloop.payment.entity;
import com.brightloop.auth.entity.User;
import com.brightloop.common.enums.PaymentMethod;
import com.brightloop.common.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
@Entity @Table(name="payments",indexes={@Index(name="idx_payments_date_status",columnList="payment_date,status"),@Index(name="idx_payments_reference",columnList="payment_reference")})
public class Payment{
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="recorded_by_user_id",nullable=false) private User recordedBy;
 @Column(name="payment_date",nullable=false) private Instant paymentDate;
 @Enumerated(EnumType.STRING) @Column(name="payment_method",nullable=false,length=24) private PaymentMethod method;
 @Column(nullable=false,precision=19,scale=4) private BigDecimal amount;
 @Column(length=1000) private String notes;
 @Column(name="payment_reference",length=120) private String reference;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private PaymentStatus status=PaymentStatus.RECORDED;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @org.hibernate.annotations.BatchSize(size=50) @OneToMany(mappedBy="payment",cascade=CascadeType.ALL,orphanRemoval=true) private List<PaymentAllocation> allocations=new ArrayList<>();
 protected Payment(){}
 public Payment(User recorder,PaymentMethod method,BigDecimal amount,String notes,String reference){recordedBy=recorder;this.method=method;this.amount=amount;this.notes=notes;this.reference=reference;paymentDate=Instant.now();}
 public void addAllocation(PaymentAllocation allocation){allocations.add(allocation);allocation.setPayment(this);refreshStatus();}
 public void refreshStatus(){BigDecimal allocated=allocations.stream().map(PaymentAllocation::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);status=allocated.signum()==0?PaymentStatus.RECORDED:allocated.compareTo(amount)>=0?PaymentStatus.ALLOCATED:PaymentStatus.PARTIALLY_ALLOCATED;}
 @PrePersist void create(){createdAt=Instant.now();}
 public UUID getId(){return id;} public User getRecordedBy(){return recordedBy;} public Instant getPaymentDate(){return paymentDate;} public PaymentMethod getMethod(){return method;} public BigDecimal getAmount(){return amount;} public String getNotes(){return notes;} public String getReference(){return reference;} public PaymentStatus getStatus(){return status;} public List<PaymentAllocation> getAllocations(){return List.copyOf(allocations);}
}
