package com.brightloop.discount.entity;
import com.brightloop.common.enums.DiscountType;
import com.brightloop.product.entity.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
@Entity @Table(name="discounts",indexes=@Index(name="idx_discounts_active_dates",columnList="active,start_date,end_date"))
public class Discount{
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="discount_name",nullable=false,length=160) private String name;
 @Enumerated(EnumType.STRING) @Column(name="discount_type",nullable=false,length=16) private DiscountType type;
 @Column(name="discount_value",nullable=false,precision=19,scale=4) private BigDecimal value;
 @Column(name="minimum_purchase",nullable=false,precision=19,scale=4) private BigDecimal minimumPurchase;
 @Column(name="minimum_quantity",nullable=false) private int minimumQuantity;
 @Column(name="start_date",nullable=false) private LocalDate startDate;
 @Column(name="end_date") private LocalDate endDate;
 @Column(name="usage_limit") private Integer usageLimit;
 @Column(name="usage_count",nullable=false) private int usageCount;
 @Column(nullable=false) private boolean active=true;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 @org.hibernate.annotations.BatchSize(size=50) @ManyToMany(fetch=FetchType.LAZY) @JoinTable(name="discount_products",joinColumns=@JoinColumn(name="discount_id"),inverseJoinColumns=@JoinColumn(name="product_id")) private Set<Product> products=new HashSet<>();
 @Version private long version;
 protected Discount(){}
 public Discount(String n,DiscountType t,BigDecimal v,BigDecimal min,int qty,LocalDate start,LocalDate end,Integer limit,Set<Product> products){update(n,t,v,min,qty,start,end,limit,products);}
 public void update(String n,DiscountType t,BigDecimal v,BigDecimal min,int qty,LocalDate start,LocalDate end,Integer limit,Set<Product> products){name=n.trim();type=t;value=v;minimumPurchase=min;minimumQuantity=qty;startDate=start;endDate=end;usageLimit=limit;this.products=new HashSet<>(products);}
 public boolean eligible(BigDecimal purchase,int quantity,Set<UUID> productIds,LocalDate date){return active&&!date.isBefore(startDate)&&(endDate==null||!date.isAfter(endDate))&&(usageLimit==null||usageCount<usageLimit)&&purchase.compareTo(minimumPurchase)>=0&&quantity>=minimumQuantity&&(products.isEmpty()||products.stream().anyMatch(p->productIds.contains(p.getId())));}
 public BigDecimal calculate(BigDecimal base){BigDecimal amount=type==DiscountType.FIXED?value:base.multiply(value).divide(new BigDecimal("100"),4,java.math.RoundingMode.HALF_UP);return amount.min(base).max(BigDecimal.ZERO);}
 public void incrementUsage(){if(usageLimit!=null&&usageCount>=usageLimit)throw new IllegalStateException("Discount usage limit exhausted");usageCount++;}
 public void archive(){active=false;} @PrePersist void create(){createdAt=updatedAt=Instant.now();}@PreUpdate void updateTime(){updatedAt=Instant.now();}
 public UUID getId(){return id;} public String getName(){return name;} public DiscountType getType(){return type;} public BigDecimal getValue(){return value;} public BigDecimal getMinimumPurchase(){return minimumPurchase;} public int getMinimumQuantity(){return minimumQuantity;} public LocalDate getStartDate(){return startDate;} public LocalDate getEndDate(){return endDate;} public Integer getUsageLimit(){return usageLimit;} public int getUsageCount(){return usageCount;} public boolean isActive(){return active;} public Set<Product> getProducts(){return Set.copyOf(products);}
}
