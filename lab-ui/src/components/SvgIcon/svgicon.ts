import * as components from "@element-plus/icons-vue";
import type { App } from "@vue/runtime-core";

export default {
  install: (app: App) => {
    // 注册elementplus图标
    for (const key in components) {
      const componentConfig = components[key];
      app.component(componentConfig.name, componentConfig);
    }
  }
};
