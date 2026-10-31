package com.brightloop.quotation.dto;
import jakarta.validation.constraints.*;
public record TemplateRequest(@NotBlank @Size(max=160) String name,@Size(max=5000) String terms,@Min(1) @Max(365) int validityDays){}
