import { defineCustomElement } from 'vue';
import MdEditorElement from './MdEditorElement.ce.vue'

const MdEditorCustomElement = defineCustomElement(MdEditorElement);

export { MdEditorCustomElement };

export function register() {
  // 检查是否已经注册，避免重复注册
  if (!customElements.get('md-editor-element')) {
    customElements.define('md-editor-element', MdEditorCustomElement);
  }
}
