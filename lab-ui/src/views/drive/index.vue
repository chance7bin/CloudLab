<template>
  <div class="app-container">

    <!--云盘布局-->
    <div class="drive-layout">
      <!--<el-row :gutter="0">

        <el-col :span="3" >
          &lt;!&ndash;菜单栏&ndash;&gt;
          <el-menu
            active-text-color="#0db2ff"
            background-color="#f9fafb"
            text-color="#000"
            :default-active=currentIndex
            class="drive-menu"
            @select="handleSelect"
            :collapse="isCollapse"
          >
            <el-menu-item index="1">
              <el-icon><Folder /></el-icon>
              <span>我的文件</span>
            </el-menu-item>
            <el-menu-item index="2" disabled>
              <el-icon><UploadFilled /></el-icon>
              <span>正在上传</span>
            </el-menu-item>
            <el-menu-item index="3" >
              <el-icon><CircleCheck /></el-icon>
              <span>传输完成</span>
            </el-menu-item>
            <el-menu-item index="4" disabled>
              <el-icon><Delete /></el-icon>
              <span>回收站</span>
            </el-menu-item>
          </el-menu>
        </el-col>

        <el-col :span="21">

          <my-folder v-if="currentIndex == '1'"></my-folder>
          <uploading v-if="currentIndex == '2'"></uploading>
          <uploaded v-if="currentIndex == '3'"></uploaded>
          <recycle-bin v-if="currentIndex == '4'"></recycle-bin>

        </el-col>
      </el-row>-->

      <!--<el-scrollbar :height="driveLayoutHeight">-->
        <my-folder></my-folder>
      <!--</el-scrollbar>-->

    </div>

  </div>
</template>

<script setup lang="ts">
import useCurrentInstance from "@/utils/currentInstance";

const { proxy } = useCurrentInstance();

import {
  Folder,
  Menu as IconMenu,
  Upload,
  UploadFilled,
  CircleCheck,
  Delete
} from '@element-plus/icons-vue'
import MyFolder from "@/components/Drive/MyFolder.vue";
import Uploading from "@/views/drive/Uploading.vue";
import Uploaded from "@/views/drive/Uploaded.vue";
import RecycleBin from "@/views/drive/RecycleBin.vue";


// 计算drive-layout高度
// let driveLayoutHeight = document.documentElement.clientHeight - 84 - 20 - 20;


//导航栏部分
const currentIndex = ref<string>("1");
const isCollapse = ref(false)

const handleSelect = (index: string, indexPath: string[]) => {
  currentIndex.value = index;
}
</script>

<style scoped lang="scss">

$layout-height: calc(100vh - 84px - 20px - 20px);


.drive-layout{
  min-height: $layout-height;
  max-height: $layout-height;
  overflow: scroll;
  overflow-x: hidden;
  border: none;
  border-radius: 15px;
  box-shadow: rgba(0, 0, 0, 0.35) 0px 5px 15px;
}
.drive-menu{
  //padding-bottom: 0;
  min-height: $layout-height;
  //border-radius: 15px 0 0 15px;
}

.is-active{
  background-color: #e0f2fb;
  border-left: 5px solid #06a3f8;
  //font-weight: bold;
  padding: 0 20px 0 15px !important;
}
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}
</style>