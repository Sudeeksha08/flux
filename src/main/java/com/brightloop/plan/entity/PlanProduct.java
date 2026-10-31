package com.brightloop.plan.entity;

import com.brightloop.product.entity.Product;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="plan_products",uniqueConstraints=@UniqueConstraint(name="uk_plan_products_pair",columnNames={"plan_id","product_id"}))
public class PlanProduct {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="plan_id",nullable=false) private Plan plan;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="product_id",nullable=false) private Product product;
    @Column(nullable=false) private int quantity;
    protected PlanProduct() { }
    public PlanProduct(Product product,int quantity){this.product=product;this.quantity=quantity;}
    void setPlan(Plan plan){this.plan=plan;}
    public UUID getId(){return id;} public Product getProduct(){return product;} public int getQuantity(){return quantity;}
}
