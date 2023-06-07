<template>
  <div class="app-container">
    <el-table :data="services" stripe style="width: 100%">
      <el-table-column label="服务名称" prop="msName" show-overflow-tooltip width="180" />
      <el-table-column label="创建者" prop="createBy" width="180" />
      <el-table-column label="创建时间" prop="createTime" width="200" />
      <el-table-column label="部署状态" width="180" >
        <template #default="scope">
          <el-tag type="success" v-if="scope.row.deployStatus == 'FINISHED'">{{ scope.row.deployStatus }}</el-tag>
          <el-tag type="danger" v-else-if="scope.row.deployStatus == 'ERROR'">{{ scope.row.deployStatus }}</el-tag>
          <el-tag v-else >{{ scope.row.deployStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button link type="primary" @click="invokeService(scope.$index, scope.row)">调用</el-button>
<!--          <el-button link type="primary">编辑</el-button>-->
          <el-button link type="primary" @click="deleteService(scope.row)">删除</el-button>
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
import { selectServiceList } from "@/api/container/modelService";

const { proxy } = useCurrentInstance();
const router = useRouter();

const services = ref<any[]>([]);

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
  selectServiceList(queryParams.value).then((res : any) => {
    services.value = res.rows;
    total.value = Number(res.total);
  });
}

function invokeService(index, row) {
  // console.log(row)
  let deployStatus = services.value[index].deployStatus;
  if (deployStatus == "ERROR"){
    proxy.$modal.msgError("模型服务部署失败, 无法调用...");
    return;
  } else if (deployStatus != "FINISHED"){
    proxy.$modal.msgWarning("模型服务正在初始化中, 请稍后...");
    return;
  }
  let query = { msId: services.value[index].msId };
  router.push({ path: "/service/invoke", query: query });
}


function deleteService(row) {
  proxy.$modal.confirm("是否删除该服务");
}
</script>

<style scoped lang="scss">

</style>
