package com.back.lowcode.service;

import com.back.lowcode.config.LowCodeConstants;
import com.back.lowcode.dto.PageSchemaDTO;
import com.back.lowcode.entity.PageSchema;
import com.back.lowcode.repository.PageSchemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PageSchemaService {

    private final PageSchemaRepository pageSchemaRepository;
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]*$");

    public List<PageSchema> listAll() {
        return pageSchemaRepository.findAllByStatusOrderByUpdatedAtDesc("published");
    }

    public Optional<PageSchema> findById(Long id) {
        return pageSchemaRepository.findById(id);
    }

    public Optional<PageSchema> findByCode(String code) {
        return pageSchemaRepository.findByCode(code);
    }

    public Optional<PageSchema> findPublishedByCode(String code) {
        return pageSchemaRepository.findByCodeAndStatus(code, "published");
    }

    @Transactional
    public PageSchema create(PageSchemaDTO dto) {
        String code = StringUtils.hasText(dto.getCode()) ? dto.getCode().trim().toLowerCase() : slugify(dto.getName());
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException("页面编码格式非法：" + code);
        }
        if (pageSchemaRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("页面编码已存在：" + code);
        }

        PageSchema entity = PageSchema.builder()
                .name(dto.getName())
                .code(code)
                .pageType(StringUtils.hasText(dto.getPageType()) ? dto.getPageType() : "custom")
                .layoutJson(dto.getLayoutJson())
                .status(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : "published")
                .version(dto.getVersion() == null ? 1 : dto.getVersion())
                .build();
        return pageSchemaRepository.save(entity);
    }

    @Transactional
    public PageSchema update(Long id, PageSchemaDTO dto) {
        PageSchema entity = pageSchemaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("页面不存在: " + id));
        if (StringUtils.hasText(dto.getName())) {
            entity.setName(dto.getName());
        }
        if (StringUtils.hasText(dto.getCode())) {
            String code = dto.getCode().trim().toLowerCase();
            if (!CODE_PATTERN.matcher(code).matches()) {
                throw new IllegalArgumentException("页面编码格式非法：" + code);
            }
            if (!code.equals(entity.getCode()) && pageSchemaRepository.findByCode(code).isPresent()) {
                throw new IllegalArgumentException("页面编码已存在：" + code);
            }
            entity.setCode(code);
        }
        if (StringUtils.hasText(dto.getPageType())) {
            entity.setPageType(dto.getPageType());
        }
        if (StringUtils.hasText(dto.getLayoutJson())) {
            entity.setLayoutJson(dto.getLayoutJson());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            entity.setStatus(dto.getStatus());
        }
        entity.setVersion(dto.getVersion() == null ? entity.getVersion() + 1 : dto.getVersion());
        return pageSchemaRepository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        if (!pageSchemaRepository.existsById(id)) {
            throw new IllegalArgumentException("页面不存在: " + id);
        }
        pageSchemaRepository.deleteById(id);
    }

    private String slugify(String input) {
        if (!StringUtils.hasText(input)) {
            throw new IllegalArgumentException("页面名称不能为空");
        }
        String slug = input.trim().toLowerCase()
                .replaceAll("[^a-z0-9_-]+", "-")
                .replaceAll("^-+|-+$", "");
        if (!StringUtils.hasText(slug)) {
            throw new IllegalArgumentException("页面名称格式不合法");
        }
        return slug;
    }
}
