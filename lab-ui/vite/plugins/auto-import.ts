import autoImport from "unplugin-auto-import/vite";

export default function createAutoImport() {
  return autoImport({
    imports: ["vue", "vue-router", "pinia"],
    // 可以选择auto-import.d.ts生成的位置，使用ts建议设置为'src/auto-import.d.ts'
    dts: "src/auto-import.d.ts",
    // eslint报错解决
    eslintrc: {
      enabled: false, // Default `false`
      filepath: "src/.eslintrc-auto-import.json", // Default `./.eslintrc-auto-import.json`
      globalsPropValue: true // Default `true`, (true | false | 'readonly' | 'readable' | 'writable' | 'writeable')
    }
  });
}
