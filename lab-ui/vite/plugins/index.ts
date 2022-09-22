import vue from "@vitejs/plugin-vue";

import createAutoImport from "./auto-import";
import createSvgIcon from "./svg-icon";
import createCompression from "./compression";
import createSetupExtend from "./setup-extend";
import createVueComponents from "./vue-components";

export default function createVitePlugins(viteEnv: any, isBuild = false) {
  const vitePlugins = [vue()];
  vitePlugins.push(createAutoImport());
  vitePlugins.push(createVueComponents());
  vitePlugins.push(createSetupExtend());
  vitePlugins.push(createSvgIcon(isBuild));
  isBuild && vitePlugins.push(...createCompression(viteEnv));
  return vitePlugins;
}
