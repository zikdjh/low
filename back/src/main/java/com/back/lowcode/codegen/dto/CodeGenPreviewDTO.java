package com.back.lowcode.codegen.dto;

import lombok.Data;

import java.util.Map;

/**
 * 代码生成器预览结果。
 *
 * <p>{@link #files} 的键为相对于代码包根目录的路径（如
 * {@code backend/src/main/java/com/back/generated/entity/Customer.java}），
 * 值为该文件的渲染后内容。</p>
 */
@Data
public class CodeGenPreviewDTO {

    /** 实体编码 */
    private String entityCode;

    /** 实体显示名 */
    private String entityName;

    /** 生成的 Java 类名（PascalCase） */
    private String className;

    /** 文件 path -> content */
    private Map<String, String> files;
}
