# Seed Releases

启动时由 `com.back.lowcode.release.seed.SeedReleaseLoader` 扫描本目录下所有
`*.json` 文件并按内容反向回灌 release 快照到数据库。

## 文件来源

通过 `GET /lowcode/release/{id}/export/file` 把一次真实 release 的产物导出为 JSON。
见 `back/scripts/m4-seed-export.http`。

## 文件格式

```jsonc
{
  "schemaVersion": 1,
  "release": { "appCode": "...", "version": "1.0.0", "status": "active", ... },
  "businessApp": { "code": "...", "name": "...", "icon": "...", "color": "..." },
  "items": [
    { "itemType": "entity|page|component|dict|menu", "itemCode": "...", "checksum": "...", "snapshotJson": "..." }
  ],
  "menus": [
    { "exportId": 12, "exportParentId": null, "appCode": "...", "name": "...", "pageCode": "..." }
  ]
}
```

## 幂等性

每个文件按 `(appCode, version)` 主键判定：数据库已有对应 release → 整文件跳过。
即"已经回灌过的不会重复"。若需要重新回灌，先在 DB 删该行：

```sql
DELETE FROM lc_release_item WHERE release_id IN (SELECT id FROM lc_release WHERE app_code='leave_management' AND version='1.0.0');
DELETE FROM lc_app_menu WHERE release_id IN (SELECT id FROM lc_release WHERE app_code='leave_management' AND version='1.0.0');
DELETE FROM lc_app_menu WHERE app_code='leave_management' AND release_id IS NULL;
DELETE FROM lc_release WHERE app_code='leave_management' AND version='1.0.0';
```
