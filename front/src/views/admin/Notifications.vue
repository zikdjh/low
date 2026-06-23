<template>
  <div class="notification-management">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">通知管理</h2>
        <p class="page-subtitle">管理系统通知消息</p>
      </div>
      <div class="header-right">
        <t-button theme="primary" @click="showAddModal = true">
          <template #icon><PlusIcon /></template>
          发布通知
        </t-button>
      </div>
    </div>

    <div class="table-container">
      <t-table
        :data="tableData"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        stripe
        show-header-overflow
        show-row-overflow
        @page-change="onPageChange"
      >
        <template #type="{ row }">
          <t-tag :theme="getTypeTheme(row.type)" variant="light">
            {{ getTypeText(row.type) }}
          </t-tag>
        </template>

        <template #status="{ row }">
          <t-tag :theme="row.isPublished ? 'success' : 'warning'" variant="light">
            {{ row.isPublished ? '已发布' : '草稿' }}
          </t-tag>
        </template>

        <template #actions="{ row }">
          <t-button
            variant="text"
            size="small"
            @click="viewNotification(row)"
          >
            <template #icon><SearchIcon /></template>
            查看
          </t-button>
          <t-button
            variant="text"
            size="small"
            @click="editNotification(row)"
          >
            <template #icon><EditIcon /></template>
            编辑
          </t-button>
          <t-button
            v-if="!row.isPublished"
            variant="text"
            size="small"
            theme="primary"
            @click="handlePublish(row.id)"
          >
            <template #icon><SendIcon /></template>
            发布
          </t-button>
          <t-button
            v-if="row.isPublished"
            variant="text"
            size="small"
            theme="warning"
            @click="handleUnpublish(row.id)"
          >
            <template #icon><PauseIcon /></template>
            下架
          </t-button>
          <t-button
            variant="text"
            size="small"
            theme="danger"
            @click="handleDelete(row.id)"
          >
            <template #icon><DeleteIcon /></template>
            删除
          </t-button>
        </template>

        <template #empty>
          <div class="empty-content">
            <NotificationIcon size="48" />
            <p>暂无通知</p>
            <t-button theme="primary" @click="showAddModal = true">发布第一条通知</t-button>
          </div>
        </template>
      </t-table>
    </div>

    <t-dialog
      v-model:visible="showAddModal"
      :header="isEditing ? '编辑通知' : '发布通知'"
      :width="600"
      @close="resetForm"
      @confirm="handleSubmit"
    >
      <div class="form-container">
        <t-form :model="formData" :rules="formRules" ref="formRef">
          <t-form-item label="标题" name="title">
            <t-input
              v-model="formData.title"
              placeholder="请输入通知标题"
              :maxlength="200"
            />
          </t-form-item>

          <t-form-item label="类型" name="type">
            <t-select v-model="formData.type" placeholder="请选择通知类型">
              <t-option value="info" label="信息" />
              <t-option value="warning" label="警告" />
              <t-option value="success" label="成功" />
              <t-option value="error" label="错误" />
            </t-select>
          </t-form-item>

          <t-form-item label="内容" name="content">
            <t-textarea
              v-model="formData.content"
              placeholder="请输入通知内容"
              :maxlength="2000"
              :rows="6"
            />
          </t-form-item>

          <t-form-item label="目标用户">
            <t-input
              v-model="formData.targetUsers"
              placeholder="留空表示全体用户，多个用户ID用逗号分隔"
            />
          </t-form-item>
        </t-form>
      </div>
    </t-dialog>

    <t-dialog
      v-model:visible="showViewModal"
      title="通知详情"
      :width="600"
    >
      <div v-if="viewData" class="view-container">
        <div class="view-row">
          <span class="view-label">标题</span>
          <span class="view-value">{{ viewData.title }}</span>
        </div>
        <div class="view-row">
          <span class="view-label">类型</span>
          <t-tag :theme="getTypeTheme(viewData.type)" variant="light">
            {{ getTypeText(viewData.type) }}
          </t-tag>
        </div>
        <div class="view-row">
          <span class="view-label">状态</span>
          <t-tag :theme="viewData.isPublished ? 'success' : 'warning'" variant="light">
            {{ viewData.isPublished ? '已发布' : '草稿' }}
          </t-tag>
        </div>
        <div class="view-row">
          <span class="view-label">内容</span>
          <div class="view-content">{{ viewData.content }}</div>
        </div>
        <div class="view-row">
          <span class="view-label">创建时间</span>
          <span class="view-value">{{ formatDate(viewData.createdAt) }}</span>
        </div>
        <div v-if="viewData.publishedAt" class="view-row">
          <span class="view-label">发布时间</span>
          <span class="view-value">{{ formatDate(viewData.publishedAt) }}</span>
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import {
  PlusIcon,
  SearchIcon,
  EditIcon,
  SendIcon,
  PauseIcon,
  DeleteIcon,
  NotificationIcon
} from 'tdesign-icons-vue-next'
import {
  notificationApi,
  type NotificationRequest,
  type NotificationResponse,
  type PageResponse
} from '@/api/notification'

const loading = ref(false)
const showAddModal = ref(false)
const showViewModal = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()

const formData = reactive<NotificationRequest>({
  title: '',
  content: '',
  type: 'info',
  targetUsers: ''
})

const viewData = ref<NotificationResponse | null>(null)

const formRules = {
  title: [
    { required: true, message: '请输入通知标题', trigger: ['blur', 'change'] },
    { max: 200, message: '标题不能超过200个字符', trigger: ['blur', 'change'] }
  ],
  content: [
    { max: 2000, message: '内容不能超过2000个字符', trigger: ['blur', 'change'] }
  ]
}

const pagination = reactive({
  current: 1,
  total: 0,
  pageSize: 10
})

const tableData = ref<NotificationResponse[]>([])

const columns = [
  {
    title: '标题',
    key: 'title',
    ellipsis: true
  },
  {
    title: '类型',
    key: 'type',
    width: 100,
    cell: 'type'
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    cell: 'status'
  },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    formatter: (row: NotificationResponse) => formatDate(row.createdAt)
  },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    cell: 'actions'
  }
]

function getTypeTheme(type: string) {
  const themes: Record<string, string> = {
    info: 'default',
    warning: 'warning',
    success: 'success',
    error: 'danger'
  }
  return themes[type] || 'default'
}

function getTypeText(type: string) {
  const texts: Record<string, string> = {
    info: '信息',
    warning: '警告',
    success: '成功',
    error: '错误'
  }
  return texts[type] || '信息'
}

function formatDate(dateStr: string | null) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

function resetForm() {
  formData.title = ''
  formData.content = ''
  formData.type = 'info'
  formData.targetUsers = ''
  isEditing.value = false
  editingId.value = null
  formRef.value?.reset()
}

async function loadData(page: number = 0) {
  loading.value = true
  try {
    const response = await notificationApi.getAllNotifications(page, pagination.pageSize)
    tableData.value = response.data.content
    pagination.total = response.data.totalElements
    pagination.current = page + 1
  } catch (error) {
    console.error('加载通知列表失败:', error)
  } finally {
    loading.value = false
  }
}

function onPageChange(page: number) {
  loadData(page - 1)
}

function viewNotification(row: NotificationResponse) {
  viewData.value = row
  showViewModal.value = true
}

function editNotification(row: NotificationResponse) {
  formData.title = row.title
  formData.content = row.content
  formData.type = row.type
  formData.targetUsers = row.targetUsers || ''
  isEditing.value = true
  editingId.value = row.id
  showAddModal.value = true
}

async function handleSubmit() {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) return
    
    if (isEditing.value && editingId.value) {
      await notificationApi.updateNotification(editingId.value, formData)
    } else {
      await notificationApi.createNotification(formData)
    }
    showAddModal.value = false
    resetForm()
    loadData()
  } catch (error) {
    console.error('提交通知失败:', error)
  }
}

async function handlePublish(id: number) {
  try {
    await notificationApi.publishNotification(id)
    loadData()
  } catch (error) {
    console.error('发布通知失败:', error)
  }
}

async function handleUnpublish(id: number) {
  try {
    await notificationApi.unpublishNotification(id)
    loadData()
  } catch (error) {
    console.error('下架通知失败:', error)
  }
}

async function handleDelete(id: number) {
  if (!confirm('确定要删除这条通知吗？')) return
  try {
    await notificationApi.deleteNotification(id)
    loadData()
  } catch (error) {
    console.error('删除通知失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.notification-management {
  min-height: calc(100vh - 64px);
  background: #f7f8fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;

  .header-left {
    .page-title {
      font-size: 24px;
      font-weight: 700;
      color: #1a1a1a;
      margin: 0 0 8px 0;
    }
    .page-subtitle {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }

  .header-right {
    .t-button {
      padding: 10px 20px;
    }
  }
}

.table-container {
  padding: 24px;
}

.form-container {
  padding: 16px 0;
}

.view-container {
  padding: 16px 0;
}

.view-row {
  display: flex;
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
}

.view-label {
  width: 100px;
  font-weight: 500;
  color: #666;
}

.view-value {
  flex: 1;
  color: #333;
}

.view-content {
  flex: 1;
  color: #333;
  white-space: pre-wrap;
  word-break: break-all;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  color: #999;

  .t-icon {
    margin-bottom: 16px;
    color: #ccc;
  }

  p {
    margin-bottom: 16px;
  }
}
</style>
