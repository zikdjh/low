package com.back.lowcode.repository;

import com.back.lowcode.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    /** 查应用某个 release 的全部菜单（按 parent + sortOrder 排好序，前端自行组树） */
    @Query("select m from Menu m where m.appCode = :appCode and m.releaseId = :releaseId " +
            "order by coalesce(m.parentId, 0), m.sortOrder, m.id")
    List<Menu> findReleaseMenu(@Param("appCode") String appCode, @Param("releaseId") Long releaseId);

    /** 查应用草稿菜单（release_id IS NULL） */
    @Query("select m from Menu m where m.appCode = :appCode and m.releaseId is null " +
            "order by coalesce(m.parentId, 0), m.sortOrder, m.id")
    List<Menu> findDraftMenu(@Param("appCode") String appCode);

    @Modifying
    @Query("delete from Menu m where m.appCode = :appCode and m.releaseId is null")
    int deleteDraftByApp(@Param("appCode") String appCode);

    @Modifying
    @Query("delete from Menu m where m.releaseId = :releaseId")
    int deleteByReleaseId(@Param("releaseId") Long releaseId);
}
