package com.brightloop.subscription.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
@Entity @Table(name="subscription_items",indexes=@Index(name="idx_subscription_items_subscription",columnList="subscription_id"))
public class SubscriptionItem{
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="subscription_id",nullable=false) private Subscription subscription;
 @Column(name="product_id",length=36) private String productId;
 @Column(name="product_name",nullable=false,length=180) private String productName;
 @Column(name="variant_name",length=160) private String variantName;
 @Column(nullable=false) private int quantity;
 @Column(name="unit_price",nullable=false,precision=19,scale=4) private BigDecimal unitPrice;
 @Column(name="line_total",nullable=false,precision=19,scale=4) private BigDecimal lineTotal;
 protected SubscriptionItem(){}
 public SubscriptionItem(String productId,String name,String variant,int quantity,BigDecimal price){this.productId=productId;this.productName=name;this.variantName=variant;this.quantity=quantity;this.unitPrice=price;this.lineTotal=price.multiply(BigDecimal.valueOf(quantity));}
 void setSubscription(Subscription s){subscription=s;}
 public UUID getId(){return id;} public String getProductId(){return productId;} public String getProductName(){return productName;} public String getVariantName(){return variantName;} public int getQuantity(){return quantity;} public BigDecimal getUnitPrice(){return unitPrice;} public BigDecimal getLineTotal(){return lineTotal;}
}
