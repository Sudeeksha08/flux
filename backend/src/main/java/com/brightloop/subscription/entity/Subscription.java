package com.brightloop.subscription.entity;

import com.brightloop.common.enums.BillingFrequency;
import com.brightloop.common.enums.SubscriptionStatus;
import com.brightloop.customer.entity.Customer;
import com.brightloop.plan.entity.Plan;
import com.brightloop.quotation.entity.Quotation;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Entity @Table(name="subscriptions",uniqueConstraints=@UniqueConstraint(name="uk_subscriptions_number",columnNames="subscription_number"),indexes={@Index(name="idx_subscriptions_due_status",columnList="status,next_billing_date"),@Index(name="idx_subscriptions_expiry_status",columnList="status,expiry_date")})
public class Subscription {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="subscription_number",nullable=false,length=40) private String number;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id",nullable=false) private Customer customer;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="plan_id",nullable=false) private Plan plan;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="quotation_id") private Quotation quotation;
 @Column(name="start_date",nullable=false) private LocalDate startDate;
 @Column(name="expiry_date") private LocalDate expiryDate;
 @Column(name="next_billing_date") private LocalDate nextBillingDate;
 @Column(name="payment_terms",length=80) private String paymentTerms;
 @Column(name="total_amount",nullable=false,precision=19,scale=4) private BigDecimal totalAmount;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private SubscriptionStatus status=SubscriptionStatus.DRAFT;
 @Column(name="paused_at") private Instant pausedAt;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 @org.hibernate.annotations.BatchSize(size=50) @OneToMany(mappedBy="subscription",cascade=CascadeType.ALL,orphanRemoval=true) private List<SubscriptionItem> items=new ArrayList<>();
 protected Subscription(){}
 public Subscription(String number,Customer customer,Plan plan,Quotation quotation,LocalDate startDate,String paymentTerms,BigDecimal totalAmount){this.number=number;this.customer=customer;this.plan=plan;this.quotation=quotation;this.startDate=startDate;this.paymentTerms=paymentTerms;this.totalAmount=totalAmount;this.nextBillingDate=startDate;this.expiryDate=add(startDate,plan.getBillingFrequency());}
 public void replaceItems(List<SubscriptionItem> lines){items.clear();lines.forEach(this::addItem);}
 public void addItem(SubscriptionItem line){items.add(line);line.setSubscription(this);}
 public void confirm(){require(SubscriptionStatus.DRAFT,SubscriptionStatus.QUOTATION);status=SubscriptionStatus.CONFIRMED;}
 public void activate(){require(SubscriptionStatus.CONFIRMED);status=SubscriptionStatus.ACTIVE;}
 public void pause(){require(SubscriptionStatus.ACTIVE);if(!plan.isPauseAllowed())throw new IllegalStateException("This plan does not allow pausing");status=SubscriptionStatus.PAUSED;pausedAt=Instant.now();}
 public void resume(){require(SubscriptionStatus.PAUSED);long days=Math.max(1,ChronoUnit.DAYS.between(pausedAt,Instant.now()));if(nextBillingDate!=null)nextBillingDate=nextBillingDate.plusDays(days);if(expiryDate!=null)expiryDate=expiryDate.plusDays(days);pausedAt=null;status=SubscriptionStatus.ACTIVE;}
 public void renew(LocalDate today){if(!plan.isRenewalAllowed())throw new IllegalStateException("This plan does not allow renewal");if(status!=SubscriptionStatus.ACTIVE&&status!=SubscriptionStatus.CLOSED)throw new IllegalStateException("Subscription cannot be renewed from its current state");LocalDate base=expiryDate==null||expiryDate.isBefore(today)?today:expiryDate;expiryDate=add(base,plan.getBillingFrequency());if(nextBillingDate==null||nextBillingDate.isBefore(today))nextBillingDate=today;status=SubscriptionStatus.ACTIVE;}
 public void close(LocalDate today){if(status==SubscriptionStatus.CLOSED)throw new IllegalStateException("Subscription is already closed");if(status!=SubscriptionStatus.ACTIVE&&status!=SubscriptionStatus.PAUSED&&status!=SubscriptionStatus.CONFIRMED)throw new IllegalStateException("Subscription cannot be closed from its current state");if(expiryDate!=null&&today.isBefore(expiryDate)&&!plan.isEarlyCloseAllowed())throw new IllegalStateException("Early close is not allowed by this plan");status=SubscriptionStatus.CLOSED;nextBillingDate=null;}
 public boolean due(LocalDate today){return status==SubscriptionStatus.ACTIVE&&nextBillingDate!=null&&!nextBillingDate.isAfter(today);}
 public void completeBillingCycle(){nextBillingDate=add(nextBillingDate,plan.getBillingFrequency());if(plan.isAutoClose()&&expiryDate!=null&&!nextBillingDate.isBefore(expiryDate)){status=SubscriptionStatus.CLOSED;nextBillingDate=null;}}
 private void require(SubscriptionStatus...allowed){if(Arrays.stream(allowed).noneMatch(s->s==status))throw new IllegalStateException("Invalid subscription state transition from "+status);}
 private LocalDate add(LocalDate date,BillingFrequency f){return switch(f){case DAILY->date.plusDays(1);case WEEKLY->date.plusWeeks(1);case MONTHLY->date.plusMonths(1);case YEARLY->date.plusYears(1);};}
 @PrePersist void create(){createdAt=updatedAt=Instant.now();} @PreUpdate void update(){updatedAt=Instant.now();}
 public UUID getId(){return id;} public String getNumber(){return number;} public Customer getCustomer(){return customer;} public Plan getPlan(){return plan;} public Quotation getQuotation(){return quotation;} public LocalDate getStartDate(){return startDate;} public LocalDate getExpiryDate(){return expiryDate;} public LocalDate getNextBillingDate(){return nextBillingDate;} public String getPaymentTerms(){return paymentTerms;} public BigDecimal getTotalAmount(){return totalAmount;} public SubscriptionStatus getStatus(){return status;} public Instant getCreatedAt(){return createdAt;} public List<SubscriptionItem> getItems(){return List.copyOf(items);}
}
