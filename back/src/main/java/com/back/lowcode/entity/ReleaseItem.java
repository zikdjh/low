package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发布单中的单个产物快照 — entity / page / component / dict / menu / codegen 其中之一
 * <p>
 * snapshotJson 反映"发布瞬间"的设计态全量序列化结果；
 * checksum 为 snapshotJson 的 sha256，用于跨 release 内容去重。
 */
@Entity
@Table(name = "lc_release_item",
        indexes = {
                @Index(name = "idx_release", columnList = "release_id"),
                @Index(name = "idx_type_code", columnList = "item_type,item_code")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "release_id", nullable = false)
    private Long releaseId;

    /** 产物类型：entity | page | component | dict | menu | codegen */
    @Column(name = "item_type", nullable = false, length = 32)
    private String itemType;

    /** 产物自然 key（页面用 pageCode，实体用 entityCode 等） */
    @Column(name = "item_code", nullable = false, length = 128)
    private String itemCode;

    @Column(name = "snapshot_json", nullable = false, columnDefinition = "LONGTEXT")
    private String snapshotJson;

    @Column(name = "checksum", nullable = false, length = 64)
    private String checksum;
}
