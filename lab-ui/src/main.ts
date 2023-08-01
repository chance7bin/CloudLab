import { createApp } from "vue";

import Cookies from "js-cookie";

import ElementPlus from "element-plus";
import locale from "element-plus/lib/locale/lang/zh-cn"; // 中文语言

import "@/assets/styles/index.scss"; // global css

import App from "./App.vue";

import store from "./stores";
import router from "./router";
import directive from "./directive"; // directive

// svg图标
import "virtual:svg-icons-register";

import SvgIcon from "@/components/SvgIcon/index.vue";
import elementIcons from "@/components/SvgIcon/svgicon";

import "./permission"; // permission control

// 注册指令
import plugins from "./plugins"; // plugins
import {download} from "@/utils/request";
// import { parseTime, resetForm, addDateRange, handleTree, selectDictLabel, selectDictLabels } from "@/utils/common";
import {resetForm, addDateRange, parseTime, handleTree} from "@/utils/common";
import {useDict} from "@/utils/dict";

const app = createApp(App);

// 全局方法挂载
app.config.globalProperties.useDict = useDict;
app.config.globalProperties.download = download;
app.config.globalProperties.parseTime = parseTime;
app.config.globalProperties.resetForm = resetForm;
app.config.globalProperties.handleTree = handleTree;
app.config.globalProperties.addDateRange = addDateRange;
// app.config.globalProperties.selectDictLabel = selectDictLabel;
// app.config.globalProperties.selectDictLabels = selectDictLabels;

// 分页组件
// import Pagination from "@/components/Pagination";
// 自定义表格工具组件
// import RightToolbar from "@/components/RightToolbar";
// 文件上传组件
// import FileUpload from "@/components/FileUpload";
// 图片上传组件
// import ImageUpload from "@/components/ImageUpload";
// 图片预览组件
// import ImagePreview from "@/components/ImagePreview";
// 自定义树选择组件
// import TreeSelect from "@/components/TreeSelect";
// 字典标签组件
// import DictTag from "@/components/DictTag";

// 全局组件挂载
// app.component("DictTag", DictTag);
// app.component("Pagination", Pagination);
// app.component("TreeSelect", TreeSelect);
// app.component("FileUpload", FileUpload);
// app.component("ImageUpload", ImageUpload);
// app.component("ImagePreview", ImagePreview);
// app.component("RightToolbar", RightToolbar);

// Vue-Cropper
import VueCropper from "vue-cropper";
import "vue-cropper/dist/index.css";

app.use(VueCropper);

// 注册vue-simple-uploader
//引入大文件分片上传
import uploader from "vue-simple-uploader";
import "vue-simple-uploader/dist/style.css";

app.use(uploader);

// 引入jquery
// import $ from "jquery";
// app.use($);
// 引入webuploader
// import WebUploader from "webuploader";

app.use(router);
app.use(store);
app.use(plugins);
app.use(elementIcons);
app.component("SvgIcon", SvgIcon);

directive(app);

// 使用element-plus 并且设置全局的大小
app.use(ElementPlus, {
  locale: locale,
  // 支持 large、default、small
  size: Cookies.get("size") || "default"
});

app.mount("#app");
