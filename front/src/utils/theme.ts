import {getSettingStore} from '../store';

/**
 * 初始化主题设置
 */
export function initTheme() {
    const settingStore = getSettingStore();

    // 先初始化显示模式，再初始化主题色，确保正确的顺序
    settingStore.changeMode(settingStore.mode).then((r: any) => {
        console.log('系统主题变化', r);
        // 在模式设置完成后再应用主题色，确保主题色与模式同步
        settingStore.changeBrandTheme(settingStore.brandTheme);
    });

    // 监听系统主题变化
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    mediaQuery.addEventListener('change', () => {
        if (settingStore.mode === 'auto') {
            // 使用平滑过渡效果
            smoothThemeTransition(() => {
                settingStore.changeMode('auto').then((r: any) => {
                    console.log('系统主题变化', r);
                });
            });
        }
    });
}

/**
 * 平滑主题切换助手函数
 */
export function smoothThemeTransition(callback: () => void) {
    // 添加过渡类
    document.documentElement.classList.add('theme-transitioning');
    
    // 执行主题切换
    callback();
    
    // 移除过渡类
    setTimeout(() => {
        document.documentElement.classList.remove('theme-transitioning');
    }, 300);
}