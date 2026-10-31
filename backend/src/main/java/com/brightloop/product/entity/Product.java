package com.brightloop.product.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="products", indexes=@Index(name="idx_products_active_name", columnList="active,name"))
public class Product {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false,length=180) private String name;
    @Column(name="product_type",nullable=false,length=40) private String productType;
    @Column(name="sales_price",nullable=false,precision=19,scale=4) private BigDecimal salesPrice;
    @Column(name="cost_price",nullable=false,precision=19,scale=4) private BigDecimal costPrice;
    @Column(length=2000) private String notes;
    @Column(nullable=false) private boolean active=true;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @org.hibernate.annotations.BatchSize(size=50)
    @OneToMany(mappedBy="product",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.LAZY)
    private List<ProductVariant> variants=new ArrayList<>();
    protected Product() { }
    public Product(String name,String type,BigDecimal sales,BigDecimal cost,String notes) { update(name,type,sales,cost,notes); }
    public void update(String name,String type,BigDecimal sales,BigDecimal cost,String notes) { this.name=name.trim();this.productType=type.trim();this.salesPrice=sales;this.costPrice=cost;this.notes=notes; }
    public void replaceVariants(List<ProductVariant> values) { variants.clear(); values.forEach(this::addVariant); }
    public void addVariant(ProductVariant value) { variants.add(value);value.setProduct(this); }
    @PrePersist void create(){createdAt=updatedAt=Instant.now();}
    @PreUpdate void updateTimestamp(){updatedAt=Instant.now();}
    public UUID getId(){return id;} public String getName(){return name;} public String getProductType(){return productType;}
    public BigDecimal getSalesPrice(){return salesPrice;} public BigDecimal getCostPrice(){return costPrice;} public String getNotes(){return notes;}
    public boolean isActive(){return active;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
    public List<ProductVariant> getVariants(){return List.copyOf(variants);} public void archive(){active=false;}
}
