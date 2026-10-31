package com.brightloop.quotation.service;
import com.brightloop.common.exception.ResourceNotFoundException;
import com.brightloop.quotation.dto.*;
import com.brightloop.quotation.entity.QuotationTemplate;
import com.brightloop.quotation.repository.QuotationTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
@Service public class QuotationTemplateService{
 private final QuotationTemplateRepository repo;
 public QuotationTemplateService(QuotationTemplateRepository repo){this.repo=repo;}
 @Transactional(readOnly=true) public List<TemplateResponse> list(){return repo.findByActiveTrueOrderByNameAsc().stream().map(this::map).toList();}
 @Transactional public TemplateResponse create(TemplateRequest r){return map(repo.save(new QuotationTemplate(r.name(),r.terms(),r.validityDays())));}
 @Transactional public TemplateResponse update(UUID id,TemplateRequest r){var t=repo.findByIdAndActiveTrue(id).orElseThrow(()->new ResourceNotFoundException("Quotation template not found"));t.update(r.name(),r.terms(),r.validityDays());return map(t);}
 @Transactional public void archive(UUID id){repo.findByIdAndActiveTrue(id).orElseThrow(()->new ResourceNotFoundException("Quotation template not found")).archive();}
 private TemplateResponse map(QuotationTemplate t){return new TemplateResponse(t.getId(),t.getName(),t.getTerms(),t.getValidityDays(),t.isActive(),t.getCreatedAt());}
}
