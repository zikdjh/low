import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { ComponentInstance, PageSchema } from '../../types/lowcode';

export const useDesignerStore = defineStore('designer', () => {
  const pageSchema = ref<PageSchema | null>(null);
  const componentTree = ref<ComponentInstance[]>([]);
  const selectedComponentId = ref<string | null>(null);
  const history = ref<ComponentInstance[][]>([]);
  const historyIndex = ref(-1);

  const selectedComponent = computed(() => {
    if (!selectedComponentId.value) return null;
    return findComponent(componentTree.value, selectedComponentId.value);
  });

  function findComponent(components: ComponentInstance[], id: string): ComponentInstance | null {
    for (const comp of components) {
      if (comp.id === id) return comp;
      if (comp.children.length > 0) {
        const found = findComponent(comp.children, id);
        if (found) return found;
      }
    }
    return null;
  }

  function initPage(schema: PageSchema | null = null) {
    pageSchema.value = schema || {
      pageCode: '',
      name: '',
      layoutJson: JSON.stringify([]),
      pageType: 'custom',
      version: 1,
      status: 'draft',
    } as PageSchema;
    componentTree.value = schema ? JSON.parse(schema.layoutJson) : [];
    selectedComponentId.value = null;
    history.value = [];
    historyIndex.value = -1;
  }

  function addComponent(parentId: string | null, compKey: string, props: Record<string, any> = {}) {
    const newComponent: ComponentInstance = {
      id: `comp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      compKey,
      label: '',
      props,
      style: {},
      children: [],
    };

    if (parentId) {
      const parent = findComponent(componentTree.value, parentId);
      if (parent) {
        parent.children.push(newComponent);
      }
    } else {
      componentTree.value.push(newComponent);
    }

    saveHistory();
    selectedComponentId.value = newComponent.id;
  }

  function updateComponent(id: string, updates: Partial<ComponentInstance>) {
    const component = findComponent(componentTree.value, id);
    if (component) {
      Object.assign(component, updates);
      saveHistory();
    }
  }

  function updateComponentProps(id: string, props: Record<string, any>) {
    const component = findComponent(componentTree.value, id);
    if (component) {
      component.props = { ...component.props, ...props };
      saveHistory();
    }
  }

  function deleteComponent(id: string) {
    removeComponent(componentTree.value, id);
    saveHistory();
    if (selectedComponentId.value === id) {
      selectedComponentId.value = null;
    }
  }

  function removeComponent(components: ComponentInstance[], id: string): boolean {
    const index = components.findIndex(c => c.id === id);
    if (index !== -1) {
      components.splice(index, 1);
      return true;
    }
    for (const comp of components) {
      if (removeComponent(comp.children, id)) {
        return true;
      }
    }
    return false;
  }

  function selectComponent(id: string | null) {
    selectedComponentId.value = id;
  }

  function moveComponent(direction: 'up' | 'down' | 'left' | 'right') {
    if (!selectedComponentId.value) return;

    const parent = findParent(componentTree.value, selectedComponentId.value);
    const siblings = parent ? parent.children : componentTree.value;
    const index = siblings.findIndex(c => c.id === selectedComponentId.value);

    if (direction === 'up' && index > 0) {
      [siblings[index], siblings[index - 1]] = [siblings[index - 1], siblings[index]];
      saveHistory();
    } else if (direction === 'down' && index < siblings.length - 1) {
      [siblings[index], siblings[index + 1]] = [siblings[index + 1], siblings[index]];
      saveHistory();
    }
  }

  function findParent(components: ComponentInstance[], id: string, parent: ComponentInstance | null = null): ComponentInstance | null {
    for (const comp of components) {
      if (comp.id === id) return parent;
      if (comp.children.length > 0) {
        const found = findParent(comp.children, id, comp);
        if (found) return found;
      }
    }
    return null;
  }

  function saveHistory() {
    const currentState = JSON.parse(JSON.stringify(componentTree.value));
    history.value = history.value.slice(0, historyIndex.value + 1);
    history.value.push(currentState);
    historyIndex.value = history.value.length - 1;
    if (history.value.length > 50) {
      history.value.shift();
      historyIndex.value--;
    }
  }

  function undo() {
    if (historyIndex.value > 0) {
      historyIndex.value--;
      componentTree.value = JSON.parse(JSON.stringify(history.value[historyIndex.value]));
    }
  }

  function redo() {
    if (historyIndex.value < history.value.length - 1) {
      historyIndex.value++;
      componentTree.value = JSON.parse(JSON.stringify(history.value[historyIndex.value]));
    }
  }

  function canUndo() {
    return historyIndex.value > 0;
  }

  function canRedo() {
    return historyIndex.value < history.value.length - 1;
  }

  function getLayoutJson() {
    return JSON.stringify(componentTree.value, null, 2);
  }

  function savePage(name: string, code: string) {
    if (!pageSchema.value) {
      pageSchema.value = {
        pageCode: code,
        name,
        layoutJson: getLayoutJson(),
        pageType: 'custom',
        version: 1,
        status: 'draft',
      } as PageSchema;
    } else {
      pageSchema.value.name = name;
      pageSchema.value.pageCode = code;
      pageSchema.value.layoutJson = getLayoutJson();
    }
    return pageSchema.value;
  }

  return {
    pageSchema,
    componentTree,
    selectedComponentId,
    selectedComponent,
    initPage,
    addComponent,
    updateComponent,
    updateComponentProps,
    deleteComponent,
    selectComponent,
    moveComponent,
    undo,
    redo,
    canUndo,
    canRedo,
    getLayoutJson,
    savePage,
  };
});