module.exports = {
  root: true,
  env: {
    node: true
  },
  parserOptions: {
    ecmaVersion: 2020,
    parser: "babel-eslint"
  },
  parser: "@typescript-eslint/parser",
  extends: [
    "plugin:vue/recommended",
    "eslint:recommended",
    './src/.eslintrc-auto-import.json',
    "plugin:prettier/recommended" //把所有格式相关的校验都交给 prettier 处理
  ],
  plugins: ["prettier"],
  rules: {
    "prettier/prettier": "error",
    "no-console": process.env.NODE_ENV === "production" ? "error" : "off",
    "no-debugger": process.env.NODE_ENV === "production" ? "error" : "off",
    // "space-before-function-paren": ["error", "never"],
    "vue/array-bracket-spacing": "error",
    "vue/arrow-spacing": "error",
    "vue/block-spacing": "error",
    "vue/brace-style": "error",
    "vue/camelcase": "error",
    "vue/comma-dangle": "error",
    "vue/component-name-in-template-casing": ["error", "kebab-case"],
    "vue/eqeqeq": "error",
    "vue/key-spacing": "error",
    "vue/match-component-file-name": "error",
    "vue/object-curly-spacing": "error",
    "no-unused-vars": "off",
    "array-element-newline": [
      "error",
      {
        ArrayExpression: "consistent",
        ArrayPattern: { "minItems": null },
      }
    ],
  }
};
