import {createApp} from 'vue'
import App from './App.vue'
import router from './router';
import {store} from './store';
import TDesign from 'tdesign-vue-next';
import {initTheme} from './utils/theme';
import './style/index.css';

const app = createApp(App);

app.use(TDesign);
app.use(store);
app.use(router);

// 初始化主题
initTheme();

app.mount('#app');