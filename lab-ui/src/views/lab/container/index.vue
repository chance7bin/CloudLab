<template>
  <div class="app-container">
    <!-- 容器操作 -->
    <el-row :gutter="20">
      <el-col :span="3">
        <el-button type="primary" @click="initContainer">初始化工作空间</el-button>
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
    </el-row>
  </div>
</template>

<script name="container" setup lang="ts">
import { initWorkspace } from "@/api/workspace";

const { proxy } = getCurrentInstance() as any;

function initContainer() {
  proxy.$modal.confirm("是否初始化工作空间")
    .then(function () {
      initWorkspace()
        .then((res: any) => {
          console.log("initWorkspace:", res);
          proxy.$modal.msgSuccess("初始化工作空间成功");
        })
        .catch(() => {
          proxy.$modal.msgSuccess("初始化工作空间失败");
        });
    })
    .catch(() => {
      proxy.$modal.msg("未进行操作");
    });
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
