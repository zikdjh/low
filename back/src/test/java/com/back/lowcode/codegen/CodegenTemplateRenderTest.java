package com.back.lowcode.codegen;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Freemarker 模板渲染单测 — 不连库，构造仿真的 root context 验证每个模板可渲染通过。
 * <p>
 * 这里把"模板语法正确性 + 关键字段是否出现在产物"作为冒烟覆盖；
 * 完整集成测试由 release 流水线在真实 Spring 上下文里完成。
 */
class CodegenTemplateRenderTest {

    private static Configuration cfg;

    @BeforeAll
    static void setup() {
        cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassForTemplateLoading(CodegenTemplateRenderTest.class, "/codegen/templates");
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setNumberFormat("0.######");
    }

    private static String render(String path, Map<String, Object> root) throws Exception {
        Template t = cfg.getTemplate(path);
        Writer w = new StringWriter();
        t.process(root, w);
        return w.toString();
    }

    @Test
    void entityTemplate() throws Exception {
        Map<String, Object> root = sampleEntityRoot();
        String out = render("java/entity.java.ftl", root);
        assertTrue(out.contains("public class Customer"));
        assertTrue(out.contains("private String customerName"));
        assertTrue(out.contains("@Entity"));
        assertTrue(out.contains("@Table(name = \"lc_customer\")"));
        assertTrue(out.contains("import java.math.BigDecimal"));
    }

    @Test
    void repositoryTemplate() throws Exception {
        Map<String, Object> root = sampleEntityRoot();
        String out = render("java/repository.java.ftl", root);
        assertTrue(out.contains("interface CustomerRepository"));
        assertTrue(out.contains("JpaRepository<Customer, Long>"));
    }

    @Test
    void serviceTemplate() throws Exception {
        Map<String, Object> root = sampleEntityRoot();
        String out = render("java/service.java.ftl", root);
        assertTrue(out.contains("class CustomerService"));
        assertTrue(out.contains("CustomerRepository repository"));
        assertTrue(out.contains("save(Customer entity)"));
    }

    @Test
    void controllerTemplate() throws Exception {
        Map<String, Object> root = sampleEntityRoot();
        String out = render("java/controller.java.ftl", root);
        assertTrue(out.contains("class CustomerController"));
        assertTrue(out.contains("@RequestMapping(\"/sales/customer\")"));
        assertTrue(out.contains("@PathVariable Long id"));
    }

    @Test
    void dtoAndQueryTemplate() throws Exception {
        Map<String, Object> root = sampleEntityRoot();
        String dto = render("java/dto.java.ftl", root);
        assertTrue(dto.contains("class CustomerDTO"));
        String query = render("java/query.java.ftl", root);
        assertTrue(query.contains("class CustomerQuery"));
        // Query 仅含 showInSearch=true 字段：sample 里只有 customerName=true
        assertTrue(query.contains("customerName"));
        assertFalse(query.contains("private BigDecimal balance"));
    }

    @Test
    void ddlTemplate() throws Exception {
        Map<String, Object> root = appWideRoot();
        String out = render("sql/ddl.sql.ftl", root);
        assertTrue(out.contains("CREATE TABLE `lc_customer`"));
        assertTrue(out.contains("VARCHAR(100)"));
        assertTrue(out.contains("DECIMAL(10,2)"));
        assertTrue(out.contains("ENGINE=InnoDB"));
    }

    @Test
    void listPageTemplate() throws Exception {
        Map<String, Object> root = pageRoot("list");
        String out = render("vue/list-page.vue.ftl", root);
        assertTrue(out.contains("<t-table"));
        assertTrue(out.contains("customerApi"));
        assertTrue(out.contains("colKey: 'customerName'"));
    }

    @Test
    void formPageTemplate() throws Exception {
        Map<String, Object> root = pageRoot("form");
        String out = render("vue/form-page.vue.ftl", root);
        assertTrue(out.contains("<t-form"));
        assertTrue(out.contains("v-model=\"form.customerName\""));
        assertTrue(out.contains("v-model=\"form.balance\""));
    }

    @Test
    void detailPageTemplate() throws Exception {
        Map<String, Object> root = pageRoot("detail");
        String out = render("vue/detail-page.vue.ftl", root);
        assertTrue(out.contains("<t-descriptions"));
        assertTrue(out.contains("customerApi.get"));
    }

    @Test
    void apiTemplate() throws Exception {
        Map<String, Object> root = sampleEntityRoot();
        String out = render("vue/api.ts.ftl", root);
        assertTrue(out.contains("interface Customer"));
        assertTrue(out.contains("customerApi"));
        assertTrue(out.contains("/sales/customer"));
    }

    @Test
    void routerAndMenuTemplate() throws Exception {
        Map<String, Object> root = appWideRoot();
        String router = render("vue/router.ts.ftl", root);
        assertTrue(router.contains("path: '/sales'"));
        assertTrue(router.contains("'../../views/sales/CustomerListPage.vue'"));

        String menu = render("vue/menu.ts.ftl", root);
        assertTrue(menu.contains("salesMenu"));
        assertTrue(menu.contains("\"客户管理\""));
    }

    // ====== 测试桩 ======
    private Map<String, Object> sampleEntityRoot() {
        Map<String, Object> entity = sampleEntity();
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("appCode", "sales");
        root.put("appName", "销售管理");
        root.put("version", "1.0.0");
        root.put("releaseId", 1L);
        root.put("basePackage", "com.generated.sales");
        root.put("entity", entity);
        root.put("pkType", "Long");
        return root;
    }

    private Map<String, Object> appWideRoot() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("appCode", "sales");
        root.put("appName", "销售管理");
        root.put("version", "1.0.0");
        root.put("releaseId", 1L);
        root.put("basePackage", "com.generated.sales");
        root.put("checksum", "abc123");
        root.put("entities", List.of(sampleEntity()));
        root.put("pages", List.of(samplePage()));
        root.put("dicts", new ArrayList<>());
        root.put("components", new ArrayList<>());
        root.put("menu", List.of(sampleMenuNode()));
        return root;
    }

    private Map<String, Object> pageRoot(String pageType) {
        Map<String, Object> root = sampleEntityRoot();
        Map<String, Object> page = samplePage();
        page.put("pageType", pageType);
        root.put("page", page);
        return root;
    }

    private Map<String, Object> sampleEntity() {
        Map<String, Object> pk = field("id", "id", "LONG", "Long", "number", "null",
                false, false, true, true, true, true, false);
        Map<String, Object> nameF = field("customer_name", "客户姓名", "VARCHAR", "String",
                "string", "''", false, false, false, false, true, true, true);
        nameF.put("length", 100);
        rebuildSqlType(nameF);
        Map<String, Object> balanceF = field("balance", "余额", "DECIMAL", "BigDecimal",
                "number", "null", true, false, false, false, true, true, false);
        balanceF.put("precision", 10);
        balanceF.put("scale", 2);
        rebuildSqlType(balanceF);
        Map<String, Object> createdF = field("created_at", "创建时间", "DATETIME", "LocalDateTime",
                "string", "''", false, true, false, false, true, false, false);

        Map<String, Object> entity = new LinkedHashMap<>();
        entity.put("code", "customer");
        entity.put("name", "客户");
        entity.put("tableName", "lc_customer");
        entity.put("description", "客户主数据");
        entity.put("status", "published");
        entity.put("className", "Customer");
        entity.put("varName", "customer");
        entity.put("fields", List.of(pk, nameF, balanceF, createdF));
        entity.put("imports", Set.of("java.math.BigDecimal", "java.time.LocalDateTime"));
        entity.put("primaryKeyField", pk);
        entity.put("hasReference", false);
        return entity;
    }

    private Map<String, Object> samplePage() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("pageCode", "customer_list");
        p.put("name", "客户列表");
        p.put("entityCode", "customer");
        p.put("pageType", "list");
        p.put("componentName", "CustomerListPage");
        p.put("layout", new LinkedHashMap<>());
        p.put("layoutJson", "{}");
        return p;
    }

    private Map<String, Object> sampleMenuNode() {
        Map<String, Object> n = new LinkedHashMap<>();
        n.put("name", "客户管理");
        n.put("icon", "user");
        n.put("pageCode", "customer_list");
        n.put("routePath", "/run/sales/customer_list");
        n.put("menuType", "menu");
        n.put("visible", true);
        n.put("children", new ArrayList<>());
        return n;
    }

    /**
     * @param numeric numeric / temporal / showInSearch / decimalType / primary / autoInc / showInList / showInForm / nullable
     */
    private Map<String, Object> field(String code, String name, String fieldType, String javaType,
                                       String tsType, String tsDefault, boolean numeric, boolean temporal,
                                       boolean primary, boolean autoInc,
                                       boolean showInList, boolean showInForm, boolean showInSearch) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("code", code);
        f.put("name", name);
        f.put("columnName", code);
        f.put("fieldType", fieldType);
        f.put("javaType", javaType);
        f.put("tsType", tsType);
        f.put("tsDefault", tsDefault);
        f.put("camelName", CodegenTypeMapper.toCamelCase(code));
        f.put("pascalName", CodegenTypeMapper.toPascalCase(code));
        f.put("mysqlType", CodegenTypeMapper.mysqlColumnType(fieldType, null, null, null));
        f.put("nullable", !primary);
        f.put("isPrimaryKey", primary);
        f.put("isAutoIncrement", autoInc);
        f.put("showInList", showInList);
        f.put("showInForm", showInForm);
        f.put("showInSearch", showInSearch);
        f.put("numeric", numeric);
        f.put("temporal", temporal);
        return rebuildSqlType(f);
    }

    private Map<String, Object> rebuildSqlType(Map<String, Object> f) {
        f.put("mysqlType", CodegenTypeMapper.mysqlColumnType(
                (String) f.get("fieldType"),
                (Integer) f.get("length"),
                (Integer) f.get("precision"),
                (Integer) f.get("scale")));
        return f;
    }
}
