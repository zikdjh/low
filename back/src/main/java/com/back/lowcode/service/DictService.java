package com.back.lowcode.service;

import com.back.lowcode.entity.DictItem;
import com.back.lowcode.entity.DictType;
import com.back.lowcode.repository.DictItemRepository;
import com.back.lowcode.repository.DictTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictService {

    private final DictTypeRepository dictTypeRepository;
    private final DictItemRepository dictItemRepository;

    public List<DictType> getAllDictTypes() {
        return dictTypeRepository.findByStatus("active");
    }

    public DictType getDictTypeByCode(String dictCode) {
        return dictTypeRepository.findByDictCode(dictCode).orElse(null);
    }

    public List<DictItem> getDictItems(String dictCode) {
        return dictItemRepository.findByDictCodeAndStatusOrderBySortOrder(dictCode, "active");
    }

    public Map<String, String> getDictMap(String dictCode) {
        return dictItemRepository.findByDictCodeAndStatusOrderBySortOrder(dictCode, "active")
                .stream()
                .collect(Collectors.toMap(DictItem::getItemKey, DictItem::getItemValue));
    }

    @Transactional
    public DictType createDictType(DictType dictType) {
        if (dictTypeRepository.existsByDictCode(dictType.getDictCode())) {
            throw new RuntimeException("字典类型已存在: " + dictType.getDictCode());
        }
        return dictTypeRepository.save(dictType);
    }

    @Transactional
    public DictType updateDictType(Long id, DictType dictType) {
        DictType existing = dictTypeRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new RuntimeException("字典类型不存在: " + id);
        }
        existing.setName(dictType.getName());
        existing.setDescription(dictType.getDescription());
        existing.setStatus(dictType.getStatus());
        return dictTypeRepository.save(existing);
    }

    @Transactional
    public void deleteDictType(Long id) {
        DictType dictType = dictTypeRepository.findById(id).orElse(null);
        if (dictType != null && dictType.getIsSystem()) {
            throw new RuntimeException("系统字典不能删除");
        }
        dictItemRepository.deleteByDictCode(dictType.getDictCode());
        dictTypeRepository.deleteById(id);
    }

    @Transactional
    public DictItem createDictItem(DictItem dictItem) {
        if (dictItemRepository.existsByDictCodeAndItemKey(dictItem.getDictCode(), dictItem.getItemKey())) {
            throw new RuntimeException("字典项已存在: " + dictItem.getDictCode() + "." + dictItem.getItemKey());
        }
        return dictItemRepository.save(dictItem);
    }

    @Transactional
    public DictItem updateDictItem(Long id, DictItem dictItem) {
        DictItem existing = dictItemRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new RuntimeException("字典项不存在: " + id);
        }
        existing.setItemKey(dictItem.getItemKey());
        existing.setItemValue(dictItem.getItemValue());
        existing.setSortOrder(dictItem.getSortOrder());
        existing.setColor(dictItem.getColor());
        existing.setStatus(dictItem.getStatus());
        return dictItemRepository.save(existing);
    }

    @Transactional
    public void deleteDictItem(Long id) {
        dictItemRepository.deleteById(id);
    }

    @Transactional
    public void batchSaveItems(String dictCode, List<DictItem> items) {
        dictItemRepository.deleteByDictCode(dictCode);
        for (DictItem item : items) {
            item.setDictCode(dictCode);
            dictItemRepository.save(item);
        }
    }
}