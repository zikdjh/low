package com.back.lowcode.lcmenu;

import com.back.lowcode.entity.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点 DTO（设计器读写 / 运行时下发的统一形态）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuNode {

    private Long id;
    private Long parentId;
    private String name;
    private String icon;
    private Integer sortOrder;
    private String pageCode;
    private String routePath;
    /** menu | group | external */
    private String menuType;
    private Boolean visible;

    /** 子节点（叶子节点为空列表） */
    @Builder.Default
    private List<MenuNode> children = new ArrayList<>();

    public static MenuNode from(Menu m) {
        return MenuNode.builder()
                .id(m.getId())
                .parentId(m.getParentId())
                .name(m.getName())
                .icon(m.getIcon())
                .sortOrder(m.getSortOrder())
                .pageCode(m.getPageCode())
                .routePath(m.getRoutePath())
                .menuType(m.getMenuType())
                .visible(m.getVisible())
                .children(new ArrayList<>())
                .build();
    }
}
