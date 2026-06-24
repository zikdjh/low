<template>
  <div class="form-element">
    <t-form ref="formRef" :data="formData" :label-width="element.props.labelWidth || 100" @submit="handleSubmit">
      <t-form-item
        v-for="field in fields"
        :key="field.code"
        :label="field.name"
        :name="field.code"
        :rules="field.required ? [{ required: true, message: '请填写' + field.name }] : []"
      >
        <!-- 文本 / 数字 -->
        <t-input
          v-if="isTextInput(field.fieldType)"
          v-model="formData[field.code]"
          :placeholder="'请输入' + field.name"
        />
        <!-- 多行文本 -->
        <t-textarea
          v-else-if="field.fieldType === 'TEXT'"
          v-model="formData[field.code]"
          :placeholder="'请输入' + field.name"
          :autosize="{ minRows: 3, maxRows: 6 }"
        />
        <!-- 日期 -->
        <t-date-picker
          v-else-if="field.fieldType === 'DATE'"
          v-model="formData[field.code]"
          :placeholder="'选择日期'"
        />
        <!-- 日期时间 -->
        <t-date-picker
          v-else-if="field.fieldType === 'DATETIME'"
          v-model="formData[field.code]"
          :placeholder="'选择日期时间'"
          enable-time-picker
        />
        <!-- 下拉选择 （根据 referenceEntityCode 加载关联数据） -->
        <t-select
          v-else-if="field.referenceEntityCode"
          v-model="formData[field.code]"
          :placeholder="'请选择' + field.name"
          filterable
        >
          <t-option
            v-for="opt in referenceOptions[field.code] || []"
            :key="opt.value"
            :value="opt.value"
            :label="opt.label"
          />
        </t-select>
        <!-- 审批状态等固定选项 -->
        <t-select
          v-else-if="hasStaticOptions(field)"
          v-model="formData[field.code]"
          :placeholder="'请选择' + field.name"
        >
          <t-option
            v-for="opt in getStaticOptions(field)"
            :key="opt.value"
            :value="opt.value"
            :label="opt.label"
          />
        </t-select>
        <!-- 开关（布尔值） -->
        <t-switch
          v-else-if="field.fieldType === 'BOOLEAN'"
          v-model="formData[field.code]"
        />
        <!-- 默认输入框 -->
        <t-input
          v-else
          v-model="formData[field.code]"
          :placeholder="'请输入' + field.name"
        />
      </t-form-item>

      <t-form-item>
        <t-space>
          <t-button theme="primary" type="submit" :loading="submitting">
            {{ submitText }}
          </t-button>
          <t-button theme="default" variant="outline" @click="handleReset">
            重置
          </t-button>
        </t-space>
      </t-form-item>
    </t-form>

    <!-- 提交结果提示 -->
    <div v-if="resultMsg" :class="['result-msg', resultType]">
      {{ resultMsg }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import dynamicDataApi from '../../../../api/lowcode/dynamicData';

const props = defineProps<{
  element: any;
  enableEvents?: boolean;
}>();

const route = useRoute();
const formRef = ref();
const formData = reactive<Record<string, any>>({});
const submitting = ref(false);
const resultMsg = ref('');
const resultType = ref('success');
const referenceOptions = reactive<Record<string, { value: any; label: string }[]>>({});

const fields = computed(() => {
  return (props.element.props?.fields || []) as Array<{
    code: string;
    name: string;
    fieldType: string;
    required: boolean;
    defaultValue?: string;
    referenceEntityCode?: string;
  }>;
});

const submitText = computed(() => {
  const editId = route.query?.editId;
  return editId ? '更新' : '提交';
});

// 静态选项映射
const staticOptionsMap: Record<string, { value: string; label: string }[]> = {
  gender: [
    { value: 'male', label: '男' },
    { value: 'female', label: '女' },
  ],
  approver_type: [
    { value: 'counselor', label: '辅导员' },
    { value: 'dept_head', label: '系主任' },
  ],
  leave_type: [
    { value: 'sick', label: '病假' },
    { value: 'personal', label: '事假' },
    { value: 'other', label: '其他' },
  ],
  status: [
    { value: 'pending_counselor', label: '待辅导员审核' },
    { value: 'pending_dean', label: '待系主任审核' },
    { value: 'approved', label: '审核通过' },
    { value: 'rejected', label: '已驳回' },
    { value: 'cancelled', label: '已撤回' },
  ],
};

function isTextInput(fieldType: string): boolean {
  return ['VARCHAR', 'INTEGER', 'LONG', 'DOUBLE', 'DECIMAL'].includes(fieldType);
}

function hasStaticOptions(field: any): boolean {
  return !!staticOptionsMap[field.code];
}

function getStaticOptions(field: any) {
  return staticOptionsMap[field.code] || [];
}

// 加载关联实体的下拉选项
async function loadReferenceOptions(field: any) {
  if (!field.referenceEntityCode) return;
  try {
    const res = await dynamicDataApi.list(field.referenceEntityCode, { page: 1, pageSize: 200 });
    if (res.data?.code !== 1) return;
    const data = res.data?.data;
    const rows = data?.content || data?.records || [];
    const displayField = field.referenceDisplayFieldCode || 'name';
    referenceOptions[field.code] = rows.map((row: any) => ({
      value: row.id,
      label: row[displayField] || row.name || row.code || String(row.id),
    }));
  } catch {
    // 忽略加载失败
  }
}

// 加载编辑数据
async function loadEditData() {
  const editId = route.query?.editId;
  const entityCode = props.element.props?.entityCode;
  if (!editId || !entityCode) return;
  try {
    const res = await dynamicDataApi.getById(entityCode, String(editId));
    if (res.data?.code === 1 && res.data?.data) {
      Object.assign(formData, res.data.data);
    }
  } catch {
    // 忽略
  }
}

// 初始化默认值
function initDefaults() {
  for (const field of fields.value) {
    if (field.defaultValue && !(field.code in formData)) {
      formData[field.code] = field.defaultValue;
    }
  }
}

async function handleSubmit() {
  resultMsg.value = '';
  submitting.value = true;
  try {
    const entityCode = props.element.props?.entityCode;
    const submitApi = props.element.props?.submitApi; // 自定义提交API
    const submitMethod = props.element.props?.submitMethod || 'post'; // 默认POST

    const payload: Record<string, any> = {};
    for (const field of fields.value) {
      if (formData[field.code] !== undefined) {
        payload[field.code] = formData[field.code];
      }
    }

    const editId = route.query?.editId;
    
    if (submitApi) {
      // 使用自定义提交API
      const axios = (await import('../../../../api/index')).default;
      const method = submitMethod.toLowerCase();
      let res;
      if (method === 'post') {
        res = await axios.post(submitApi, payload);
      } else if (method === 'put') {
        res = await axios.put(submitApi, payload);
      } else {
        res = await axios.post(submitApi, payload);
      }
      resultType.value = res.data?.code === 1 || res.data?.code === 0 ? 'success' : 'error';
      resultMsg.value = res.data?.message || res.data?.msg || '提交成功';
    } else if (entityCode) {
      if (editId) {
        await dynamicDataApi.update(entityCode, String(editId), payload);
      } else {
        await dynamicDataApi.create(entityCode, payload);
      }
      resultType.value = 'success';
      resultMsg.value = editId ? '更新成功' : '提交成功';
    } else {
      return;
    }

    // 新增成功后重置表单
    if (!editId && resultType.value === 'success') {
      handleReset();
    }
  } catch (e: any) {
    resultType.value = 'error';
    resultMsg.value = '操作失败: ' + (e?.response?.data?.msg || e?.message || '未知错误');
  } finally {
    submitting.value = false;
  }
}

function handleReset() {
  formRef.value?.reset();
  initDefaults();
}

onMounted(async () => {
  initDefaults();
  await loadEditData();
  // 加载所有引用字段的选项
  for (const field of fields.value) {
    if (field.referenceEntityCode) {
      await loadReferenceOptions(field);
    }
  }
});
</script>

<style scoped lang="less">
.form-element {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.result-msg {
  margin-top: 12px;
  padding: 10px 16px;
  border-radius: 6px;
  font-size: 14px;
  &.success {
    background: #f0faf4;
    color: #2ba471;
    border: 1px solid #b7ebd0;
  }
  &.error {
    background: #fef0f0;
    color: #d54941;
    border: 1px solid #fbb8b4;
  }
}
</style>
