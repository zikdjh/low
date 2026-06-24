package com.back.lowcode.lcmenu;

import com.back.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用菜单接口
 * <ul>
 *   <li>GET   /lowcode/app/{appCode}/menu              —— 终端读 active release 菜单</li>
 *   <li>GET   /lowcode/app/{appCode}/menu/draft        —— 设计器读草稿</li>
 *   <li>PUT   /lowcode/app/{appCode}/menu/draft        —— 设计器全量保存草稿</li>
 *   <li>POST  /lowcode/app/{appCode}/menu/reorder      —— 拖拽局部排序</li>
 *   <li>GET   /lowcode/app/{appCode}/menu/release/{id} —— 查指定 release 菜单（用于历史回看）</li>
 * </ul>
 */
@RestController
@RequestMapping("/lowcode/app")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/{appCode}/menu")
    public Result getActiveMenu(@PathVariable String appCode) {
        return Result.success(menuService.getActiveMenuTree(appCode));
    }

    @GetMapping("/{appCode}/menu/draft")
    public Result getDraftMenu(@PathVariable String appCode) {
        return Result.success(menuService.getDraftTree(appCode));
    }

    @PutMapping("/{appCode}/menu/draft")
    public Result saveDraftMenu(@PathVariable String appCode, @RequestBody List<MenuNode> tree) {
        return Result.success(menuService.saveDraft(appCode, tree));
    }

    @PostMapping("/{appCode}/menu/reorder")
    public Result reorder(@PathVariable String appCode, @RequestBody List<ReorderItem> items) {
        menuService.reorder(appCode, items);
        return Result.success();
    }

    @GetMapping("/{appCode}/menu/release/{releaseId}")
    public Result getMenuByRelease(@PathVariable String appCode, @PathVariable Long releaseId) {
        return Result.success(menuService.getMenuTreeByRelease(appCode, releaseId));
    }
}
