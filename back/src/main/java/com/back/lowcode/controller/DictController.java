package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.entity.DictItem;
import com.back.lowcode.entity.DictType;
import com.back.lowcode.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lowcode/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @GetMapping("/types")
    public Result getAllDictTypes() {
        return Result.success(dictService.getAllDictTypes());
    }

    @GetMapping("/types/{dictCode}")
    public Result getDictType(@PathVariable String dictCode) {
        DictType dictType = dictService.getDictTypeByCode(dictCode);
        if (dictType == null) {
            return Result.error("字典类型不存在");
        }
        return Result.success(dictType);
    }

    @PostMapping("/types")
    public Result createDictType(@RequestBody DictType dictType) {
        return Result.success(dictService.createDictType(dictType));
    }

    @PutMapping("/types/{id}")
    public Result updateDictType(@PathVariable Long id, @RequestBody DictType dictType) {
        return Result.success(dictService.updateDictType(id, dictType));
    }

    @DeleteMapping("/types/{id}")
    public Result deleteDictType(@PathVariable Long id) {
        dictService.deleteDictType(id);
        return Result.success();
    }

    @GetMapping("/items/{dictCode}")
    public Result getDictItems(@PathVariable String dictCode) {
        return Result.success(dictService.getDictItems(dictCode));
    }

    @GetMapping("/map/{dictCode}")
    public Result getDictMap(@PathVariable String dictCode) {
        return Result.success(dictService.getDictMap(dictCode));
    }

    @PostMapping("/items")
    public Result createDictItem(@RequestBody DictItem dictItem) {
        return Result.success(dictService.createDictItem(dictItem));
    }

    @PutMapping("/items/{id}")
    public Result updateDictItem(@PathVariable Long id, @RequestBody DictItem dictItem) {
        return Result.success(dictService.updateDictItem(id, dictItem));
    }

    @DeleteMapping("/items/{id}")
    public Result deleteDictItem(@PathVariable Long id) {
        dictService.deleteDictItem(id);
        return Result.success();
    }

    @PostMapping("/items/batch/{dictCode}")
    public Result batchSaveItems(@PathVariable String dictCode, @RequestBody List<DictItem> items) {
        dictService.batchSaveItems(dictCode, items);
        return Result.success();
    }
}
