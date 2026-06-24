package com.back.lowcode.release;

import com.back.common.Result;
import com.back.lowcode.entity.Release;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 发布管理 REST 接口
 * <ul>
 *   <li>POST   /lowcode/release/{appCode}             —— 发起一次发布</li>
 *   <li>GET    /lowcode/release/{appCode}             —— 列出该应用历史发布</li>
 *   <li>GET    /lowcode/release/detail/{id}           —— 单次发布详情（含阶段日志）</li>
 *   <li>GET    /lowcode/release/{id}/items            —— 快照清单</li>
 *   <li>POST   /lowcode/release/{id}/rollback         —— 回滚到该 release</li>
 *   <li>POST   /lowcode/release/{id}/activate         —— 把 draft release 激活</li>
 * </ul>
 */
@RestController
@RequestMapping("/lowcode/release")
@RequiredArgsConstructor
public class ReleaseController {

    private final ReleaseService releaseService;

    @PostMapping("/{appCode}")
    public Result publish(@PathVariable String appCode, @RequestBody(required = false) PublishRequest req) {
        if (req == null) {
            req = new PublishRequest();
        }
        boolean activate = req.getActivate() == null || req.getActivate();
        Release r = releaseService.publish(appCode, req.getVersionBump(), req.getNotes(), req.getCreatedBy(), activate);
        return Result.success(r);
    }

    @GetMapping("/{appCode}")
    public Result listByApp(@PathVariable String appCode) {
        return Result.success(releaseService.listByApp(appCode));
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        Release r = releaseService.getById(id);
        if (r == null) {
            return Result.error("Release 不存在");
        }
        ReleaseDetail detail = new ReleaseDetail();
        detail.setRelease(r);
        detail.setLogs(releaseService.getLogs(id));
        return Result.success(detail);
    }

    @GetMapping("/{id}/items")
    public Result items(@PathVariable Long id) {
        return Result.success(releaseService.getItems(id));
    }

    @PostMapping("/{id}/rollback")
    public Result rollback(@PathVariable Long id) {
        return Result.success(releaseService.rollback(id));
    }

    @PostMapping("/{id}/activate")
    public Result activate(@PathVariable Long id) {
        return Result.success(releaseService.activate(id));
    }

    @Data
    public static class PublishRequest {
        /** major | minor | patch，默认 patch */
        private String versionBump;
        private String notes;
        private String createdBy;
        /** 是否在流水线末尾自动激活，默认 true */
        private Boolean activate;
    }

    @Data
    public static class ReleaseDetail {
        private Release release;
        private java.util.List<com.back.lowcode.entity.ReleaseLog> logs;
    }
}
