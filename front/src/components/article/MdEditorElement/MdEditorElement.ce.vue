<template>
  <MdEditor
      v-model="text"
      noUploadImg
      :theme="markdownStore.currentTheme"
      :previewTheme="markdownStore.currentPreviewTheme"
      :codeTheme="markdownStore.currentCodeTheme"
  />
</template>

<script setup>
import {ref, watch} from 'vue';
import {MdEditor, config} from 'md-editor-v3';
import screenfull from 'screenfull';
import katex from 'katex';
import mermaid from 'mermaid';
import highlight from 'highlight.js';

// >=3.0
import * as prettier from 'prettier';
import parserMarkdown from 'prettier/plugins/markdown';

import md from './data.md';
import {useMarkdownEditorStore} from '../../../store/index.js';

// 使用markdown编辑器store
const markdownStore = useMarkdownEditorStore();

config({
  iconfontType: 'class',
  editorExtensions: {
    prettier: {
      prettierInstance: prettier,
      parserMarkdownInstance: parserMarkdown
    },
    highlight: {
      instance: highlight
    },
    screenfull: {
      instance: screenfull
    },
    katex: {
      instance: katex
    },
    mermaid: {
      instance: mermaid
    }
  }
});

const text = ref(md);

// 新增：同步编辑内容到 store，初次加载也写入
watch(text, (v) => {
  markdownStore.setContent(v || '');
}, {immediate: true});

</script>

<style lang="css">
@import 'md-editor-v3/lib/style.css';
@import 'highlight.js/styles/atom-one-dark.css';
@import 'katex/dist/katex.min.css';

@import './iconfont.css';
</style>
