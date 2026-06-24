package com.back.lowcode.lcmenu;

import lombok.Data;

/** 菜单拖拽排序入参单项 */
@Data
public class ReorderItem {
    /** 菜单 id（必填） */
    private Long id;
    /** 调整后的父菜单 id；根节点为 null */
    private Long parentId;
    /** 同层新顺序，0 起 */
    private Integer sortOrder;
}
