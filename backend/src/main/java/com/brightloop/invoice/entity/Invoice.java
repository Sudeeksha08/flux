package com.brightloop.invoice.entity;
import com.brightloop.common.enums.InvoiceStatus;
import com.brightloop.customer.entity.Customer;
import com.brightloop.subscription.entity.Subscription;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
@Entity @Table(name="invoices",uniqueConstraints={@UniqueConstraint(name="uk_invoices_number",columnNames="invoice_number"),@UniqueConstraint(name="uk_invoice_subscription_period",columnNames={"subscription_id","billing_period_start"})},indexes={@Index(name="idx_invoices_due_status",columnList="due_date,status"),@Index(name="idx_invoices_status_date",columnList="status,invoice_date")})
public class Invoice{
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="invoice_number",nullable=false,length=40) private String number;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id",nullable=false) private Customer customer;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="subscription_id",nullable=false) private Subscription subscription;
 @Column(name="billing_period_start",nullable=false) private LocalDate periodStart;@Column(name="billing_period_end",nullable=false) private LocalDate periodEnd;
 @Column(name="invoice_date",nullable=false) private LocalDate invoiceDate;@Column(name="due_date",nullable=false) private LocalDate dueDate;
 @Column(name="customer_name",nullable=false,length=160) private String customerName;@Column(name="customer_email",nullable=false,length=254) private String customerEmail;
 @Column(name="company_name",length=180) private String companyName;@Column(name="customer_phone",length=40) private String customerPhone;@Column(name="customer_address",length=500) private String customerAddress;
 @Column(nullable=false,precision=19,scale=4) private BigDecimal subtotal;@Column(name="discount_amount",nullable=false,precision=19,scale=4) private BigDecimal discountAmount;
 @Column(name="tax_amount",nullable=false,precision=19,scale=4) private BigDecimal taxAmount;@Column(name="total_amount",nullable=false,precision=19,scale=4) private BigDecimal totalAmount;
 @Column(name="paid_amount",nullable=false,precision=19,scale=4) private BigDecimal paidAmount=BigDecimal.ZERO;@Column(name="outstanding_amount",nullable=false,precision=19,scale=4) private BigDecimal outstandingAmount;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private InvoiceStatus status=InvoiceStatus.DRAFT;
 @Column(name="discount_name",length=160) private String discountName;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;@Column(name="updated_at",nullable=false) private Instant updatedAt;
 @org.hibernate.annotations.BatchSize(size=50) @OneToMany(mappedBy="invoice",cascade=CascadeType.ALL,orphanRemoval=true) private List<InvoiceItem> items=new ArrayList<>();
 protected Invoice(){}
 public Invoice(String number,Subscription subscription,LocalDate start,LocalDate end,LocalDate date,LocalDate due,BigDecimal subtotal,BigDecimal discount,BigDecimal tax,BigDecimal total,String discountName){this.number=number;this.subscription=subscription;this.customer=subscription.getCustomer();this.periodStart=start;this.periodEnd=end;this.invoiceDate=date;this.dueDate=due;this.customerName=customer.getName();this.customerEmail=customer.getEmail();this.companyName=customer.getCompanyName();this.customerPhone=customer.getPhone();this.customerAddress=customer.getAddress();this.subtotal=subtotal;this.discountAmount=discount;this.taxAmount=tax;this.totalAmount=total;this.outstandingAmount=total;this.discountName=discountName;}
 public void addItem(InvoiceItem item){items.add(item);item.setInvoice(this);}
 public void confirm(){if(status!=InvoiceStatus.DRAFT)throw new IllegalStateException("Only draft invoices can be confirmed");status=InvoiceStatus.CONFIRMED;}
 public void cancel(){if(paidAmount.signum()>0)throw new IllegalStateException("An invoice with payments cannot be cancelled");if(status==InvoiceStatus.CANCELLED)throw new IllegalStateException("Invoice is already cancelled");status=InvoiceStatus.CANCELLED;outstandingAmount=BigDecimal.ZERO;}
 public void applyPayment(BigDecimal amount){if(status==InvoiceStatus.CANCELLED||status==InvoiceStatus.DRAFT)throw new IllegalStateException("Only issued invoices can receive payments");if(amount.signum()<=0||amount.compareTo(outstandingAmount)>0)throw new IllegalStateException("Payment allocation exceeds the invoice outstanding amount");paidAmount=paidAmount.add(amount);outstandingAmount=totalAmount.subtract(paidAmount);status=outstandingAmount.signum()==0?InvoiceStatus.PAID:InvoiceStatus.PARTIALLY_PAID;}
 public void markOverdue(LocalDate today){if(outstandingAmount.signum()>0&&dueDate.isBefore(today)&&(status==InvoiceStatus.CONFIRMED||status==InvoiceStatus.PARTIALLY_PAID))status=InvoiceStatus.OVERDUE;}
 @PrePersist void create(){createdAt=updatedAt=Instant.now();}@PreUpdate void update(){updatedAt=Instant.now();}
 public UUID getId(){return id;} public String getNumber(){return number;} public Customer getCustomer(){return customer;} public Subscription getSubscription(){return subscription;} public LocalDate getPeriodStart(){return periodStart;} public LocalDate getPeriodEnd(){return periodEnd;} public LocalDate getInvoiceDate(){return invoiceDate;} public LocalDate getDueDate(){return dueDate;} public String getCustomerName(){return customerName;} public String getCustomerEmail(){return customerEmail;} public String getCompanyName(){return companyName;} public String getCustomerPhone(){return customerPhone;} public String getCustomerAddress(){return customerAddress;} public BigDecimal getSubtotal(){return subtotal;} public BigDecimal getDiscountAmount(){return discountAmount;} public BigDecimal getTaxAmount(){return taxAmount;} public BigDecimal getTotalAmount(){return totalAmount;} public BigDecimal getPaidAmount(){return paidAmount;} public BigDecimal getOutstandingAmount(){return outstandingAmount;} public InvoiceStatus getStatus(){return status;} public String getDiscountName(){return discountName;} public List<InvoiceItem> getItems(){return List.copyOf(items);}
}
