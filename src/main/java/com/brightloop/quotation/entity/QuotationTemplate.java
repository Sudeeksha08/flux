package com.brightloop.quotation.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="quotation_templates")
public class QuotationTemplate {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false,length=160) private String name;
    @Column(length=5000) private String terms;
    @Column(name="validity_days",nullable=false) private int validityDays;
    @Column(nullable=false) private boolean active=true;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    protected QuotationTemplate(){}
    public QuotationTemplate(String name,String terms,int validityDays){this.name=name.trim();this.terms=terms;this.validityDays=validityDays;}
    @PrePersist void create(){createdAt=updatedAt=Instant.now();} @PreUpdate void update(){updatedAt=Instant.now();}
    public UUID getId(){return id;} public String getName(){return name;} public String getTerms(){return terms;} public int getValidityDays(){return validityDays;} public boolean isActive(){return active;} public Instant getCreatedAt(){return createdAt;} public void update(String n,String t,int d){name=n.trim();terms=t;validityDays=d;} public void archive(){active=false;}
}
