package com.back.lowcode.service;

import com.back.lowcode.entity.BusinessApp;
import com.back.lowcode.entity.PageSchema;
import com.back.lowcode.repository.BusinessAppRepository;
import com.back.lowcode.repository.PageSchemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessAppService {

    private final BusinessAppRepository businessAppRepository;
    private final PageSchemaRepository pageSchemaRepository;

    public List<BusinessApp> getAllApps() {
        return businessAppRepository.findAll();
    }

    public BusinessApp getByCode(String code) {
        return businessAppRepository.findByCode(code).orElse(null);
    }

    public BusinessApp getById(Long id) {
        return businessAppRepository.findById(id).orElse(null);
    }

    @Transactional
    public BusinessApp create(BusinessApp app) {
        if (businessAppRepository.existsByCode(app.getCode())) {
            throw new RuntimeException("应用编码已存在: " + app.getCode());
        }
        return businessAppRepository.save(app);
    }

    @Transactional
    public BusinessApp update(Long id, BusinessApp app) {
        BusinessApp existing = businessAppRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new RuntimeException("应用不存在: " + id);
        }
        existing.setName(app.getName());
        existing.setDescription(app.getDescription());
        existing.setIcon(app.getIcon());
        existing.setColor(app.getColor());
        return businessAppRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        // 删除应用时，解除关联页面的 appCode
        BusinessApp app = businessAppRepository.findById(id).orElse(null);
        if (app != null) {
            List<PageSchema> pages = pageSchemaRepository.findByAppCode(app.getCode());
            for (PageSchema page : pages) {
                page.setAppCode(null);
                pageSchemaRepository.save(page);
            }
        }
        businessAppRepository.deleteById(id);
    }

    /**
     * 获取某应用下的所有已发布页面
     */
    public List<PageSchema> getAppPages(String appCode) {
        return pageSchemaRepository.findByAppCode(appCode);
    }

    /**
     * 将页面分配到某个应用
     */
    @Transactional
    public void assignPageToApp(Long pageId, String appCode) {
        PageSchema page = pageSchemaRepository.findById(pageId).orElse(null);
        if (page == null) {
            throw new RuntimeException("页面不存在: " + pageId);
        }
        if (appCode != null && !businessAppRepository.existsByCode(appCode)) {
            throw new RuntimeException("应用不存在: " + appCode);
        }
        page.setAppCode(appCode);
        pageSchemaRepository.save(page);
    }

    /**
     * 批量将多个页面分配到某个应用（聚合为系统）
     */
    @Transactional
    public void assignPagesToApp(List<Long> pageIds, String appCode) {
        if (appCode == null || !businessAppRepository.existsByCode(appCode)) {
            throw new RuntimeException("应用不存在: " + appCode);
        }
        List<PageSchema> pages = pageSchemaRepository.findAllById(pageIds);
        if (pages.isEmpty()) {
            throw new RuntimeException("未找到任何页面");
        }
        for (PageSchema page : pages) {
            page.setAppCode(appCode);
            // 如果页面还是草稿状态，自动发布
            if (!"published".equals(page.getStatus())) {
                page.setStatus("published");
                page.setPublishedAt(java.time.LocalDateTime.now());
            }
        }
        pageSchemaRepository.saveAll(pages);
    }
}
