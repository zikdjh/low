package com.back.lowcode.service;

import com.back.lowcode.entity.PageSchema;
import com.back.lowcode.repository.PageSchemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PageSchemaService {

    private final PageSchemaRepository pageSchemaRepository;

    public List<PageSchema> getAllPages() {
        return pageSchemaRepository.findAll();
    }

    public List<PageSchema> getPagesByStatus(String status) {
        return pageSchemaRepository.findByStatus(status);
    }

    public PageSchema getByPageCode(String pageCode) {
        return pageSchemaRepository.findByPageCode(pageCode).orElse(null);
    }

    public PageSchema getById(Long id) {
        return pageSchemaRepository.findById(id).orElse(null);
    }

    @Transactional
    public PageSchema create(PageSchema pageSchema) {
        if (pageSchemaRepository.existsByPageCode(pageSchema.getPageCode())) {
            throw new RuntimeException("页面标识已存在: " + pageSchema.getPageCode());
        }
        return pageSchemaRepository.save(pageSchema);
    }

    @Transactional
    public PageSchema update(Long id, PageSchema pageSchema) {
        PageSchema existing = pageSchemaRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new RuntimeException("页面不存在: " + id);
        }
        existing.setName(pageSchema.getName());
        existing.setLayoutJson(pageSchema.getLayoutJson());
        existing.setPageType(pageSchema.getPageType());
        existing.setEntityCode(pageSchema.getEntityCode());
        existing.setDescription(pageSchema.getDescription());
        return pageSchemaRepository.save(existing);
    }

    @Transactional
    public PageSchema publish(Long id) {
        PageSchema pageSchema = pageSchemaRepository.findById(id).orElse(null);
        if (pageSchema == null) {
            throw new RuntimeException("页面不存在: " + id);
        }
        pageSchema.setStatus("published");
        pageSchema.setPublishedAt(LocalDateTime.now());
        pageSchema.setVersion(pageSchema.getVersion() + 1);
        return pageSchemaRepository.save(pageSchema);
    }

    @Transactional
    public PageSchema unpublish(Long id) {
        PageSchema pageSchema = pageSchemaRepository.findById(id).orElse(null);
        if (pageSchema == null) {
            throw new RuntimeException("页面不存在: " + id);
        }
        pageSchema.setStatus("draft");
        pageSchema.setPublishedAt(null);
        return pageSchemaRepository.save(pageSchema);
    }

    @Transactional
    public void delete(Long id) {
        pageSchemaRepository.deleteById(id);
    }
}