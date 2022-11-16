<template>
  <div class="app-container">
    <el-table :data="services" stripe style="width: 100%">
      <el-table-column label="服务名称" prop="msName" show-overflow-tooltip width="180" />
      <el-table-column label="创建者" prop="createBy" width="180" />
      <el-table-column label="创建时间" prop="createTime" width="180" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button link type="primary" @click="invokeService(scope.$index, scope.row)">调用</el-button>
          <el-button link type="primary">编辑</el-button>
          <el-button link type="primary">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import useCurrentInstance from "@/utils/currentInstance";
import { selectServiceList } from "@/api/msc";

const { proxy } = useCurrentInstance();
const router = useRouter();

const services = ref<any[]>([]);

selectServiceList().then((res) => {
  services.value = res.data;
});

function invokeService(index, row) {
  // console.log(row)
  let query = {msId: services.value[index].msId}
  router.push({path: "/service/invoke", query: query});

}

</script>

<style scoped lang="scss">

</style>