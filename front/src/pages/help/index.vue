<template>
  <div class="help-center">
    <!-- 页面头部 -->
    <div class="help-header">
      <div class="header-content">
        <div class="header-left">
          <div class="header-icon">
            <HelpCircleIcon size="48" />
          </div>
          <div class="header-text">
            <h1>帮助中心</h1>
            <p>查找常见问题解答和使用指南</p>
          </div>
        </div>
        <t-button variant="outline" class="back-btn" @click="goHome">
          <template #icon><HomeIcon size="16" /></template>
          返回主页
        </t-button>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-links">
      <div
        v-for="item in quickLinks"
        :key="item.id"
        class="quick-link-card"
        @click="scrollToSection(item.section)"
      >
        <div class="ql-icon" :style="{ background: item.color }">
          <component :is="item.icon" size="24" />
        </div>
        <div class="ql-content">
          <span class="ql-title">{{ item.title }}</span>
          <span class="ql-desc">{{ item.desc }}</span>
        </div>
        <ChevronRightIcon size="20" />
      </div>
    </div>

    <!-- 帮助内容 -->
    <div class="help-content">
      <div class="content-sidebar">
        <div
          v-for="section in helpSections"
          :key="section.id"
          class="sidebar-section"
        >
          <div class="section-title">
            <component :is="section.icon" size="16" />
            <span>{{ section.title }}</span>
          </div>
          <div class="section-links">
            <div
              v-for="link in section.links"
              :key="link.id"
              class="sidebar-link"
              :class="{ active: activeSection === link.id }"
              @click="scrollToSection(link.id)"
            >
              {{ link.title }}
            </div>
          </div>
        </div>
      </div>

      <div class="content-main" ref="contentMainRef">
        <!-- 入门指南 -->
        <section id="getting-started" class="help-section">
          <div class="section-header">
            <BookOpenIcon size="24" />
            <h2>入门指南</h2>
          </div>
          <div class="section-content">
            <p class="section-intro">欢迎使用低代码开发平台！按照以下步骤快速上手：</p>
            <div class="guide-cards">
              <div class="guide-card" @click="openGuide(1)">
                <div class="gc-step">01</div>
                <div class="gc-content">
                  <h3>创建您的第一个页面</h3>
                  <p>了解如何使用低代码平台创建您的第一个页面应用。从组件库拖拽组件到画布，自由编辑布局。</p>
                  <span class="gc-link">查看详情 →</span>
                </div>
              </div>
              <div class="guide-card" @click="openGuide(2)">
                <div class="gc-step">02</div>
                <div class="gc-content">
                  <h3>设计数据模型</h3>
                  <p>学习如何设计和管理应用的数据实体和关系。支持多种字段类型和关联关系配置。</p>
                  <span class="gc-link">查看详情 →</span>
                </div>
              </div>
              <div class="guide-card" @click="openGuide(3)">
                <div class="gc-step">03</div>
                <div class="gc-content">
                  <h3>预览与发布</h3>
                  <p>完成页面设计后在设计器中预览效果，确认无误后一键发布到生产环境。</p>
                  <span class="gc-link">查看详情 →</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- 常见问题 -->
        <section id="faq" class="help-section">
          <div class="section-header">
            <ChatIcon size="24" />
            <h2>常见问题</h2>
          </div>
          <div class="section-content">
            <t-collapse v-model="activeFaq" accordion>
              <t-collapse-panel
                v-for="faq in faqs"
                :key="faq.id"
                :value="faq.id"
                :header="faq.question"
              >
                <p>{{ faq.answer }}</p>
              </t-collapse-panel>
            </t-collapse>
          </div>
        </section>

        <!-- 功能说明 -->
        <section id="features" class="help-section">
          <div class="section-header">
            <LayoutIcon size="24" />
            <h2>功能说明</h2>
          </div>
          <div class="section-content">
            <div class="feature-grid">
              <div v-for="feature in features" :key="feature.id" class="feature-card">
                <div class="feature-icon" :style="{ background: feature.color }">
                  <component :is="feature.icon" size="20" />
                </div>
                <h3>{{ feature.title }}</h3>
                <p>{{ feature.desc }}</p>
                <ul class="feature-list">
                  <li v-for="item in feature.items" :key="item">{{ item }}</li>
                </ul>
              </div>
            </div>
          </div>
        </section>

        <!-- 快捷键参考 -->
        <section id="shortcuts" class="help-section">
          <div class="section-header">
            <KeyboardIcon size="24" />
            <h2>快捷键参考</h2>
          </div>
          <div class="section-content">
            <div class="shortcut-table">
              <div v-for="sc in shortcuts" :key="sc.key" class="shortcut-row">
                <div class="shortcut-keys">
                  <kbd v-for="(k, i) in sc.keys" :key="i">{{ k }}</kbd>
                </div>
                <span class="shortcut-desc">{{ sc.desc }}</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 版本信息 -->
        <section id="version" class="help-section">
          <div class="section-header">
            <InfoCircleIcon size="24" />
            <h2>版本信息</h2>
          </div>
          <div class="section-content">
            <div class="version-grid">
              <div class="version-card">
                <div class="vc-badge current">当前版本</div>
                <div class="vc-name">LowCode Platform</div>
                <div class="vc-version">v1.0.0</div>
                <div class="vc-date">2026-06-24</div>
              </div>
            </div>
          </div>
        </section>

      </div>
    </div>

    <!-- 入门指南弹窗 -->
    <t-dialog
      v-model:visible="guideDialogVisible"
      :header="guideDialogTitle"
      width="680px"
      :footer="false"
    >
      <div class="guide-dialog-body">
        <p>{{ guideDialogContent }}</p>
      </div>
      <template #footer>
        <t-button theme="primary" @click="guideDialogVisible = false">我知道了</t-button>
      </template>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  HelpCircleIcon, ChevronRightIcon,
  BookOpenIcon, ChatIcon, LayoutIcon,
  HomeIcon,
  DataBaseIcon, StarIcon, InternetIcon,
  KeyboardIcon, InfoCircleIcon
} from 'tdesign-icons-vue-next';

const router = useRouter();
const activeSection = ref('getting-started');
const activeFaq = ref('faq-1');
const contentMainRef = ref<HTMLElement | null>(null);

// ======= 导航 =======
function goHome() {
  router.push('/home');
}

// ======= 入门指南弹窗 =======
const guideDialogVisible = ref(false);
const guideDialogTitle = ref('');
const guideDialogContent = ref('');

const guideDetails: Record<number, { title: string; content: string }> = {
  1: {
    title: '创建您的第一个页面',
    content: '要创建您的第一个页面，请先在左侧导航栏点击「页面管理」，然后点击「新建页面」按钮。您可以选择空白模板或预设模板开始。\n\n在页面设计器中，您可以从左侧组件库中拖拽所需的组件到画布上，包括按钮、表格、表单、图表等 20+ 种组件。拖拽到画布后，您可以在右侧属性面板中调整每个组件的样式、数据和交互行为。\n\n画布支持自由缩放和平移，您可以使用鼠标滚轮缩放视图，或按住空白区域拖动来平移画布。完成设计后点击「保存」即可保存您的页面。'
  },
  2: {
    title: '设计数据模型',
    content: '数据模型是您应用的核心。在左侧导航栏点击「数据建模」进入实体管理页面，点击「新建实体」开始创建您的第一个数据模型。\n\n为实体命名后，您可以添加各种类型的字段，包括：文本、数字、日期时间、布尔值、枚举、文件、关联关系等 10+ 种字段类型。每个字段都可以配置是否必填、默认值、验证规则等属性。\n\n您还可以为实体之间建立关联关系，如一对一、一对多、多对多。创建实体后，系统会自动为您生成完整的 CRUD RESTful API 接口，无需手动编写后端代码。'
  },
  3: {
    title: '预览与发布',
    content: '在页面设计器中，您可以随时点击顶部的「预览」按钮查看页面的实际效果。预览模式会以终端用户的视角展示您的页面，帮助您在发布前发现布局或交互问题。\n\n确认页面无误后，点击顶部导航栏的「发布」按钮。系统会提示您选择发布环境（测试环境或生产环境），并展示本次发布包含的页面和资源变更清单。\n\n点击「确认发布」后，系统将自动构建并部署您的应用到所选环境。发布过程通常只需几秒到一分钟。发布完成后，您将获得一个可访问的应用 URL，可以直接分享给终端用户使用。'
  }
};

function openGuide(index: number) {
  const guide = guideDetails[index];
  if (guide) {
    guideDialogTitle.value = guide.title;
    guideDialogContent.value = guide.content;
    guideDialogVisible.value = true;
  }
}

// ======= 滚动 =======
function scrollToSection(sectionId: string) {
  activeSection.value = sectionId;
  const element = document.getElementById(sectionId);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}

// ======= 快捷入口 =======
const quickLinks = [
  { id: 'getting-started', title: '快速入门', desc: '3分钟上手', icon: StarIcon, color: 'linear-gradient(135deg, #f5a623, #e8a317)', section: 'getting-started' },
  { id: 'faq', title: '常见问题', desc: '快速找到答案', icon: ChatIcon, color: 'linear-gradient(135deg, #e8a317, #d4920a)', section: 'faq' },
  { id: 'features', title: '功能说明', desc: '了解全部功能', icon: LayoutIcon, color: 'linear-gradient(135deg, #e8a317, #d4920a)', section: 'features' },
  { id: 'shortcuts', title: '快捷键', desc: '提升操作效率', icon: KeyboardIcon, color: 'linear-gradient(135deg, #f5a623, #d4920a)', section: 'shortcuts' },
];

// ======= 侧边栏 =======
const helpSections = [
  {
    id: 'start',
    title: '入门指南',
    icon: BookOpenIcon,
    links: [
      { id: 'getting-started', title: '快速入门' },
    ]
  },
  {
    id: 'help-content-section',
    title: '文档中心',
    icon: LayoutIcon,
    links: [
      { id: 'faq', title: '常见问题' },
      { id: 'features', title: '功能说明' },
      { id: 'shortcuts', title: '快捷键参考' },
    ]
  },
  {
    id: 'dev-section',
    title: '开发资源',
    icon: InfoCircleIcon,
    links: [
      { id: 'version', title: '版本信息' },
    ]
  },
];

// ======= 常见问题 =======
const faqs = [
  {
    id: 'faq-1',
    question: '如何创建一个新页面？',
    answer: '在左侧导航栏点击「页面管理」，然后点击「新建页面」按钮。选择模板或空白页面，输入页面名称即可创建。您可以使用可视化设计器拖拽组件来构建页面布局，自由调整位置和尺寸。'
  },
  {
    id: 'faq-2',
    question: '如何配置数据源？',
    answer: '进入「数据模型」管理页面，点击「新建实体」创建数据模型。支持连接多种数据源，包括 MySQL、PostgreSQL、SQLite 等关系型数据库，以及 MongoDB 等 NoSQL 数据库。创建实体后系统自动生成 CRUD 接口。'
  },
  {
    id: 'faq-3',
    question: '如何发布应用到生产环境？',
    answer: '在顶部导航栏点击「发布」按钮，选择发布环境（测试/生产），确认发布内容后点击「确认发布」。系统会自动构建并部署您的应用。发布前建议在预览中充分测试。'
  },
  {
    id: 'faq-4',
    question: '如何自定义主题色？',
    answer: '进入「系统设置」→「主题设置」，在主题色选项中选择预设颜色或输入自定义颜色值。主题色会自动应用到整个平台界面，包括按钮、导航栏、选中态等元素。'
  },
  {
    id: 'faq-5',
    question: '如何添加团队成员？',
    answer: '进入「管理后台」→「用户管理」，点击「添加用户」按钮，输入用户邮箱和角色权限即可邀请团队成员。支持管理员、开发者、观察者等多种角色，不同角色拥有不同的操作权限。'
  },
  {
    id: 'faq-6',
    question: '画布空间不够怎么办？',
    answer: '在页面设计器中，您可以在画布空白区域按住鼠标左键拖动来平移视图。也可以使用鼠标滚轮缩放画布，或使用快捷键 Ctrl+0 重置画布到默认大小。'
  },
  {
    id: 'faq-7',
    question: '如何将页面组件绑定到数据实体？',
    answer: '在设计器中选中表格或表单组件，在右侧属性面板的「数据源」中选择已创建的数据实体。选中实体后，组件会自动加载该实体的字段列表，无需手动配置每一列。'
  },
  {
    id: 'faq-8',
    question: '消息通知为什么有时不显示？',
    answer: '消息通知需要管理员在后台发布后才能在前端显示。如果您是普通用户，请联系管理员发布通知。已读状态会保存在浏览器本地存储中，清除缓存后历史通知将显示为未读。'
  },
];

// ======= 功能说明 =======
const features = [
  {
    id: 'feature-1',
    title: '可视化页面设计',
    desc: '拖拽式设计器，所见即所得',
    icon: LayoutIcon,
    color: 'linear-gradient(135deg, #f5a623, #e8a317)',
    items: ['丰富的组件库（20+ 组件）', '响应式布局自动适配', '实时预览 + 自由缩放画布']
  },
  {
    id: 'feature-2',
    title: '数据模型管理',
    desc: '可视化数据建模工具',
    icon: DataBaseIcon,
    color: 'linear-gradient(135deg, #e8a317, #d4920a)',
    items: ['实体关系可视化设计', '10+ 字段类型支持', '自动生成 CRUD 接口']
  },
  {
    id: 'feature-3',
    title: '自动化工作流',
    desc: '零代码配置业务流程',
    icon: StarIcon,
    color: 'linear-gradient(135deg, #f5a623, #d4920a)',
    items: ['可视化流程设计器', '灵活的触发器配置', '审批流程 + 消息通知']
  },
  {
    id: 'feature-4',
    title: '多端适配',
    desc: '一次开发，多端运行',
    icon: InternetIcon,
    color: 'linear-gradient(135deg, #e8a317, #f5a623)',
    items: ['PC 端完美适配', '移动端 H5 自适应', '微信小程序支持']
  },
];

// ======= 快捷键 =======
const shortcuts = [
  { keys: ['Ctrl', 'K'], desc: '打开命令面板，全局搜索页面/实体/功能' },
  { keys: ['Ctrl', 'S'], desc: '在页面设计器中快速保存当前页面' },
  { keys: ['Ctrl', '0'], desc: '重置画布缩放为 100%' },
  { keys: ['Ctrl', '滚轮'], desc: '在设计器画布中缩放视图' },
  { keys: ['鼠标拖动空白区'], desc: '在画布中平移视图（按住空白区域拖动）' },
  { keys: ['右键组件'], desc: '在画布中右键组件可快速复制、删除或置顶' },
  { keys: ['Delete'], desc: '删除选中的画布组件' },
  { keys: ['Ctrl', 'Z'], desc: '撤销上一步操作' },
];
</script>

<style scoped lang="less">
.help-center {
  min-height: 100vh;
  background: #f8f9fa;
}

/* 头部 */
.help-header {
  background: linear-gradient(135deg, #f5a623 0%, #e8a317 100%);
  padding: 50px 40px 40px;
  color: #fff;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(6px);
  flex-shrink: 0;

  &:hover {
    color: #fff;
    border-color: #fff;
    background: rgba(255, 255, 255, 0.25);
  }
}

.header-icon {
  width: 70px;
  height: 70px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-text h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
}

.header-text p {
  font-size: 15px;
  opacity: 0.9;
  margin: 0;
}

/* 快捷入口 */
.quick-links {
  display: flex;
  gap: 16px;
  padding: 0 40px;
  margin-top: -30px;
  position: relative;
  z-index: 1;
}

.quick-link-card {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
    border-color: #e8a317;
  }

  &.hidden { display: none; }
}

.ql-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.ql-content {
  flex: 1;
  min-width: 0;
}

.ql-title {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.ql-desc {
  font-size: 12px;
  color: #999;
}

/* 帮助内容 */
.help-content {
  display: flex;
  gap: 30px;
  padding: 40px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 侧边栏 */
.content-sidebar {
  width: 220px;
  flex-shrink: 0;
  position: sticky;
  top: 20px;
  height: fit-content;
}

.sidebar-section {
  margin-bottom: 20px;

  &.hidden { display: none; }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 10px;
}

.section-links {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-link {
  padding: 8px 12px;
  font-size: 13px;
  color: #555;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: #fef9ef;
    color: #c78a0e;
  }

  &.active {
    background: rgba(232, 163, 23, 0.1);
    color: #e8a317;
    font-weight: 500;
  }
}

/* 主内容区 */
.content-main {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.help-section {
  margin-bottom: 44px;

  &:last-child { margin-bottom: 0; }

  &.hidden { display: none; }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0;
  }
}

.section-content {
  color: #555;

  .section-intro {
    font-size: 14px;
    color: #666;
    margin: 0 0 18px;
  }
}

/* 入门指南 */
.guide-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.guide-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
  transition: all 0.2s;
  border: 1px solid transparent;
  cursor: pointer;

  &:hover {
    background: #fef9ef;
    border-color: #e8a317;
  }
}

.gc-step {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #f5a623, #e8a317);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.gc-content {
  flex: 1;

  h3 {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0 0 6px;
  }

  p {
    font-size: 13px;
    color: #777;
    margin: 0 0 10px;
  }
}

.gc-link {
  font-size: 13px;
  color: #e8a317;
  font-weight: 500;
}

/* FAQ */
:deep(.t-collapse__header) {
  padding: 16px 0 !important;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

:deep(.t-collapse__body) {
  padding: 0 0 16px !important;
  font-size: 14px;
  color: #666;
  line-height: 1.7;
}

:deep(.t-collapse__header-arrow) {
  color: #999;
}

:deep(.t-collapse-panel__header:hover .t-collapse__header) {
  color: #e8a317;
}

/* 功能说明 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.feature-card {
  padding: 24px;
  background: #fafafa;
  border-radius: 12px;
  border: 1px solid transparent;
  transition: all 0.2s;

  &:hover {
    background: #fef9ef;
    border-color: #e8a317;
  }
}

.feature-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 14px;
}

.feature-card h3 {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.feature-card p {
  font-size: 13px;
  color: #777;
  margin: 0 0 14px;
}

.feature-list {
  margin: 0;
  padding-left: 20px;
}

.feature-list li {
  font-size: 13px;
  color: #555;
  margin-bottom: 6px;

  &:last-child { margin-bottom: 0; }
}

/* 快捷键 */
.shortcut-table {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.shortcut-row {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.15s;

  &:last-child { border-bottom: none; }

  &:hover {
    background: #fef9ef;
  }
}

.shortcut-keys {
  width: 180px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;

  kbd {
    padding: 3px 8px;
    font-size: 12px;
    font-family: 'SF Mono', Monaco, Consolas, monospace;
    background: #f3f4f6;
    border: 1px solid #d1d5db;
    border-radius: 5px;
    color: #374151;
    white-space: nowrap;
  }
}

.shortcut-desc {
  font-size: 13px;
  color: #555;
}

/* 版本信息 */
.version-grid {
  display: flex;
  justify-content: center;
}

.version-card {
  padding: 24px;
  background: #fafafa;
  border-radius: 12px;
  text-align: center;

  .vc-badge {
    display: inline-block;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    background: #f0f0f0;
    color: #666;
    margin-bottom: 14px;

    &.current {
      background: linear-gradient(135deg, #f5a623, #e8a317);
      color: #fff;
    }
  }

  .vc-name {
    font-size: 14px;
    color: #555;
    margin-bottom: 6px;
  }

  .vc-version {
    font-size: 28px;
    font-weight: 700;
    color: #e8a317;
    margin-bottom: 4px;
  }

  .vc-date {
    font-size: 12px;
    color: #999;
  }
}

/* 弹窗内容 */
.guide-dialog-body {
  font-size: 14px;
  color: #444;
  line-height: 1.9;
  white-space: pre-line;
  max-height: 50vh;
  overflow-y: auto;
}

/* 响应式 */
@media (max-width: 768px) {
  .help-header {
    padding: 30px 20px 25px;
  }

  .quick-links {
    flex-direction: column;
    padding: 0 20px;
  }

  .help-content {
    flex-direction: column;
    padding: 20px;
  }

  .content-sidebar {
    width: 100%;
    position: static;
    margin-bottom: 20px;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }

  .shortcut-keys {
    width: 130px;
  }
}
</style>
