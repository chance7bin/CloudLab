<template>
  <div class="app-container">
    <el-table :data="images" stripe style="width: 100%">
      <el-table-column label="镜像名称" prop="imageName" show-overflow-tooltip width="180" />
      <el-table-column label="版本号" prop="tag" show-overflow-tooltip width="100" />
      <el-table-column label="镜像大小" prop="size" width="180" />
      <el-table-column label="状态" prop="size" width="180" >
        <template #default="scope">
          <el-tag type="success" v-if="scope.row.status == 'finished'">{{ scope.row.status }}</el-tag>
          <el-tag  v-else >{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button link type="primary" @click="init(scope.row)">创建工作空间</el-button>
          <el-button link type="primary">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

  </div>
</template>

<script name="images" setup lang="ts">
import { listImages } from "@/api/container/image";
import { initWorkspace } from "@/api/container/workspace";
import useCurrentInstance from "@/utils/currentInstance";
import {ElMessage, ElMessageBox} from "element-plus";
import {validateName} from "@/utils/validate";
const { proxy } = useCurrentInstance();

const total = ref<number>(0);
const images = ref<any[]>([]);



const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10
  }
});

const { queryParams} = toRefs(data);

getList();
function getList() {
  listImages(queryParams.value).then((res: any) => {
    images.value = res.rows;
    total.value = Number(res.total);
    // images.value = res.data;
    // console.log("images:", images.value);
  });
}

function init(row) {
  let imageId = row.imageId;

  ElMessageBox.prompt("", "新建空间", {
    confirmButtonText: "下一步",
    cancelButtonText: "取消",
    inputValidator: (value) => {
      return validateName(value);
    },
    inputPlaceholder: "工作空间名称",
    inputErrorMessage: "非法的工作空间名称"
  })
    .then(({ value }) => {
      if (value == "" || value == null) {
        proxy.$modal.msgWarning("请填写工作空间名称");
      } else {
        createWorkspace(imageId, value);
      }
    })
    .catch(() => {
      ElMessage({
        type: 'info',
        message: '取消',
      })
    });
}

const createWorkspace = (imageId: string, containerName: string) => {
  ElMessageBox({
    title: "系统提示",
    message: "是否初始化工作空间",
    showCancelButton: true,
    confirmButtonText: "创建",
    cancelButtonText: "取消",
    type: "warning",
    beforeClose: (action, instance, done) => {
      if (action === "confirm") {
        instance.confirmButtonLoading = true;
        instance.confirmButtonText = "初始化中...";

        // 初始化工作空间
        initWorkspace(imageId, containerName)
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
};
</script>

<style scoped lang="scss"></style>
