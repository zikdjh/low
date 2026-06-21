<template>
  <div class="help-center">
    <!-- 页面头部 -->
    <div class="help-header">
      <div class="header-content">
        <div class="header-icon">
          <HelpCircleIcon size="48" />
        </div>
        <div class="header-text">
          <h1>帮助中心</h1>
          <p>查找常见问题解答和使用指南</p>
        </div>
      </div>
      <div class="search-box">
        <SearchIcon size="18" class="search-icon" />
        <input
          v-model="searchQuery"
          type="text"
          class="search-input"
          placeholder="搜索帮助文档..."
          @input="handleSearch"
        />
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

      <div class="content-main">
        <!-- 入门指南 -->
        <section id="getting-started" class="help-section">
          <div class="section-header">
            <BookOpenIcon size="24" />
            <h2>入门指南</h2>
          </div>
          <div class="section-content">
            <div class="guide-cards">
              <div class="guide-card">
                <div class="gc-step">01</div>
                <div class="gc-content">
                  <h3>创建您的第一个页面</h3>
                  <p>了解如何使用低代码平台创建您的第一个页面应用。</p>
                  <a href="#" class="gc-link">开始学习 →</a>
                </div>
              </div>
              <div class="guide-card">
                <div class="gc-step">02</div>
                <div class="gc-content">
                  <h3>设计数据模型</h3>
                  <p>学习如何设计和管理应用的数据实体和关系。</p>
                  <a href="#" class="gc-link">开始学习 →</a>
                </div>
              </div>
              <div class="guide-card">
                <div class="gc-step">03</div>
                <div class="gc-content">
                  <h3>配置业务流程</h3>
                  <p>掌握如何配置自动化业务流程和工作流。</p>
                  <a href="#" class="gc-link">开始学习 →</a>
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

        <!-- API文档 -->
        <section id="api" class="help-section">
          <div class="section-header">
            <CodeIcon size="24" />
            <h2>API 文档</h2>
          </div>
          <div class="section-content">
            <div class="api-intro">
              <p>我们提供完整的 RESTful API，方便您与其他系统集成。</p>
              <div class="api-actions">
                <t-button variant="outline">查看 API 参考</t-button>
                <t-button variant="outline">下载 SDK</t-button>
              </div>
            </div>
            <div class="api-example">
              <h4>快速示例</h4>
              <pre class="api-code"><code>curl -X GET "https://api.example.com/v1/users" \
  -H "Authorization: Bearer YOUR_TOKEN"</code></pre>
            </div>
          </div>
        </section>

        <!-- 联系支持 -->
        <section id="support" class="help-section">
          <div class="section-header">
            <EarphoneIcon size="24" />
            <h2>联系支持</h2>
          </div>
          <div class="section-content">
            <div class="support-options">
              <div class="support-option">
                <div class="so-icon">
                  <ChatIcon size="24" />
                </div>
                <div>
                  <h3>在线客服</h3>
                  <p>工作日 9:00 - 18:00 在线</p>
                  <t-button theme="primary">立即咨询</t-button>
                </div>
              </div>
              <div class="support-option">
                <div class="so-icon">
                  <MailIcon size="24" />
                </div>
                <div>
                  <h3>邮件支持</h3>
                  <p>support@example.com</p>
                  <t-button variant="outline">发送邮件</t-button>
                </div>
              </div>
              <div class="support-option">
                <div class="so-icon">
                  <CallIcon size="24" />
                </div>
                <div>
                  <h3>电话支持</h3>
                  <p>400-888-8888</p>
                  <t-button variant="outline">拨打电话</t-button>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import {
  HelpCircleIcon, SearchIcon, ChevronRightIcon,
  BookOpenIcon, ChatIcon, LayoutIcon,
  CodeIcon, EarphoneIcon, MailIcon,
  DataBaseIcon, StarIcon, InternetIcon, CallIcon
} from 'tdesign-icons-vue-next';

const searchQuery = ref('');
const activeSection = ref('getting-started');
const activeFaq = ref('faq-1');

// 快捷入口
const quickLinks = [
  { id: 'getting-started', title: '快速入门', desc: '3分钟上手', icon: StarIcon, color: 'linear-gradient(135deg, #f5a623, #e8a317)', section: 'getting-started' },
  { id: 'faq', title: '常见问题', desc: '快速找到答案', icon: ChatIcon, color: 'linear-gradient(135deg, #1677ff, #69b1ff)', section: 'faq' },
  { id: 'features', title: '功能说明', desc: '了解全部功能', icon: LayoutIcon, color: 'linear-gradient(135deg, #52c41a, #73d13d)', section: 'features' },
  { id: 'support', title: '联系支持', desc: '获取帮助', icon: EarphoneIcon, color: 'linear-gradient(135deg, #eb2f96, #ff69b4)', section: 'support' },
];

// 侧边栏导航
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
    id: 'faq-section',
    title: '常见问题',
    icon: ChatIcon,
    links: [
      { id: 'faq', title: 'FAQ' },
    ]
  },
  {
    id: 'feature-section',
    title: '功能说明',
    icon: LayoutIcon,
    links: [
      { id: 'features', title: '功能介绍' },
    ]
  },
  {
    id: 'api-section',
    title: '开发资源',
    icon: CodeIcon,
    links: [
      { id: 'api', title: 'API 文档' },
    ]
  },
  {
    id: 'support-section',
    title: '支持服务',
    icon: EarphoneIcon,
    links: [
      { id: 'support', title: '联系我们' },
    ]
  },
];

// 常见问题
const faqs = [
  {
    id: 'faq-1',
    question: '如何创建一个新页面？',
    answer: '在左侧导航栏点击「页面管理」，然后点击「新建页面」按钮。选择模板或空白页面，输入页面名称即可创建。您可以使用可视化设计器拖拽组件来构建页面布局。'
  },
  {
    id: 'faq-2',
    question: '如何配置数据源？',
    answer: '进入「数据模型」管理页面，点击「新建实体」创建数据模型。支持连接多种数据源，包括 MySQL、PostgreSQL、SQLite 等关系型数据库，以及 MongoDB 等 NoSQL 数据库。'
  },
  {
    id: 'faq-3',
    question: '如何发布应用到生产环境？',
    answer: '在顶部导航栏点击「发布」按钮，选择发布环境（测试/生产），确认发布内容后点击「确认发布」。系统会自动构建并部署您的应用。'
  },
  {
    id: 'faq-4',
    question: '如何自定义主题色？',
    answer: '进入「系统设置」→「主题设置」，在主题色选项中选择预设颜色或点击「自定义颜色」选择您喜欢的颜色。主题色会自动应用到整个平台界面。'
  },
  {
    id: 'faq-5',
    question: '如何添加团队成员？',
    answer: '进入「管理后台」→「用户管理」，点击「添加用户」按钮，输入用户邮箱和角色权限即可邀请团队成员。支持管理员、开发者、观察者等多种角色。'
  },
];

// 功能说明
const features = [
  {
    id: 'feature-1',
    title: '可视化页面设计',
    desc: '拖拽式设计器，所见即所得',
    icon: LayoutIcon,
    color: 'linear-gradient(135deg, #1677ff, #69b1ff)',
    items: ['丰富的组件库', '响应式布局', '实时预览']
  },
  {
    id: 'feature-2',
    title: '数据模型管理',
    desc: '可视化数据建模工具',
    icon: DataBaseIcon,
    color: 'linear-gradient(135deg, #52c41a, #73d13d)',
    items: ['实体关系设计', '字段类型丰富', '数据验证规则']
  },
  {
    id: 'feature-3',
    title: '自动化工作流',
    desc: '零代码配置业务流程',
    icon: StarIcon,
    color: 'linear-gradient(135deg, #f5a623, #e8a317)',
    items: ['流程设计器', '触发器配置', '审批流程']
  },
  {
    id: 'feature-4',
    title: '多端适配',
    desc: '一次开发，多端运行',
    icon: InternetIcon,
    color: 'linear-gradient(135deg, #eb2f96, #ff69b4)',
    items: ['PC端', '移动端', '小程序']
  },
];

function handleSearch() {
  // 搜索功能可后续扩展
}

function scrollToSection(sectionId: string) {
  activeSection.value = sectionId;
  const element = document.getElementById(sectionId);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}
</script>

<style scoped lang="less">
.help-center {
  min-height: 100vh;
  background: #f8f9fa;
}

/* 头部 */
.help-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 50px 40px 40px;
  color: #fff;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
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

.search-box {
  max-width: 500px;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 10px;
  padding: 10px 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.search-icon {
  color: #999;
  margin-right: 10px;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #333;
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
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  }
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
    background: #f3f4f6;
    color: #333;
  }
  &.active {
    background: rgba(102, 126, 234, 0.1);
    color: #667eea;
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
  margin-bottom: 40px;
  &:last-child {
    margin-bottom: 0;
  }
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
}

/* 入门指南卡片 */
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
  &:hover {
    background: #f3f4f6;
  }
}

.gc-step {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea, #764ba2);
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
}

.gc-content h3 {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 6px;
}

.gc-content p {
  font-size: 13px;
  color: #777;
  margin: 0 0 10px;
}

.gc-link {
  font-size: 13px;
  color: #667eea;
  font-weight: 500;
  text-decoration: none;
  &:hover {
    text-decoration: underline;
  }
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
  &:last-child {
    margin-bottom: 0;
  }
}

/* API文档 */
.api-intro {
  padding: 24px;
  background: #f6f8ff;
  border-radius: 12px;
  margin-bottom: 20px;
  p {
    font-size: 14px;
    color: #555;
    margin: 0 0 16px;
  }
}

.api-actions {
  display: flex;
  gap: 10px;
}

.api-example {
  background: #1a1a1a;
  border-radius: 12px;
  padding: 20px;
}

.api-example h4 {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 14px;
}

.api-code {
  margin: 0;
  font-size: 13px;
  color: #a0aec0;
  overflow-x: auto;
}

/* 联系支持 */
.support-options {
  display: flex;
  gap: 20px;
}

.support-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px;
  background: #fafafa;
  border-radius: 12px;
  text-align: center;
}

.so-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 14px;
}

.support-option h3 {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 6px;
}

.support-option p {
  font-size: 13px;
  color: #777;
  margin: 0 0 14px;
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
  .support-options {
    flex-direction: column;
  }
}
</style>