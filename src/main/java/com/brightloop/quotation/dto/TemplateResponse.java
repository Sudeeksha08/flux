package com.brightloop.quotation.dto;
import java.time.Instant;
import java.util.UUID;
public record TemplateResponse(UUID id,String name,String terms,int validityDays,boolean active,Instant createdAt){}
