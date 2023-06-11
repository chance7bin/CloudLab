<template>
  <div class="app-container">
    <!--头部按钮-->
    <div class="top-btn">
      <div>
        <el-button class="upload-btn" type="primary" round :icon="Upload" @click="dialogVisible = true">上传</el-button>
        <el-button class="upload-btn" type="primary" plain round :icon="FolderAdd" @click="openDialog"
          >新建文件夹</el-button
        >
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
            <a @click="breadcrumbClick(item)">{{ item.name }}</a>
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!--文件夹容器-->
    <div class="folder-container">
      <!--<el-scrollbar :height="folderHeight">-->
        <el-empty v-if="folderList.length === 0" description="哎呀, 怎么一个文件都没有呢 (T_T)" />
        <el-space v-else wrap>
          <div v-for="(item, index) in folderList" :key="index">
            <!--{{ item.filename }}-->

            <el-tooltip :content="item.filename" placement="bottom" effect="light" :show-after="600">
              <div
                  class="folder-item"
                  :class="selectedItem == item.fileId ? 'folder-active' : ''"
                  @dblclick="folderDbClick(item)"
                  @click="folderSelect(item, index)"
                  @mouseover="item['showPlugin'] = true"
                  @mouseleave="item['showPlugin'] = false"
              >
                <!--下载按钮-->
                <div class="item-plugin">
                  <!--<el-icon><Download /></el-icon>-->
                  <svg-icon
                      class="icon-hover"
                      icon-class="download-file"
                      @click="downloadFile(item)"
                      v-show="!item.directory && item['showPlugin']"
                  ></svg-icon>
                </div>

                <!--图标-->
                <!--<svg-icon v-if="item.directory" icon-class="folder" class-name="icon-size"></svg-icon>-->
                <!--<svg-icon v-else icon-class="file" class-name="icon-size"></svg-icon>-->
                <svg-icon :icon-class="selectTypeIcon(item)" class-name="icon-size"></svg-icon>

                <!--文件名-->
                <div class="ellipsis-fixed">
                  <div class="overflow-ellipsis">{{ item.filename }}</div>
                </div>

              </div>
            </el-tooltip>
          </div>
        </el-space>
      <!--</el-scrollbar>-->
      <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getFolder"
      />
    </div>

    <!--新建文件夹-->

    <!--上传文件对话框-->
    <div>
      <el-dialog v-model="dialogVisible" title="上传文件" width="30%" draggable>
        <!--<span>支持上传一个或多个文件</span>-->
        <simple-uploader @uploadSuccess="uploadSuccess"></simple-uploader>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import useCurrentInstance from "@/utils/currentInstance";
import { Download, Upload, FolderAdd, ArrowLeft, ArrowRight, RefreshRight } from "@element-plus/icons-vue";
const { proxy } = useCurrentInstance();
import { ElNotification as notify, ElMessageBox, ElMessage } from "element-plus";
import { getFileList, addFile } from "@/api/drive/drive";
import type { FileInfoDTO } from "@/api/drive/drive";
import { checkAuth } from "@/api/admin/user";
import {ref} from "vue";
import useDriveStore from "@/stores/modules/drive";

const total = ref<number>(0);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10
  }
});
const { queryParams } = toRefs(data);

const emit = defineEmits(["selectedItem"]);

// 文件夹部分
// let folderHeight = document.querySelector(".folder-container")?.clientHeight;
const folderList = ref<any[]>([]);
const selectedItem = ref<number | null>();

// 在 DOM 渲染完成后执行的操作
onMounted(() => {
  // 根据app-container的高度计算folder-container的高度
  // queryParams.value.pageSize = calcPaginationSize();
  queryParams.value.pageSize = 40;

  // 先获取到请求的分页大小，再获取文件列表
  getFolder();
});

function calcPaginationSize(){

  const appContainer = document.querySelector(".app-container");
  const topBtn = document.querySelector(".top-btn");
  const folderNav = document.querySelector(".folder-nav");
  const paginationCls = document.querySelector(".pagination-container");

  let itemHeight = 160;
  let itemWeight = 150;

  let containerPadding = 20;
  const height = (appContainer as Element).clientHeight - (topBtn as Element).clientHeight
      - (folderNav as Element).clientHeight - (paginationCls as Element).clientHeight - containerPadding * 4;
  const weight = (appContainer as Element).clientWidth - containerPadding * 4;
  let colCnt = Math.floor(height / itemHeight);
  let rowCnt = Math.floor(weight / itemWeight);
  console.log("colCnt:", colCnt, "rowCnt:", rowCnt);
  return colCnt * rowCnt;
}

// 云盘路径
interface FilePath {
  name: string;
  parentId: string;
}

const dialogVisible = ref(false);

// 导航部分
const breadcrumbList = ref<FilePath[]>([{ name: "我的云盘", parentId: "root" }]);
let searchText = ref<string>("");
// 当前展示的文件夹位置
let currentFile: FilePath = { name: "我的云盘", parentId: "root" };
const onBack = () => {
  notify("Back");
};
const breadcrumbClick = (item) => {
  // console.log("breadcrumbClick:", item);
  // console.log("breadcrumbList:", breadcrumbList);

  //定位到当前文件夹
  let tempList = [] as FilePath[];
  for (let i = 0; i < breadcrumbList.value.length; i++) {
    tempList.push(breadcrumbList.value[i]);
    if (breadcrumbList.value[i].parentId == item.parentId) {
      break;
    }
  }

  breadcrumbList.value = tempList;

  currentFile = item;

  getFolder();
};

const refreshFolder = () => {
  getFolder();
};

// 根据breadcrumbList获取该文件夹下的文件
function getFolder() {
  // selectedItem.value = null;
  // useDriveStore().setCurrentPath(currentFile.parentId);
  getFileList(currentFile.parentId, queryParams.value).then((res : any) => {
    // console.log("listWorkspaceDir:", res);
    folderList.value = res.rows;
    total.value = Number(res.total);
  });
}



const folderSelect = (item, index) => {
  selectedItem.value = item.fileId;
  emit("selectedItem", item);
};

const folderDbClick = (item) => {
  if (item.directory == false) {
    return;
  }
  // console.log("item:", item);
  const nextFile = { name: item.filename, parentId: item.fileId };
  breadcrumbList.value.push(nextFile);
  currentFile = nextFile;
  getFolder();
};

const uploadFile = () => {};
const validateFileName = () => {};

const openDialog = () => {
  ElMessageBox.prompt("", "新建", {
    confirmButtonText: "确认",
    cancelButtonText: "取消",
    inputValidator: (value) => {
      let reg = new RegExp('[\\\\/:*?"<>|]');
      return !reg.test(value);
    },
    inputPlaceholder: "新建文件夹",
    inputErrorMessage: "非法的文件夹名称"
  })
    .then(({ value }) => {

      if (value == "" || value == null){
        proxy.$modal.msgWarning("请填写文件夹名称")
      }

      else {
        addFileDir(value);
      }

      // ElMessage({
      //   type: 'success',
      //   message: `Your email is:${value}`,
      // })
    })
    .catch(() => {
      // ElMessage({
      //   type: 'info',
      //   message: 'Input canceled',
      // })
    });
};
const addFileDir = (filename: string) => {
  const fileInfoDTO: FileInfoDTO = {
    parentId: currentFile.parentId,
    filename: filename,
    directory: true,
    md5: null,
    size: null,
    type: null,
    driveFileId: null
  };
  addFile(fileInfoDTO).then(() => {
    proxy.$modal.msgSuccess("文件夹添加成功");
    getFolder();
  });
};

// 上传组件上传成功后的事件传递
const uploadSuccess = (file) => {
  console.log("uploadSuccess:", file);
  const fileInfoDTO: FileInfoDTO = {
    parentId: currentFile.parentId,
    filename: file.name,
    directory: false,
    md5: file.uniqueIdentifier,
    size: file.size,
    type: file.fileType,
    driveFileId: null
  };
  addFile(fileInfoDTO).then(() => {
    proxy.$modal.msgSuccess("文件添加成功");
    dialogVisible.value = false;
    getFolder();
  });
};

// 下载文件
const downloadFile = async (item) => {
  // console.log(item);
  await checkAuth();

  window.location.href = import.meta.env.VITE_APP_DRIVE_API + "/file/download/" + item.driveFileId + "?downloadFilename=" + item.filename;

  // proxy.download(import.meta.env.VITE_APP_DRIVE_API + "/file/download/" + item.driveFileId, {}, "123");

};

// 选择文件展示的图标
const selectTypeIcon = (item): string => {
  if (item.directory) {
    return "folder";
  } else {
    let icon;
    switch (item.type) {
      case "pdf":
        icon = "pdf_big";
        break;
      case "doc":
      case "docx":
        icon = "doc_big";
        break;
      case "xls":
      case "xlsx":
        icon = "xlsx_big";
        break;
      case "csv":
        icon = "csv_big";
        break;
      case "txt":
        icon = "txt_big";
        break;
      case "exe":
        icon = "exe_big";
        break;
      case "jpg":
      case "png":
      case "gif":
        icon = "jpg_big";
        break;
      case "zip":
      case "tar.gz":
        icon = "zip_big"
        break;
      case "mp4":
      case "avi":
        icon = "video_big";
        break;
      default:
        icon = "file";
    }
    return icon;
  }
};
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
  //position: relative;
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

.item-plugin {
  height: 20px;
  margin: 0 !important;
  //display: flex !important;
  //justify-content: right !important;
  //float: right !important;
  padding: 0 !important;
}

.newFolderClass{
  //position: absolute !important;
  //margin-top: 15vh !important;
  //background-color: #1ab394 !important;
}
</style>
