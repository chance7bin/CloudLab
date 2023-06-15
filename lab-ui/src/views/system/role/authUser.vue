<template>
  <div class="app-container">
    <!--搜索框-->
    <el-form :model="queryParams" ref="queryRef" v-show="showSearch" :inline="true">
      <el-form-item label="当前角色">
        <el-input
            disabled
            style="width: 120px"
            :placeholder="currentRole"
        />
      </el-form-item>

      <el-form-item label="用户名称" prop="userName">
        <el-input
            v-model="queryParams.userName"
            placeholder="请输入用户名称"
            clearable
            style="width: 240px"
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号码" prop="phonenumber">
        <el-input
            v-model="queryParams.phonenumber"
            placeholder="请输入手机号码"
            clearable
            style="width: 240px"
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="userList">
      <el-table-column label="用户名称" prop="userName" :show-overflow-tooltip="true"/>
      <el-table-column label="用户昵称" prop="nickName" :show-overflow-tooltip="true"/>
      <el-table-column label="邮箱" prop="email" :show-overflow-tooltip="true"/>
      <el-table-column label="手机" prop="phonenumber" :show-overflow-tooltip="true"/>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
              link
              type="primary"
              icon="CircleClose"
              @click="cancelAuthUser(scope.row)"
              v-hasPermi="['system:role:remove']"
          >取消授权
          </el-button
          >
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
import {allocatedUserList, authUserCancel, getRole} from "@/api/admin/role";
import {parseTime} from "@/utils/common";
import useCurrentInstance from "@/utils/currentInstance";
import {ElForm} from "element-plus";

const route = useRoute();
const {proxy} = useCurrentInstance();
const {sys_normal_disable} = proxy.useDict("sys_normal_disable");

const userList = ref([]);
const currentRole = ref("未知角色");
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const queryRef = ref<InstanceType<typeof ElForm>>();

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleId: route.params.roleId,
  userName: undefined,
  phonenumber: undefined
});
getRole(route.params.roleId).then((response: any) => {
  currentRole.value = response.data.roleName;
});
getList();

/** 查询授权用户列表 */
function getList() {
  loading.value = true;
  allocatedUserList(queryParams).then((response: any) => {
    userList.value = response.rows;
    total.value = Number(response.total);
    loading.value = false;
  });
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm(queryRef);
  handleQuery();
}

/** 取消授权按钮操作 */
function cancelAuthUser(row) {
  proxy.$modal
      .confirm('确认要取消该用户"' + currentRole.value + '"的角色吗？')
      .then(function () {
        return authUserCancel({userId: row.userId, roleId: queryParams.roleId});
      })
      .then(() => {
        getList();
        proxy.$modal.msgSuccess("取消授权成功");
      })
      .catch(() => {
      });
}


</script>
