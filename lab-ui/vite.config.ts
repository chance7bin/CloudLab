import { fileURLToPath, URL } from "node:url";

import { defineConfig, loadEnv } from "vite";
import createVitePlugins from "./vite/plugins";
import path from "path";

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd());
  const { VITE_APP_ENV } = env;
  return {
    // 部署生产环境和开发环境下的URL。
    // 默认情况下，vite 会假设你的应用是被部署在一个域名的根路径上
      // 例如 https://www.binb.vip/。如果应用被部署在一个子路径上，你就需要用这个选项指定这个子路径。例如，如果你的应用被部署在 https://www.ruoyi.vip/admin/，则设置 baseUrl 为 /admin/。
    base: VITE_APP_ENV === "production" ? "/" : "/",
    plugins: createVitePlugins(env, command === "build"),
    resolve: {
      // alias: {
      //   "@": fileURLToPath(new URL("./src", import.meta.url))
      // },
      // https://cn.vitejs.dev/config/#resolve-alias
      alias: {
        // 设置路径
        "~": path.resolve(__dirname, "./"),
        // 设置别名
        "@": path.resolve(__dirname, "./src")
      },
      // https://cn.vitejs.dev/config/#resolve-extensions
      extensions: [".mjs", ".js", ".ts", ".jsx", ".tsx", ".json", ".vue"]
    },
    // vite 相关配置
    server: {
      port: 80,
      host: true,
      open: true,
      proxy: {
        // https://cn.vitejs.dev/config/#server-proxy
        "/dev-api-base": {
          target: "http://172.21.212.240:8807/admin",
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api-base/, "")
          // rewrite: (p) => p.replace("^" + process.env.VITE_APP_BASE_API, "")
        },
        "/dev-api-drive": {
          target: "http://172.21.212.240:8809/drive",
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api-drive/, "")
        },
        "/dev-api-container": {
          target: "http://172.21.212.240:8810/container",
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api-container/, "")
        }
      }
    }
  };
});
