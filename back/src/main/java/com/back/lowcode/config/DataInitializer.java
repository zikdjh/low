package com.back.lowcode.config;

import com.back.lowcode.entity.ComponentDef;
import com.back.lowcode.entity.DictItem;
import com.back.lowcode.entity.DictType;
import com.back.lowcode.repository.ComponentDefRepository;
import com.back.lowcode.repository.DictItemRepository;
import com.back.lowcode.repository.DictTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ComponentDefRepository componentDefRepository;
    private final DictTypeRepository dictTypeRepository;
    private final DictItemRepository dictItemRepository;

    @Override
    public void run(String... args) {
        initComponents();
        initDicts();
    }

    private void initComponents() {
        if (componentDefRepository.count() > 0) {
            return;
        }

        List<ComponentDef> components = Arrays.asList(
                createComponent("text", "文本", "基础", "font", "文本展示组件",
                        "{\"text\": \"Hello World\", \"color\": \"#333\", \"fontSize\": 14}",
                        "{\"type\": \"object\", \"properties\": {\"text\": {\"type\": \"string\", \"title\": \"文本内容\"}, \"color\": {\"type\": \"string\", \"title\": \"颜色\"}, \"fontSize\": {\"type\": \"number\", \"title\": \"字体大小\"}}}", 1),
                
                createComponent("button", "按钮", "基础", "button", "按钮组件",
                        "{\"content\": \"确定\", \"type\": \"primary\", \"size\": \"medium\"}",
                        "{\"type\": \"object\", \"properties\": {\"content\": {\"type\": \"string\", \"title\": \"按钮文字\"}, \"type\": {\"type\": \"string\", \"title\": \"类型\", \"enum\": [\"primary\", \"secondary\", \"outline\", \"ghost\"]}, \"size\": {\"type\": \"string\", \"title\": \"大小\", \"enum\": [\"small\", \"medium\", \"large\"]}}}", 2),
                
                createComponent("divider", "分割线", "基础", "minus", "分割线组件",
                        "{\"orientation\": \"horizontal\", \"dashed\": false}",
                        "{\"type\": \"object\", \"properties\": {\"orientation\": {\"type\": \"string\", \"title\": \"方向\", \"enum\": [\"horizontal\", \"vertical\"]}, \"dashed\": {\"type\": \"boolean\", \"title\": \"虚线\"}}}", 3),
                
                createComponent("input", "输入框", "表单", "edit", "输入框组件",
                        "{\"label\": \"输入\", \"placeholder\": \"请输入...\", \"required\": false}",
                        "{\"type\": \"object\", \"properties\": {\"label\": {\"type\": \"string\", \"title\": \"标签\"}, \"placeholder\": {\"type\": \"string\", \"title\": \"占位符\"}, \"required\": {\"type\": \"boolean\", \"title\": \"必填\"}}}", 1),
                
                createComponent("select", "下拉选择", "表单", "chevron-down", "下拉选择组件",
                        "{\"label\": \"选择\", \"options\": [], \"dictCode\": \"\", \"required\": false}",
                        "{\"type\": \"object\", \"properties\": {\"label\": {\"type\": \"string\", \"title\": \"标签\"}, \"dictCode\": {\"type\": \"string\", \"title\": \"字典编码\"}, \"required\": {\"type\": \"boolean\", \"title\": \"必填\"}}}", 2),
                
                createComponent("checkbox", "复选框", "表单", "check-circle", "复选框组件",
                        "{\"label\": \"选项\", \"checked\": false}",
                        "{\"type\": \"object\", \"properties\": {\"label\": {\"type\": \"string\", \"title\": \"标签\"}, \"checked\": {\"type\": \"boolean\", \"title\": \"默认选中\"}}}", 3),
                
                createComponent("radio", "单选框", "表单", "circle", "单选框组件",
                        "{\"label\": \"选项\", \"options\": [], \"value\": \"\"}",
                        "{\"type\": \"object\", \"properties\": {\"label\": {\"type\": \"string\", \"title\": \"标签\"}, \"options\": {\"type\": \"array\", \"items\": {\"type\": \"object\", \"properties\": {\"label\": {}, \"value\": {}}}, \"title\": \"选项\"}}}", 4),
                
                createComponent("date", "日期选择", "表单", "calendar", "日期选择组件",
                        "{\"label\": \"日期\", \"type\": \"date\", \"required\": false}",
                        "{\"type\": \"object\", \"properties\": {\"label\": {\"type\": \"string\", \"title\": \"标签\"}, \"type\": {\"type\": \"string\", \"title\": \"类型\", \"enum\": [\"date\", \"datetime\", \"daterange\"]}, \"required\": {\"type\": \"boolean\", \"title\": \"必填\"}}}", 5),
                
                createComponent("grid", "栅格", "布局", "layout-grid", "栅格布局组件",
                        "{\"columns\": 12, \"gutter\": 16, \"children\": []}",
                        "{\"type\": \"object\", \"properties\": {\"columns\": {\"type\": \"number\", \"title\": \"总列数\"}, \"gutter\": {\"type\": \"number\", \"title\": \"间距\"}}}", 1),
                
                createComponent("space", "间距", "布局", "spacing-horizontal", "间距组件",
                        "{\"size\": \"medium\", \"direction\": \"horizontal\"}",
                        "{\"type\": \"object\", \"properties\": {\"size\": {\"type\": \"string\", \"title\": \"大小\", \"enum\": [\"small\", \"medium\", \"large\"]}, \"direction\": {\"type\": \"string\", \"title\": \"方向\", \"enum\": [\"horizontal\", \"vertical\"]}}}", 2),
                
                createComponent("card", "卡片", "容器", "credit-card", "卡片容器组件",
                        "{\"title\": \"\", \"bordered\": true, \"hoverable\": false}",
                        "{\"type\": \"object\", \"properties\": {\"title\": {\"type\": \"string\", \"title\": \"标题\"}, \"bordered\": {\"type\": \"boolean\", \"title\": \"显示边框\"}, \"hoverable\": {\"type\": \"boolean\", \"title\": \"悬停效果\"}}}", 1),
                
                createComponent("form", "表单", "容器", "form", "表单容器组件",
                        "{\"labelWidth\": 100, \"colon\": true}",
                        "{\"type\": \"object\", \"properties\": {\"labelWidth\": {\"type\": \"number\", \"title\": \"标签宽度\"}, \"colon\": {\"type\": \"boolean\", \"title\": \"显示冒号\"}}}", 2),
                
                createComponent("table", "表格", "数据", "table", "数据表格组件",
                        "{\"columns\": [], \"entityCode\": \"\", \"pageSize\": 10}",
                        "{\"type\": \"object\", \"properties\": {\"entityCode\": {\"type\": \"string\", \"title\": \"实体编码\"}, \"pageSize\": {\"type\": \"number\", \"title\": \"每页条数\"}}}", 1),
                
                createComponent("list", "列表", "数据", "list", "列表组件",
                        "{\"data\": [], \"layout\": \"vertical\"}",
                        "{\"type\": \"object\", \"properties\": {\"layout\": {\"type\": \"string\", \"title\": \"布局\", \"enum\": [\"vertical\", \"horizontal\"]}}}", 2),
                
                createComponent("chart", "图表", "数据", "bar-chart", "图表组件",
                        "{\"type\": \"bar\", \"title\": \"\", \"data\": []}",
                        "{\"type\": \"object\", \"properties\": {\"type\": {\"type\": \"string\", \"title\": \"图表类型\", \"enum\": [\"bar\", \"line\", \"pie\"]}, \"title\": {\"type\": \"string\", \"title\": \"标题\"}}}", 3)
        );

        componentDefRepository.saveAll(components);
    }

    private ComponentDef createComponent(String compKey, String name, String category, String icon,
                                          String description, String defaultPropsJson, String propsSchemaJson,
                                          Integer groupIndex) {
        ComponentDef component = new ComponentDef();
        component.setCompKey(compKey);
        component.setName(name);
        component.setCategory(category);
        component.setIcon(icon);
        component.setDescription(description);
        component.setDefaultPropsJson(defaultPropsJson);
        component.setPropsSchemaJson(propsSchemaJson);
        component.setGroupIndex(groupIndex);
        component.setIsSystem(true);
        component.setStatus("active");
        return component;
    }

    private void initDicts() {
        if (dictTypeRepository.count() > 0) {
            return;
        }

        DictType statusDict = new DictType();
        statusDict.setDictCode("status");
        statusDict.setName("状态");
        statusDict.setDescription("通用状态字典");
        statusDict.setIsSystem(true);
        statusDict.setStatus("active");
        dictTypeRepository.save(statusDict);

        DictType genderDict = new DictType();
        genderDict.setDictCode("gender");
        genderDict.setName("性别");
        genderDict.setDescription("性别字典");
        genderDict.setIsSystem(true);
        genderDict.setStatus("active");
        dictTypeRepository.save(genderDict);

        DictType yesNoDict = new DictType();
        yesNoDict.setDictCode("yes_no");
        yesNoDict.setName("是/否");
        yesNoDict.setDescription("是/否选择");
        yesNoDict.setIsSystem(true);
        yesNoDict.setStatus("active");
        dictTypeRepository.save(yesNoDict);

        List<DictItem> statusItems = Arrays.asList(
                createDictItem("status", "active", "启用", 1, "#00B42A"),
                createDictItem("status", "inactive", "禁用", 2, "#F53F3F"),
                createDictItem("status", "draft", "草稿", 3, "#8F8F9D"),
                createDictItem("status", "archived", "归档", 4, "#969799")
        );

        List<DictItem> genderItems = Arrays.asList(
                createDictItem("gender", "male", "男", 1, ""),
                createDictItem("gender", "female", "女", 2, "")
        );

        List<DictItem> yesNoItems = Arrays.asList(
                createDictItem("yes_no", "yes", "是", 1, "#00B42A"),
                createDictItem("yes_no", "no", "否", 2, "#F53F3F")
        );

        dictItemRepository.saveAll(statusItems);
        dictItemRepository.saveAll(genderItems);
        dictItemRepository.saveAll(yesNoItems);
    }

    private DictItem createDictItem(String dictCode, String itemKey, String itemValue, Integer sortOrder, String color) {
        DictItem item = new DictItem();
        item.setDictCode(dictCode);
        item.setItemKey(itemKey);
        item.setItemValue(itemValue);
        item.setSortOrder(sortOrder);
        item.setColor(color);
        item.setStatus("active");
        return item;
    }
}