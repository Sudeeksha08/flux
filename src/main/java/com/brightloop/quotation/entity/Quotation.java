package com.brightloop.quotation.entity;

import com.brightloop.common.enums.QuotationStatus;
import com.brightloop.customer.entity.Customer;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name="quotations",uniqueConstraints=@UniqueConstraint(name="uk_quotations_number",columnNames="quotation_number"),indexes=@Index(name="idx_quotations_customer_status",columnList="customer_id,status"))
public class Quotation {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(name="quotation_number",nullable=false,length=40) private String number;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id",nullable=false) private Customer customer;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="template_id") private QuotationTemplate template;
    @Column(name="validity_date",nullable=false) private LocalDate validityDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private QuotationStatus status=QuotationStatus.DRAFT;
    @Column(nullable=false,precision=19,scale=4) private BigDecimal subtotal=BigDecimal.ZERO;
    @Column(name="discount_amount",nullable=false,precision=19,scale=4) private BigDecimal discountAmount=BigDecimal.ZERO;
    @Column(name="tax_amount",nullable=false,precision=19,scale=4) private BigDecimal taxAmount=BigDecimal.ZERO;
    @Column(name="total_amount",nullable=false,precision=19,scale=4) private BigDecimal totalAmount=BigDecimal.ZERO;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @org.hibernate.annotations.BatchSize(size=50) @OneToMany(mappedBy="quotation",cascade=CascadeType.ALL,orphanRemoval=true) private List<QuotationItem> items=new ArrayList<>();
    protected Quotation(){}
    public Quotation(String number,Customer customer,QuotationTemplate template,LocalDate validityDate){this.number=number;this.customer=customer;this.template=template;this.validityDate=validityDate;}
    public void replaceItems(List<QuotationItem> lines){items.clear();lines.forEach(this::addItem);recalculate();}
    public void revise(Customer customer,QuotationTemplate template,LocalDate validityDate,List<QuotationItem> lines){this.customer=customer;this.template=template;this.validityDate=validityDate;replaceItems(lines);}
    public void addItem(QuotationItem item){items.add(item);item.setQuotation(this);}
    public void recalculate(){subtotal=items.stream().map(QuotationItem::getLineTotal).reduce(BigDecimal.ZERO,BigDecimal::add);totalAmount=subtotal.subtract(discountAmount).add(taxAmount);}
    public void confirm(){if(!validityDate.isAfter(LocalDate.now()))throw new IllegalStateException("Quotation has expired");if(status!=QuotationStatus.DRAFT&&status!=QuotationStatus.QUOTATION)throw new IllegalStateException("Quotation cannot be confirmed from its current state");status=QuotationStatus.CONFIRMED;}
    public void send(){if(!validityDate.isAfter(LocalDate.now()))throw new IllegalStateException("Quotation has expired");if(status!=QuotationStatus.DRAFT)throw new IllegalStateException("Only a draft quotation can be sent");status=QuotationStatus.QUOTATION;}
    public void expire(){if(status==QuotationStatus.DRAFT||status==QuotationStatus.QUOTATION)status=QuotationStatus.EXPIRED;}
    @PrePersist void create(){createdAt=updatedAt=Instant.now();} @PreUpdate void update(){updatedAt=Instant.now();}
    public UUID getId(){return id;} public String getNumber(){return number;} public Customer getCustomer(){return customer;} public QuotationTemplate getTemplate(){return template;} public LocalDate getValidityDate(){return validityDate;} public QuotationStatus getStatus(){return status;} public BigDecimal getSubtotal(){return subtotal;} public BigDecimal getDiscountAmount(){return discountAmount;} public BigDecimal getTaxAmount(){return taxAmount;} public BigDecimal getTotalAmount(){return totalAmount;} public Instant getCreatedAt(){return createdAt;} public List<QuotationItem> getItems(){return List.copyOf(items);}
}
