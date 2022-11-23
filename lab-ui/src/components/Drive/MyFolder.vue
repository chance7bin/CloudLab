<template>
  <div class="app-container">
    <!--头部按钮-->
    <div class="top-btn">
      <div>
        <el-button class="upload-btn" type="primary" round :icon="Upload">上传</el-button>
        <el-button class="upload-btn" type="primary" plain round :icon="FolderAdd">新建文件夹</el-button>
      </div>
      <div>
        <el-input v-model="searchText" placeholder="搜索我的云盘文件">
          <template #prefix>
            <el-icon class="el-input__icon"><search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!--文件夹导航-->
    <div class="folder-nav">
      <div class="folder-nav-left">
        <!--<el-icon class="mr10 icon-hover"><Back /></el-icon>-->
        <!--<el-icon class="mr10 icon-hover"><Right /></el-icon>-->
        <el-icon class="mr10 icon-hover" @click="refreshFolder"><RefreshRight /></el-icon>
        <el-breadcrumb class="folder-breadcrumb" :separator-icon="ArrowRight">
          <el-breadcrumb-item v-for="(item, index) in breadcrumbList" :key="index">
            <a @click="breadcrumbClick(item)">{{ item }}</a>
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!--文件夹容器-->
    <div class="folder-container">
      <el-space wrap>
        <div v-for="(item, index) in folderList" :key="index">
          <!--{{ item.filename }}-->
          <div
            class="folder-item"
            :class="selectedItem == index ? 'folder-active' : ''"
            @dblclick="folderDbClick(item)"
            @click="folderSelect(index)"
          >
            <svg-icon v-if="item.directory" icon-class="folder" class-name="icon-size"></svg-icon>
            <svg-icon v-else icon-class="file" class-name="icon-size"></svg-icon>
            <div class="ellipsis-fixed">
              <div class="overflow-ellipsis">{{ item.filename }}</div>
            </div>
          </div>
        </div>
      </el-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import useCurrentInstance from "@/utils/currentInstance";
import { Upload, FolderAdd, ArrowLeft, ArrowRight, RefreshRight } from "@element-plus/icons-vue";
const { proxy } = useCurrentInstance();
import { ElNotification as notify } from "element-plus";
import { listWorkspaceDir } from "@/api/drive/drive";
import qs from "qs";


// 导航部分
const breadcrumbList = ref<string[]>(["我的云盘"]);
let searchText = ref<string>("");
const onBack = () => {
  notify("Back");
};
const breadcrumbClick = (item) => {
  // console.log("breadcrumbClick:", item);

  //定位到当前文件夹
  let tempList = [] as string[];
  for (let i = 0; i < breadcrumbList.value.length; i++) {
    tempList.push(item as string);
    if (breadcrumbList.value[i] == item){
      break;
    }
  }

  breadcrumbList.value = tempList;

  getFolder();
}

const refreshFolder = () => {
  getFolder();
}


// 文件夹部分

const folderList = ref<any[]>([]);
const selectedItem = ref<number | null>();
listWorkspaceDir().then((res) => {
  // console.log("listWorkspaceDir:", res);
  folderList.value = res.data;
});

const folderSelect = (index) => {
  selectedItem.value = index;
};

const folderDbClick = (item) => {
  if (item.directory == false){
    return;
  }
  breadcrumbList.value.push(item.filename);
  getFolder();

};


// 根据breadcrumbList获取该文件夹下的文件
const getFolder = () => {

  selectedItem.value = null;

  // 弹出根路径 "我的云盘"
  breadcrumbList.value.shift();

  let params = { pathList: breadcrumbList.value };
  let paramsStr = qs.stringify(params, {arrayFormat:"repeat"})

  // 恢复根路径
  breadcrumbList.value.unshift("我的云盘");
  listWorkspaceDir(paramsStr).then((res) => {
    // console.log("listWorkspaceDir:", res);
    folderList.value = res.data;
  });
}

</script>

<style scoped lang="scss">
$icon-size: 80px;
$padding-horizontal: 20px;

.top-btn {
  display: flex;
  justify-content: space-between;
}

.upload-btn {
  margin-left: 10px;
}
.folder-nav {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  margin: 10px 0;
  border-top: 1px solid #e1e4ec;
  border-bottom: 1px solid #e1e4ec;
}
.folder-nav-left {
  display: flex;
  align-items: center;
}

.folder-breadcrumb {
  padding: 0 10px;
  border-left: 2px solid #c8c8d0;
}

.icon-size {
  width: $icon-size;
  height: $icon-size;
}
.folder-item {
  width: calc($icon-size + $padding-horizontal * 2 + 20px);
  padding: 10px $padding-horizontal;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  border: 2px solid transparent;
  transition: 400ms ease all;

  div {
    text-align: center;
  }

  > div {
    min-width: 0;
    margin-top: 10px;
    padding: 0 $padding-horizontal;
  }

  &:hover {
    background: #f2faff;

    &:before,
    &:after {
      width: 100%;
      transition: 400ms ease all;
    }
  }

  &:before,
  &:after {
    content: "";
    position: absolute;
    top: 0;
    right: 0;
    height: 2px;
    width: 0;
    transition: 400ms ease all;
  }

  &::after {
    right: inherit;
    top: inherit;
    left: 0;
    bottom: 0;
  }
}
.ellipsis-fixed {
  width: inherit;
}

.folder-active {
  background-color: #daf5ff;
  border: 2px solid #59dcff;
}

.icon-hover {
  transition: 400ms ease all;

  &:hover {
    cursor: pointer;
    color: #409eff;

    &:before,
    &:after {
      width: 100%;
      transition: 400ms ease all;
    }
  }
}

</style>
