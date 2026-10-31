package com.brightloop.plan.entity;

import com.brightloop.common.enums.BillingFrequency;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="plans",indexes=@Index(name="idx_plans_active_frequency",columnList="active,billing_frequency"))
public class Plan {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(name="plan_name",nullable=false,length=160) private String name;
    @Column(nullable=false,precision=19,scale=4) private BigDecimal price;
    @Enumerated(EnumType.STRING) @Column(name="billing_frequency",nullable=false,length=16) private BillingFrequency billingFrequency;
    @Column(name="minimum_quantity",nullable=false) private int minimumQuantity;
    @Column(name="available_from") private LocalDate availableFrom;
    @Column(name="available_to") private LocalDate availableTo;
    @Column(name="auto_close",nullable=false) private boolean autoClose;
    @Column(name="early_close_allowed",nullable=false) private boolean earlyCloseAllowed;
    @Column(name="pause_allowed",nullable=false) private boolean pauseAllowed;
    @Column(name="renewal_allowed",nullable=false) private boolean renewalAllowed;
    @Column(nullable=false) private boolean active=true;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @org.hibernate.annotations.BatchSize(size=50) @OneToMany(mappedBy="plan",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.LAZY) private List<PlanProduct> products=new ArrayList<>();
    protected Plan() { }
    public Plan(String name,BigDecimal price,BillingFrequency frequency,int min,LocalDate from,LocalDate to,boolean auto,boolean early,boolean pause,boolean renewal){update(name,price,frequency,min,from,to,auto,early,pause,renewal);}
    public void update(String name,BigDecimal price,BillingFrequency frequency,int min,LocalDate from,LocalDate to,boolean auto,boolean early,boolean pause,boolean renewal){this.name=name.trim();this.price=price;this.billingFrequency=frequency;this.minimumQuantity=min;this.availableFrom=from;this.availableTo=to;this.autoClose=auto;this.earlyCloseAllowed=early;this.pauseAllowed=pause;this.renewalAllowed=renewal;}
    public void replaceProducts(List<PlanProduct> values){products.clear();values.forEach(this::addProduct);}
    public void addProduct(PlanProduct value){products.add(value);value.setPlan(this);}
    public void validateAvailable(LocalDate date){if(!active||(availableFrom!=null&&date.isBefore(availableFrom))||(availableTo!=null&&date.isAfter(availableTo)))throw new IllegalStateException("Plan is not available on the requested date");}
    @PrePersist void create(){createdAt=updatedAt=Instant.now();} @PreUpdate void updateTimestamp(){updatedAt=Instant.now();}
    public UUID getId(){return id;} public String getName(){return name;} public BigDecimal getPrice(){return price;} public BillingFrequency getBillingFrequency(){return billingFrequency;} public int getMinimumQuantity(){return minimumQuantity;} public LocalDate getAvailableFrom(){return availableFrom;} public LocalDate getAvailableTo(){return availableTo;} public boolean isAutoClose(){return autoClose;} public boolean isEarlyCloseAllowed(){return earlyCloseAllowed;} public boolean isPauseAllowed(){return pauseAllowed;} public boolean isRenewalAllowed(){return renewalAllowed;} public boolean isActive(){return active;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;} public List<PlanProduct> getProducts(){return List.copyOf(products);} public void archive(){active=false;}
}
