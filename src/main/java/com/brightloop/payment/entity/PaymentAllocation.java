package com.brightloop.payment.entity;
import com.brightloop.invoice.entity.Invoice;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="payment_allocations",uniqueConstraints=@UniqueConstraint(name="uk_payment_invoice",columnNames={"payment_id","invoice_id"}),indexes=@Index(name="idx_allocations_invoice",columnList="invoice_id"))
public class PaymentAllocation{
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="payment_id",nullable=false) private Payment payment;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="invoice_id",nullable=false) private Invoice invoice;
 @Column(nullable=false,precision=19,scale=4) private BigDecimal amount;
 @Column(name="allocated_at",nullable=false) private Instant allocatedAt;
 protected PaymentAllocation(){}
 public PaymentAllocation(Invoice invoice,BigDecimal amount){this.invoice=invoice;this.amount=amount;allocatedAt=Instant.now();}
 void setPayment(Payment payment){this.payment=payment;}
 public UUID getId(){return id;} public Invoice getInvoice(){return invoice;} public BigDecimal getAmount(){return amount;} public Instant getAllocatedAt(){return allocatedAt;}
}
