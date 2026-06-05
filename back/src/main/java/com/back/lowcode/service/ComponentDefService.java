package com.back.lowcode.service;

import com.back.lowcode.entity.ComponentDef;
import com.back.lowcode.repository.ComponentDefRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ComponentDefService {

    @Autowired
    private ComponentDefRepository componentDefRepository;

    public List<ComponentDef> getAllActiveComponents() {
        return componentDefRepository.findByStatus("active");
    }

    public List<ComponentDef> getComponentsByCategory(String category) {
        return componentDefRepository.findByCategoryAndStatus(category, "active");
    }

    public Map<String, List<ComponentDef>> getComponentsGroupedByCategory() {
        return componentDefRepository.findByStatus("active")
                .stream()
                .collect(Collectors.groupingBy(ComponentDef::getCategory));
    }

    public Optional<ComponentDef> getByCompKey(String compKey) {
        return componentDefRepository.findByCompKey(compKey);
    }

    public ComponentDef getById(Long id) {
        return componentDefRepository.findById(id).orElse(null);
    }

    @Transactional
    public ComponentDef create(ComponentDef componentDef) {
        if (componentDefRepository.existsByCompKey(componentDef.getCompKey())) {
            throw new RuntimeException("组件标识已存在: " + componentDef.getCompKey());
        }
        return componentDefRepository.save(componentDef);
    }

    @Transactional
    public ComponentDef update(Long id, ComponentDef componentDef) {
        ComponentDef existing = componentDefRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new RuntimeException("组件不存在: " + id);
        }
        existing.setName(componentDef.getName());
        existing.setCategory(componentDef.getCategory());
        existing.setIcon(componentDef.getIcon());
        existing.setDescription(componentDef.getDescription());
        existing.setDefaultPropsJson(componentDef.getDefaultPropsJson());
        existing.setPropsSchemaJson(componentDef.getPropsSchemaJson());
        existing.setGroupIndex(componentDef.getGroupIndex());
        existing.setStatus(componentDef.getStatus());
        return componentDefRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        ComponentDef component = componentDefRepository.findById(id).orElse(null);
        if (component != null && component.getIsSystem()) {
            throw new RuntimeException("系统组件不能删除");
        }
        componentDefRepository.deleteById(id);
    }

    @Transactional
    public void batchSave(List<ComponentDef> components) {
        for (ComponentDef component : components) {
            Optional<ComponentDef> existing = componentDefRepository.findByCompKey(component.getCompKey());
            if (existing.isPresent()) {
                ComponentDef update = existing.get();
                update.setName(component.getName());
                update.setCategory(component.getCategory());
                update.setIcon(component.getIcon());
                update.setDescription(component.getDescription());
                update.setDefaultPropsJson(component.getDefaultPropsJson());
                update.setPropsSchemaJson(component.getPropsSchemaJson());
                update.setGroupIndex(component.getGroupIndex());
                componentDefRepository.save(update);
            } else {
                componentDefRepository.save(component);
            }
        }
    }
}