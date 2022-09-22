import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";

export default function createVueComponents() {
  return Components({
    resolvers: [ElementPlusResolver()],
    // 配置文件生成位置
    dts: "src/components.d.ts"
  });
}
