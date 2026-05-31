<template>
  <t-dialog v-model:visible="visible" width="800px" :z-index="3000">
    <div class="cropper-container">
      <!-- 裁剪比例选择器 -->
      <div class="ratio-selector">
        <div class="ratio-title">裁剪比例：</div>
        <div class="ratio-options">
          <t-button
              v-for="ratio in activeRatios"
              :key="ratio.label"
              :variant="selectedRatio === ratio.value ? 'base' : 'outline'"
              size="small"
              @click="selectRatio(ratio.value, ratio.label)"
              class="ratio-btn"
          >
            {{ ratio.label }}
          </t-button>
          <t-button
              v-if="showCustomRatioButton"
              :variant="isCustomRatio ? 'base' : 'outline'"
              size="small"
              @click="toggleCustomRatio"
              class="ratio-btn"
          >
            自定义
          </t-button>
        </div>

        <!-- 自定义比例输入 -->
        <div v-if="isCustomRatio" class="custom-ratio">
          <t-input
              v-model="customWidth"
              placeholder="宽"
              size="small"
              style="width: 60px"
              @input="updateCustomRatio"
          />
          <span class="ratio-separator">:</span>
          <t-input
              v-model="customHeight"
              placeholder="高"
              size="small"
              style="width: 60px"
              @input="updateCustomRatio"
          />
        </div>
      </div>

      <!-- 裁剪器 -->
      <vue-cropper
          ref="cropperRef"
          :img="cropperImage"
          :info="true"
          :autoCrop="true"
          :fixed="isFixedRatio"
          :fixedNumber="currentFixedNumber"
          :fixedBox="isFixedRatio"
          :centerBox="true"
          @realTime="cropperPreview"
          style="width: 100%; height: 360px"
          class="cropper-main"/>
    </div>

    <template #footer>
      <t-button @click="cancelUpload">取消</t-button>
      <t-button @click="confirmCrop">确定</t-button>
    </template>
  </t-dialog>
</template>

<script setup lang="ts">
/* script setup */
import {VueCropper} from 'vue-cropper';
import 'vue-cropper/dist/index.css';
import {ref, computed, watch, nextTick} from 'vue';
import {storeToRefs} from 'pinia';
import {getCropperStore} from '../../store';

const cropperRef = ref<any>(null);
const cropper = getCropperStore();
const {ratios} = storeToRefs(cropper);

// 默认裁剪比例
const defaultRatios = [
  {label: '自由', value: 'free'},
  {label: '1:1', value: [1, 1]},
  {label: '3:4', value: [3, 4]},
  {label: '4:3', value: [4, 3]},
  {label: '16:9', value: [16, 9]},
  {label: '9:16', value: [9, 16]},
  {label: '2:3', value: [2, 3]},
  {label: '3:2', value: [3, 2]}
];

// 当前激活的裁剪比例
const activeRatios = computed(() => {
  return ratios.value && ratios.value.length > 0 ? ratios.value : defaultRatios;
});

// 是否显示自定义比例按钮
const showCustomRatioButton = computed(() => {
  return !ratios.value || ratios.value.length === 0;
});

// 根据传入的 ratios 初始化 selectedRatio
const getDefaultRatio = () => {
  if (ratios.value && ratios.value.length > 0) {
    return ratios.value[0];
  }
  return {label: '3:4', value: [3, 4]}; // 默认值
};

// 当前选中的比例
const selectedRatio = ref<any>(getDefaultRatio().value);
const selectedRatioLabel = ref(getDefaultRatio().label);

// 自定义比例相关
const isCustomRatio = ref(false);
const customWidth = ref('');
const customHeight = ref('');

// 计算属性
const visible = computed({
  get: () => cropper.visible,
  set: (v: boolean) => (cropper.visible = v)
});

const cropperImage = computed(() => cropper.sourceUrl || '');

// 是否固定比例
const isFixedRatio = computed(() => {
  return selectedRatio.value !== 'free';
});

// 当前固定比例数值
const currentFixedNumber = computed(() => {
  if (selectedRatio.value === 'free') {
    return [1, 1]; // 自由模式下的默认值
  }
  return selectedRatio.value;
});

// 方法
const cropperPreview = () => {
  // 可选：实时预览，如果后续需要可在此处理
};

const cancelUpload = () => {
  cropper.cancel();
};

const confirmCrop = () => {
  const instance = cropperRef.value;
  if (!instance) return;
  instance.getCropBlob((blob: Blob) => {
    cropper.confirm(blob);
  });
};

// 选择比例
const selectRatio = (ratio: any, label: string) => {
  selectedRatio.value = ratio;
  selectedRatioLabel.value = label;
  isCustomRatio.value = false;

  // 更新裁剪器
  updateCropper();
};

// 切换自定义比例
const toggleCustomRatio = () => {
  isCustomRatio.value = !isCustomRatio.value;
  if (isCustomRatio.value) {
    // 如果当前有选中的比例，将其设置为自定义比例的初始值
    if (Array.isArray(selectedRatio.value)) {
      customWidth.value = selectedRatio.value[0].toString();
      customHeight.value = selectedRatio.value[1].toString();
    } else {
      customWidth.value = '1';
      customHeight.value = '1';
    }
    selectedRatioLabel.value = '自定义';
  }
};

// 更新自定义比例
const updateCustomRatio = () => {
  const width = parseInt(customWidth.value) || 1;
  const height = parseInt(customHeight.value) || 1;

  if (width > 0 && height > 0) {
    selectedRatio.value = [width, height];
    updateCropper();
  }
};

// 更新裁剪器
const updateCropper = async () => {
  await nextTick();
  const instance = cropperRef.value;
  if (instance) {
    // 重新初始化裁剪器以应用新的比例设置
    instance.refresh();
  }
};

// 重置比例选择
const resetRatioSelection = () => {
  const defaultRatio = getDefaultRatio();
  selectedRatio.value = defaultRatio.value;
  selectedRatioLabel.value = defaultRatio.label;
  isCustomRatio.value = false;
  customWidth.value = '';
  customHeight.value = '';
};

// 关键：弹窗显示后刷新裁剪器，避免 0×0
watch(visible, async (v) => {
  if (v) {
    // 如果有传入的 ratios，则将初始比例设置为第一个
    if (ratios.value && ratios.value.length > 0) {
      const firstRatio = ratios.value[0];
      selectRatio(firstRatio.value, firstRatio.label);
    }
    await nextTick();
    cropperRef.value?.refresh();
  } else {
    // 弹窗关闭时重置比例选择
    resetRatioSelection();
  }
});

// 当图片地址切换时刷新裁剪器
watch(cropperImage, async (url) => {
  if (url) {
    await nextTick();
    cropperRef.value?.refresh();
  }
});
</script>

<style scoped lang="less">
.cropper-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.ratio-selector {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.ratio-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.ratio-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.ratio-btn {
  min-width: 60px;
  height: 28px;
  font-size: 12px;

  &:hover {
    transform: translateY(-1px);
    transition: transform 0.2s ease;
  }
}

.custom-ratio {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e9ecef;
}

.ratio-separator {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.cropper-main {
  border: 1px solid #e9ecef;
  border-radius: 6px;
  overflow: hidden;
}

// 响应式设计
@media (max-width: 768px) {
  .ratio-options {
    justify-content: center;
  }

  .ratio-btn {
    min-width: 50px;
    font-size: 11px;
  }

  .custom-ratio {
    justify-content: center;
  }
}
</style>