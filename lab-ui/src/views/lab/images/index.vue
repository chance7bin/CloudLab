<template>
  <div class="app-container">
    <el-table :data="images" stripe style="width: 100%">
      <el-table-column label="镜像名称" prop="repoTags" show-overflow-tooltip width="180" />
      <el-table-column label="镜像大小" prop="size" width="180" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button link type="primary" @click="init(scope.row)">初始化</el-button>
          <el-button link type="primary">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script name="images" setup lang="ts">
import { listImages } from "@/api/container/docker";
import { initWorkspace } from "@/api/container/workspace";
import useCurrentInstance from "@/utils/currentInstance";
import { ElMessageBox } from "element-plus";
const { proxy } = useCurrentInstance();

const images = ref<any[]>([]);

listImages().then((res) => {
  images.value = res.data;
  // console.log("images:", images.value);
});

function init(row) {
  let imageName = row.repoTags;

  ElMessageBox({
    title: "系统提示",
    message: "是否初始化工作空间",
    showCancelButton: true,
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
    beforeClose: (action, instance, done) => {
      if (action === "confirm") {
        instance.confirmButtonLoading = true;
        instance.confirmButtonText = "初始化中...";

        // 初始化工作空间
        initWorkspace(imageName)
          .then((res) => {
            proxy.$modal.msgSuccess("初始化工作空间成功");
            done();
            setTimeout(() => {
              instance.confirmButtonLoading = false;
            }, 300);
          })
          .catch(() => {
            setTimeout(() => {
              instance.confirmButtonLoading = false;
            }, 300);
            done();
          });
      } else {
        done();
      }
    }
  })
    .then((action) => {})
    .catch(() => {
      proxy.$modal.msg("未进行操作");
    });

  /* proxy.$modal
    .confirm("是否初始化工作空间")
    .then(function () {
      initWorkspace(imageName).then((res) => {
        proxy.$modal.msgSuccess("初始化工作空间成功");
      });
    })
    .catch(() => {
      proxy.$modal.msg("未进行操作");
    }); */
}
</script>

<style scoped lang="scss"></style>
