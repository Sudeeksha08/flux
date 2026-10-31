package com.brightloop.invoice.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
@Entity @Table(name="invoice_items",indexes=@Index(name="idx_invoice_items_invoice",columnList="invoice_id"))
public class InvoiceItem{
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="invoice_id",nullable=false) private Invoice invoice;
 @Column(name="product_id",length=36) private String productId;@Column(name="product_name",nullable=false,length=180) private String productName;@Column(name="variant_name",length=160) private String variantName;
 @Column(nullable=false) private int quantity;@Column(name="unit_price",nullable=false,precision=19,scale=4) private BigDecimal unitPrice;@Column(nullable=false,precision=19,scale=4) private BigDecimal subtotal;
 @Column(name="discount_amount",nullable=false,precision=19,scale=4) private BigDecimal discountAmount;@Column(name="tax_amount",nullable=false,precision=19,scale=4) private BigDecimal taxAmount;
 @Column(name="total_amount",nullable=false,precision=19,scale=4) private BigDecimal totalAmount;@Column(name="tax_details_json",nullable=false,columnDefinition="TEXT") private String taxDetailsJson;
 protected InvoiceItem(){}
 public InvoiceItem(String productId,String name,String variant,int quantity,BigDecimal unit,BigDecimal discount,BigDecimal tax,BigDecimal total,String taxJson){this.productId=productId;productName=name;variantName=variant;this.quantity=quantity;unitPrice=unit;subtotal=unit.multiply(BigDecimal.valueOf(quantity));discountAmount=discount;taxAmount=tax;totalAmount=total;taxDetailsJson=taxJson;}
 void setInvoice(Invoice invoice){this.invoice=invoice;}
 public UUID getId(){return id;} public String getProductId(){return productId;} public String getProductName(){return productName;} public String getVariantName(){return variantName;} public int getQuantity(){return quantity;} public BigDecimal getUnitPrice(){return unitPrice;} public BigDecimal getSubtotal(){return subtotal;} public BigDecimal getDiscountAmount(){return discountAmount;} public BigDecimal getTaxAmount(){return taxAmount;} public BigDecimal getTotalAmount(){return totalAmount;} public String getTaxDetailsJson(){return taxDetailsJson;}
}
