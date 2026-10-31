package com.brightloop.discount.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="discount_usages",uniqueConstraints=@UniqueConstraint(name="uk_discount_usage_reference",columnNames={"discount_id","billing_reference"}))
public class DiscountUsage{
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="discount_id",nullable=false) private Discount discount;
 @Column(name="billing_reference",nullable=false,length=80) private String billingReference;
 @Column(name="invoice_id") private UUID invoiceId;
 @Column(nullable=false,precision=19,scale=4) private BigDecimal amount;
 @Column(name="used_at",nullable=false) private Instant usedAt;
 protected DiscountUsage(){}
 public DiscountUsage(Discount d,String reference,BigDecimal amount){discount=d;billingReference=reference;this.amount=amount;usedAt=Instant.now();}
 public Discount getDiscount(){return discount;} public BigDecimal getAmount(){return amount;}
 public void linkInvoice(UUID invoiceId){this.invoiceId=invoiceId;}
}
