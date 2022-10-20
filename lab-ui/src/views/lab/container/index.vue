<template>
  <div class="app-container">
    <!--容器操作-->
    <!--<el-row :gutter="20">
      <el-col :span="3">
        <el-button type="primary">初始化工作空间</el-button>
      </el-col>
      <el-col :span="3">
        <el-button type="success">重启工作空间</el-button>
      </el-col>
      <el-col :span="3">
        <el-button type="info">删除工作空间</el-button>
      </el-col>
      <el-col :span="3">
        <el-button type="warning">新建工作空间</el-button>
      </el-col>
    </el-row>-->

    <!--容器列表-->
    <el-table :data="containers" stripe style="width: 100%">
      <el-table-column label="容器名称" prop="containerName" show-overflow-tooltip width="360" />
      <el-table-column label="镜像名称" prop="imageName" width="200" />
      <el-table-column label="容器状态" prop="status" width="180" />
      <el-table-column label="启动时间" prop="started" width="180" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button link type="primary" @click="enterWorkspace(scope.$index, scope.row)">进入</el-button>
          <el-button link type="primary">重启</el-button>
          <el-button link type="primary">删除</el-button>
        </template>
      </el-table-column>
    </el-table>


  </div>
</template>

<script name="container" setup lang="ts">

import { listContainers } from "@/api/docker";
import useCurrentInstance from "@/utils/currentInstance";

const { proxy } = useCurrentInstance();
const router = useRouter();


const containers = ref<any[]>([]);

listContainers().then((res) => {
  containers.value = res.data;
  // console.log("images:", images.value);
});

function enterWorkspace(index, row) {
  // console.log(row)
  let query = {containerId: containers.value[index].containerId}
  router.push({path: "/workspace/instance", query: query});

}


</script>

<style scoped>
.el-row {
  margin-bottom: 20px;
}
.el-row:last-child {
  margin-bottom: 0;
}
.el-col {
  border-radius: 4px;
}

.grid-content {
  border-radius: 4px;
  min-height: 36px;
}
</style>
