import {defineStore} from 'pinia';

// 定义主题类型
export type EditorTheme = 'light' | 'dark';
export type PreviewTheme = 'default' | 'github' | 'vuepress' | 'mk-cute' | 'smart-blue' | 'cyanosis';
export type CodeTheme = 'atom' | 'a11y' | 'github' | 'gradient' | 'kimbie' | 'paraiso' | 'qtcreator' | 'stackoverflow';

// 定义状态接口
export interface MarkdownEditorState {
    theme: EditorTheme;
    previewTheme: PreviewTheme;
    codeTheme: CodeTheme;
    // 新增：编辑内容
    content: string;
}

// 默认状态
const defaultState: MarkdownEditorState = {
    theme: 'light',
    previewTheme: 'default',
    codeTheme: 'atom',
    // 新增：默认编辑内容为空
    content: ''
};

export const useMarkdownEditorStore = defineStore('markdownEditor', {
    state: (): MarkdownEditorState => ({
        ...defaultState
    }),

    getters: {
        // 获取当前编辑器主题
        currentTheme: (state) => state.theme,
        // 获取当前预览主题
        currentPreviewTheme: (state) => state.previewTheme,
        // 获取当前代码主题
        currentCodeTheme: (state) => state.codeTheme,
        // 获取所有主题设置
        allThemes: (state) => ({
            theme: state.theme,
            previewTheme: state.previewTheme,
            codeTheme: state.codeTheme
        }),
        // 新增：获取当前编辑内容
        currentContent: (state) => state.content
    },

    actions: {
        // 设置编辑器主题
        setTheme(theme: EditorTheme) {
            this.theme = theme;
        },

        // 设置预览主题
        setPreviewTheme(previewTheme: PreviewTheme) {
            this.previewTheme = previewTheme;
        },

        // 设置代码主题
        setCodeTheme(codeTheme: CodeTheme) {
            this.codeTheme = codeTheme;
        },

        // 批量更新主题设置
        updateThemes(themes: Partial<MarkdownEditorState>) {
            if (themes.theme !== undefined) {
                this.theme = themes.theme;
            }
            if (themes.previewTheme !== undefined) {
                this.previewTheme = themes.previewTheme;
            }
            if (themes.codeTheme !== undefined) {
                this.codeTheme = themes.codeTheme;
            }
        },

        // 重置为默认主题
        resetToDefault() {
            this.theme = defaultState.theme;
            this.previewTheme = defaultState.previewTheme;
            this.codeTheme = defaultState.codeTheme;
        },
        // 新增：设置编辑内容
        setContent(content: string) {
            this.content = content ?? '';
        }
    },

    // 持久化存储
    persist: {
        key: 'markdown-editor-themes',
        paths: ['theme', 'previewTheme', 'codeTheme']
    }
});

// 导出store实例获取函数