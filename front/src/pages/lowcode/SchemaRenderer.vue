<template>
  <div class="schema-renderer">
    <div v-if="loading" class="loading">
      <t-loading size="large" text="加载中..." />
    </div>
    <template v-else>
      <div class="page-header">
        <h2>{{ pageData?.name || '页面预览' }}</h2>
        <t-tag v-if="pageData?.status === 'published'" theme="success" variant="light">已发布</t-tag>
        <t-tag v-else theme="warning" variant="light">草稿</t-tag>
      </div>
      <div class="page-content">
        <template v-for="comp in componentTree" :key="comp.id">
          <RecursiveRenderer :component="comp" />
        </template>
        <div v-if="componentTree.length === 0" class="empty-tip">
          <p>暂无内容</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, defineComponent, h } from 'vue';
import { useRoute } from 'vue-router';
import type { ComponentInstance } from '../../types/lowcode';
import { pageSchemaApi } from '../../api/lowcode/pageSchema';
import ButtonElement from './page/components/ButtonElement.vue';
import CardElement from './page/components/CardElement.vue';
import ChartElement from './page/components/ChartElement.vue';
import CheckboxElement from './page/components/CheckboxElement.vue';
import DateElement from './page/components/DateElement.vue';
import DividerElement from './page/components/DividerElement.vue';
import FormElement from './page/components/FormElement.vue';
import GridElement from './page/components/GridElement.vue';
import InputElement from './page/components/InputElement.vue';
import ListElement from './page/components/ListElement.vue';
import RadioElement from './page/components/RadioElement.vue';
import SelectElement from './page/components/SelectElement.vue';
import SpaceElement from './page/components/SpaceElement.vue';
import TableElement from './page/components/TableElement.vue';
import TextElement from './page/components/TextElement.vue';

const route = useRoute();
const componentTree = ref<ComponentInstance[]>([]);
const pageData = ref<any>(null);
const loading = ref(true);

const componentMap: Record<string, any> = {
  button: ButtonElement,
  card: CardElement,
  chart: ChartElement,
  checkbox: CheckboxElement,
  date: DateElement,
  divider: DividerElement,
  form: FormElement,
  grid: GridElement,
  input: InputElement,
  list: ListElement,
  radio: RadioElement,
  select: SelectElement,
  space: SpaceElement,
  table: TableElement,
  text: TextElement,
};

async function loadPage() {
  loading.value = true;
  const pageCode = route.params.pageCode as string;
  if (!pageCode) {
    loading.value = false;
    return;
  }
  try {
    const res = await pageSchemaApi.getByPageCode(pageCode);
    if (res.data.code === 1 && res.data.data) {
      pageData.value = res.data.data;
      try {
        const layout = typeof res.data.data.layoutJson === 'string'
          ? JSON.parse(res.data.data.layoutJson)
          : (res.data.data.layoutJson || []);
        componentTree.value = Array.isArray(layout) ? layout : [];
      } catch (e) {
        console.error('Failed to parse layout JSON:', e);
      }
    }
  } catch (e: any) {
    console.error('Failed to load page:', e);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadPage();
});

function getRenderComponent(compKey: string) {
  return componentMap[compKey] || TextElement;
}

const RecursiveRenderer = defineComponent({
  name: 'RecursiveRenderer',
  props: {
    component: {
      type: Object as () => ComponentInstance,
      required: true,
    },
  },
  render() {
    const comp = this.component;
    const RenderComponent = getRenderComponent(comp.compKey);
    
    const children = comp.children && comp.children.length > 0
      ? comp.children.map((child: ComponentInstance) => 
          h(RecursiveRenderer, { component: child, key: child.id })
        )
      : [];
    
    return h('div', { class: 'render-component' }, [
      h(RenderComponent, { element: comp, 'enable-events': true }, children),
    ]);
  },
});
</script>

<style scoped>
.schema-renderer {
  min-height: 100vh;
  padding: 24px;
  background: #f5f5f5;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #1a1a1a;
  }
}

.page-content {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  padding: 24px;
}

.empty-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #999;
}

.empty-tip p {
  margin: 0;
}

.render-component {
  margin: 8px 0;
}
</style>