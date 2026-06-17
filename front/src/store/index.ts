import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';

const store = createPinia();
store.use(piniaPluginPersistedstate);

export {store}

export * from "./modules/setting.ts";
export * from "./modules/markdownEditor.ts";
export * from "./modules/cropper.ts";
export * from "./modules/user.ts";
export * from "./modules/notification.ts";

export default store