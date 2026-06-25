<template>
  <div class="codegen-page">
    <t-card :bordered="false">
      <template #title>
        <t-space align="center">
          <code-icon size="20px" />
          <span>代码生成器</span>
          <t-tag v-if="preview" theme="primary" variant="light">{{ preview.entityName }} / {{ preview.className }}</t-tag>
        </t-space>
      </template>
      <template #actions>
        <t-space>
          <t-button theme="default" @click="goBack">返回</t-button>
          <t-button theme="primary" :loading="downloading" :disabled="!preview" @click="handleDownload">
            <template #icon><download-icon /></template>
            下载 ZIP
          </t-button>
          <t-popconfirm
            theme="warning"
            content="将把生成的代码直接写入项目源码目录。如果文件已存在会自动跳过，确认继续吗？"
            @confirm="() => handleInstall(false)"
          >
            <t-button theme="success" :loading="installing" :disabled="!preview">
              <template #icon><rocket-icon /></template>
              安装到项目
            </t-button>
          </t-popconfirm>
        </t-space>
      </template>

      <t-loading :loading="loading" text="正在生成代码…">
        <t-alert
          v-if="error"
          theme="error"
          :message="error"
          :close-btn="true"
          style="margin-bottom: 12px"
          @close="error = ''"
        />

        <div v-if="preview" class="codegen-body">
          <div class="codegen-sidebar">
            <div class="sidebar-title">生成文件 ({{ fileEntries.length }})</div>
            <div class="file-list">
              <div
                v-for="entry in fileEntries"
                :key="entry.path"
                :class="['file-item', { active: entry.path === activePath }]"
                @click="activePath = entry.path"
              >
                <file-icon size="14px" />
                <span class="file-name">{{ entry.shortName }}</span>
                <span class="file-dir">{{ entry.dir }}</span>
              </div>
            </div>
          </div>

          <div class="codegen-content">
            <div class="content-header">
              <span class="path-label">{{ activePath }}</span>
              <t-button size="small" variant="text" theme="primary" @click="copyActive">
                <template #icon><copy-icon /></template>
                复制
              </t-button>
            </div>
            <pre class="code-block"><code>{{ activeContent }}</code></pre>
          </div>
        </div>

        <t-empty v-else-if="!loading && !error" description="请通过实体列表的'生成代码'按钮进入" />
      </t-loading>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { MessagePlugin } from 'tdesign-vue-next';
import { CodeIcon, DownloadIcon, FileIcon, CopyIcon, RocketIcon } from 'tdesign-icons-vue-next';
import codeGenApi, { type CodeGenPreview } from '@/api/lowcode/codegen';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const downloading = ref(false);
const installing = ref(false);
const error = ref('');
const preview = ref<CodeGenPreview | null>(null);
const activePath = ref('');

const entityId = computed(() => Number(route.params.id));

const fileEntries = computed(() => {
  if (!preview.value) return [] as { path: string; shortName: string; dir: string }[];
  return Object.keys(preview.value.files).map(path => {
    const idx = path.lastIndexOf('/');
    return {
      path,
      shortName: idx >= 0 ? path.slice(idx + 1) : path,
      dir: idx >= 0 ? path.slice(0, idx) : '',
    };
  });
});

const activeContent = computed(() => {
  if (!preview.value || !activePath.value) return '';
  return preview.value.files[activePath.value] || '';
});

async function loadPreview() {
  if (!entityId.value || Number.isNaN(entityId.value)) {
    error.value = '缺少实体 ID';
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    const res: any = await codeGenApi.preview(entityId.value);
    if (res?.data?.code === 1) {
      preview.value = res.data.data as CodeGenPreview;
      const first = Object.keys(preview.value.files)[0];
      if (first) activePath.value = first;
    } else {
      error.value = res?.data?.msg || '生成失败';
    }
  } catch (e: any) {
    error.value = e?.message || '请求失败';
  } finally {
    loading.value = false;
  }
}

async function handleDownload() {
  if (!entityId.value) return;
  downloading.value = true;
  try {
    const res: any = await codeGenApi.download(entityId.value);
    const blob = res.data instanceof Blob ? res.data : new Blob([res.data]);
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${preview.value?.className || 'codegen'}-codegen.zip`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
    MessagePlugin.success('已下载');
  } catch (e: any) {
    MessagePlugin.error(e?.message || '下载失败');
  } finally {
    downloading.value = false;
  }
}

async function handleInstall(force: boolean) {
  if (!entityId.value) return;
  installing.value = true;
  try {
    const res: any = await codeGenApi.install(entityId.value, force);
    if (res?.data?.code !== 1) {
      MessagePlugin.error(res?.data?.msg || '安装失败');
      return;
    }
    const r = res.data.data as { written: string[]; skipped: string[]; note: string };
    if (r.skipped?.length) {
      // 有跳过的文件 —— 提供强制覆盖入口
      const ok = window.confirm(
        `已写入 ${r.written.length} 个文件，${r.skipped.length} 个文件已存在被跳过：\n` +
          r.skipped.join('\n') +
          '\n\n是否强制覆盖这些文件？'
      );
      if (ok) {
        await handleInstall(true);
        return;
      }
      MessagePlugin.warning(r.note);
    } else {
      MessagePlugin.success(r.note);
    }
  } catch (e: any) {
    MessagePlugin.error(e?.response?.data?.msg || e?.message || '安装失败');
  } finally {
    installing.value = false;
  }
}

async function copyActive() {
  if (!activeContent.value) return;
  try {
    await navigator.clipboard.writeText(activeContent.value);
    MessagePlugin.success('已复制');
  } catch {
    MessagePlugin.warning('复制失败，请手动选择');
  }
}

function goBack() {
  router.back();
}

onMounted(loadPreview);
</script>

<style scoped>
.codegen-page {
  padding: 16px;
}

.codegen-body {
  display: flex;
  gap: 16px;
  min-height: 540px;
}

.codegen-sidebar {
  width: 320px;
  flex-shrink: 0;
  border: 1px solid var(--td-component-border, #e7e7e7);
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  padding: 10px 12px;
  background: var(--td-bg-color-secondarycontainer, #f5f5f5);
  font-weight: 500;
  border-bottom: 1px solid var(--td-component-border, #e7e7e7);
}

.file-list {
  flex: 1;
  overflow-y: auto;
  max-height: 540px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid var(--td-component-stroke, #f0f0f0);
  transition: background-color 0.15s;
}

.file-item:hover {
  background: var(--td-bg-color-container-hover, #f5f7fa);
}

.file-item.active {
  background: var(--td-brand-color-light, #ecf2fe);
  color: var(--td-brand-color, #0052d9);
}

.file-name {
  font-weight: 500;
  flex-shrink: 0;
}

.file-dir {
  font-size: 12px;
  color: var(--td-text-color-placeholder, #999);
  margin-left: auto;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
  direction: rtl;
  text-align: left;
}

.codegen-content {
  flex: 1;
  min-width: 0;
  border: 1px solid var(--td-component-border, #e7e7e7);
  border-radius: 6px;
  display: flex;
  flex-direction: column;
}

.content-header {
  padding: 8px 12px;
  background: var(--td-bg-color-secondarycontainer, #f5f5f5);
  border-bottom: 1px solid var(--td-component-border, #e7e7e7);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.path-label {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
  color: var(--td-text-color-secondary, #666);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.code-block {
  flex: 1;
  margin: 0;
  padding: 12px 16px;
  background: #1e1e1e;
  color: #d4d4d4;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
  line-height: 1.55;
  overflow: auto;
  max-height: 540px;
  white-space: pre;
}
</style>
