package com.brightloop.product.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name="product_variants",indexes=@Index(name="idx_product_variants_product",columnList="product_id"))
public class ProductVariant {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="product_id",nullable=false) private Product product;
    @Column(name="variant_name",nullable=false,length=160) private String name;
    @Column(name="additional_price",nullable=false,precision=19,scale=4) private BigDecimal additionalPrice;
    protected ProductVariant() { }
    public ProductVariant(String name,BigDecimal additionalPrice){this.name=name.trim();this.additionalPrice=additionalPrice;}
    void setProduct(Product product){this.product=product;}
    public UUID getId(){return id;} public String getName(){return name;} public BigDecimal getAdditionalPrice(){return additionalPrice;}
    public BigDecimal finalPrice(Product product){return product.getSalesPrice().add(additionalPrice);}
}
