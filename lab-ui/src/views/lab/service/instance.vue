<template>
  <div class="app-container">
    <el-table :data="instances" stripe style="width: 100%">
      <el-table-column label="实例ID" prop="insId" show-overflow-tooltip width="300" />
      <el-table-column label="服务名称" prop="serviceName" show-overflow-tooltip width="180" />
      <el-table-column label="镜像名称" prop="imageName" width="180" />
      <el-table-column label="当前步骤" prop="currentState" width="180" />
      <el-table-column label="状态" prop="status" width="180" />
      <el-table-column label="运行用时" prop="spanTime" width="180" />
      <el-table-column label="服务类型" prop="serviceType" width="180" />
      <el-table-column label="创建时间" prop="createTime" width="200" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button link type="primary" >查看</el-button>
          <el-button link type="primary" >删除</el-button>
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

<script setup lang="ts">
import useCurrentInstance from "@/utils/currentInstance";
import {getMsInsList} from "@/api/container/msIns";

const {proxy} = useCurrentInstance();

const instances = ref<any[]>([]);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10
  }
});
const total = ref<number>(0);
const { queryParams } = toRefs(data);

getList();
function getList() {
  getMsInsList(queryParams.value).then((res : any) => {
    instances.value = res.rows;
    total.value = Number(res.total);
  });
}


</script>

<style scoped lang="scss">

</style>