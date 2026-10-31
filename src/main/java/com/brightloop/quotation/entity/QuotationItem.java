package com.brightloop.quotation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name="quotation_items",indexes=@Index(name="idx_quotation_items_quotation",columnList="quotation_id"))
public class QuotationItem {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="quotation_id",nullable=false) private Quotation quotation;
    @Column(name="product_id",length=36) private String productId;
    @Column(name="product_name",nullable=false,length=180) private String productName;
    @Column(name="variant_name",length=160) private String variantName;
    @Column(nullable=false) private int quantity;
    @Column(name="unit_price",nullable=false,precision=19,scale=4) private BigDecimal unitPrice;
    @Column(name="line_total",nullable=false,precision=19,scale=4) private BigDecimal lineTotal;
    protected QuotationItem(){}
    public QuotationItem(String productId,String name,String variant,int quantity,BigDecimal price){this.productId=productId;this.productName=name;this.variantName=variant;this.quantity=quantity;this.unitPrice=price;this.lineTotal=price.multiply(BigDecimal.valueOf(quantity));}
    void setQuotation(Quotation quotation){this.quotation=quotation;}
    public BigDecimal getLineTotal(){return lineTotal;} public UUID getId(){return id;} public String getProductName(){return productName;} public String getVariantName(){return variantName;} public int getQuantity(){return quantity;} public BigDecimal getUnitPrice(){return unitPrice;} public String getProductId(){return productId;}
}
