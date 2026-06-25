package com.back.lowcode.config;

import com.back.lowcode.entity.BusinessApp;
import com.back.lowcode.entity.PageSchema;
import com.back.lowcode.repository.BusinessAppRepository;
import com.back.lowcode.repository.PageSchemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 初始化业务应用 —— 将已有的请假管理页面归入"请假管理系统"应用
 * 在 LeavePageInitializer/LeavePageInitializerV2(Order=2) 之后执行
 */
@Component
@Order(3)
@RequiredArgsConstructor
public class BusinessAppInitializer implements CommandLineRunner {

    private final BusinessAppRepository businessAppRepository;
    private final PageSchemaRepository pageSchemaRepository;

    private static final String LEAVE_APP_CODE = "leave_management";
    private static final String LEAVE_APP_NAME = "请假管理系统";
    private static final String LEAVE_APP_DESC = "学生请假申请、审批与记录管理——支持学生/辅导员/系主任/管理员四类角色";

    @Override
    @Transactional
    public void run(String... args) {
        // 1. 创建"请假管理系统"应用（如果不存在）
        BusinessApp leaveApp = businessAppRepository.findByCode(LEAVE_APP_CODE).orElse(null);
        if (leaveApp == null) {
            leaveApp = new BusinessApp();
            leaveApp.setCode(LEAVE_APP_CODE);
            leaveApp.setName(LEAVE_APP_NAME);
            leaveApp.setDescription(LEAVE_APP_DESC);
            leaveApp.setIcon("calendar");
            leaveApp.setColor("#e8a317");
            leaveApp = businessAppRepository.save(leaveApp);
        } else {
            // 更新描述
            if (!LEAVE_APP_DESC.equals(leaveApp.getDescription())) {
                leaveApp.setDescription(LEAVE_APP_DESC);
                businessAppRepository.save(leaveApp);
            }
        }

        // 2. 将已有的 leave_ 页面归入"请假管理系统"（包括V2新页面）
        assignLeavePages();
    }

    @Transactional
    public void assignLeavePages() {
        List<PageSchema> allPages = pageSchemaRepository.findAll();
        int count = 0;
        for (PageSchema page : allPages) {
            if (page.getPageCode() != null && page.getPageCode().startsWith("leave_")
                    && !LEAVE_APP_CODE.equals(page.getAppCode())) {
                page.setAppCode(LEAVE_APP_CODE);
                pageSchemaRepository.save(page);
                count++;
            }
        }
    }
}
