package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.entity.BusinessApp;
import com.back.lowcode.service.BusinessAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lowcode/app")
@RequiredArgsConstructor
public class BusinessAppController {

    private final BusinessAppService businessAppService;

    @GetMapping
    public Result getAllApps() {
        return Result.success(businessAppService.getAllApps());
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        BusinessApp app = businessAppService.getById(id);
        if (app == null) {
            return Result.error("应用不存在");
        }
        return Result.success(app);
    }

    @GetMapping("/code/{code}")
    public Result getByCode(@PathVariable String code) {
        BusinessApp app = businessAppService.getByCode(code);
        if (app == null) {
            return Result.error("应用不存在");
        }
        return Result.success(app);
    }

    @GetMapping("/{appCode}/pages")
    public Result getAppPages(@PathVariable String appCode) {
        return Result.success(businessAppService.getAppPages(appCode));
    }

    @PostMapping
    public Result create(@RequestBody BusinessApp app) {
        return Result.success(businessAppService.create(app));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody BusinessApp app) {
        return Result.success(businessAppService.update(id, app));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        businessAppService.delete(id);
        return Result.success();
    }

    @PostMapping("/{appCode}/assign/{pageId}")
    public Result assignPage(@PathVariable String appCode, @PathVariable Long pageId) {
        businessAppService.assignPageToApp(pageId, appCode);
        return Result.success();
    }

    /**
     * 批量将多个页面聚合到某个业务应用
     */
    @PostMapping("/{appCode}/assign-batch")
    public Result assignBatchPages(@PathVariable String appCode, @RequestBody Map<String, List<Long>> body) {
        List<Long> pageIds = body.get("pageIds");
        if (pageIds == null || pageIds.isEmpty()) {
            return Result.error("请选择至少一个页面");
        }
        businessAppService.assignPagesToApp(pageIds, appCode);
        return Result.success("已将 " + pageIds.size() + " 个页面聚合到应用 " + appCode);
    }
}
