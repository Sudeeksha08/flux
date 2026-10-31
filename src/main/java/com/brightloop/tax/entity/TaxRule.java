package com.brightloop.tax.entity;
import com.brightloop.common.enums.TaxType;
import com.brightloop.product.entity.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
@Entity @Table(name="tax_rules",indexes=@Index(name="idx_tax_rules_active_dates",columnList="active,effective_from,effective_to"))
public class TaxRule{
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="tax_name",nullable=false,length=160) private String name;
 @Column(name="tax_percentage",nullable=false,precision=9,scale=4) private BigDecimal percentage;
 @Enumerated(EnumType.STRING) @Column(name="tax_type",nullable=false,length=16) private TaxType type;
 @Column(name="effective_from",nullable=false) private LocalDate effectiveFrom;
 @Column(name="effective_to") private LocalDate effectiveTo;
 @Column(nullable=false) private boolean active=true;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 @org.hibernate.annotations.BatchSize(size=50) @ManyToMany(fetch=FetchType.LAZY) @JoinTable(name="tax_rule_products",joinColumns=@JoinColumn(name="tax_rule_id"),inverseJoinColumns=@JoinColumn(name="product_id")) private Set<Product> products=new HashSet<>();
 protected TaxRule(){}
 public TaxRule(String n,BigDecimal p,TaxType t,LocalDate f,LocalDate to,Set<Product> products){update(n,p,t,f,to,products);}
 public void update(String n,BigDecimal p,TaxType t,LocalDate f,LocalDate to,Set<Product> products){name=n.trim();percentage=p;type=t;effectiveFrom=f;effectiveTo=to;this.products=new HashSet<>(products);}
 public boolean appliesTo(UUID productId,LocalDate date){return active&&!date.isBefore(effectiveFrom)&&(effectiveTo==null||!date.isAfter(effectiveTo))&&(products.isEmpty()||products.stream().anyMatch(p->p.getId().equals(productId)));}
 public void archive(){active=false;} @PrePersist void create(){createdAt=updatedAt=Instant.now();}@PreUpdate void updateTime(){updatedAt=Instant.now();}
 public UUID getId(){return id;} public String getName(){return name;} public BigDecimal getPercentage(){return percentage;} public TaxType getType(){return type;} public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getEffectiveTo(){return effectiveTo;} public boolean isActive(){return active;} public Set<Product> getProducts(){return Set.copyOf(products);}
}
