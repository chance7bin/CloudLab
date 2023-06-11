<template>
  <div class="app-container">

    <!--搜索-->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
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
      <el-form-item label="状态" prop="status">
        <el-select
            v-model="queryParams.status"
            placeholder="用户状态"
            clearable
            style="width: 240px"
        >
          <el-option
              v-for="dict in sys_normal_disable"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" style="width: 308px;font-weight: bold;">
        <el-date-picker
            v-model="dateRange"
            value-format="YYYY-MM-DD"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!--用户数据-->
    <el-table v-loading="loading" :data="userList" >
      <el-table-column :label="columns[0].label" align="center" width="150" key="userId" prop="userId" v-if="columns[0].visible" />
      <el-table-column
        :label="columns[1].label"
        align="center"
        key="userName"
        width="150"
        prop="userName"
        v-if="columns[1].visible"
        :show-overflow-tooltip="true"
      />
      <el-table-column
          :label="columns[5].label"
          align="center"
          key="email"
          prop="email"
          width="180"
          v-if="columns[5].visible"
          :show-overflow-tooltip="true"
      />
      <el-table-column
        :label="columns[2].label"
        align="center"
        key="phonenumber"
        prop="phonenumber"
        v-if="columns[2].visible"
        width="150"
      />
      <el-table-column :label="columns[3].label" align="center" width="150" key="status" v-if="columns[3].visible">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column :label="columns[4].label" align="center" prop="createTime" v-if="columns[4].visible" width="210">
        <template #default="scope">
          <span>{{ scope.row.createTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作"  class-name="small-padding fixed-width">
        <template #default="scope">
          <el-tooltip content="修改" placement="top" v-if="scope.row.userId != 1">
            <el-button
              link
              type="primary"
              icon="Edit"
              @click="handleUpdate(scope.row)"
            ></el-button>
          </el-tooltip>
          <el-tooltip content="重置密码" placement="top" v-if="scope.row.userId != 1">
            <el-button
              link
              type="primary"
              icon="Key"
              @click="handleResetPwd(scope.row)"
            ></el-button>
          </el-tooltip>
          <el-tooltip content="删除" placement="top" v-if="scope.row.userId != 1">
            <el-button
                link
                type="primary"
                icon="Delete"
                @click="handleDelete(scope.row)"
            ></el-button>
          </el-tooltip>
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

    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="userRef" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户昵称" prop="nickName">
              <el-input v-model="form.userName" placeholder="请输入用户昵称" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phonenumber">
              <el-input v-model="form.phonenumber" placeholder="请输入手机号码" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户性别">
              <el-select v-model="form.sex" placeholder="请选择">
                <el-option
                    v-for="dict in sys_user_sex"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!--新增用户-->
        <!--<el-row>-->
        <!--  <el-col :span="12">-->
        <!--    <el-form-item v-if="form.userId == undefined" label="用户名称" prop="userName">-->
        <!--      <el-input v-model="form.userName" placeholder="请输入用户名称" maxlength="30" />-->
        <!--    </el-form-item>-->
        <!--  </el-col>-->
        <!--  <el-col :span="12">-->
        <!--    <el-form-item v-if="form.userId == undefined" label="用户密码" prop="password">-->
        <!--      <el-input v-model="form.password" placeholder="请输入用户密码" type="password" maxlength="20" show-password />-->
        <!--    </el-form-item>-->
        <!--  </el-col>-->
        <!--</el-row>-->
        <el-row>
          <el-col :span="12">
            <el-form-item label="角色">
              <el-select v-model="form.roleIds" multiple placeholder="请选择">
                <el-option
                    v-for="item in roleOptions"
                    :key="item.roleId"
                    :label="item.roleName"
                    :value="item.roleId"
                    :disabled="item.status == 1"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio
                    v-for="dict in sys_normal_disable"
                    :key="dict.value"
                    :label="dict.value"
                >{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>


  </div>
</template>

<script setup lang="ts">
import useCurrentInstance from "@/utils/currentInstance";
import { changeUserStatus, getUser, listUser, delUser, resetUserPwd, updateUser } from "@/api/admin/user";
import type { FormInstance, FormRules } from "element-plus";
const { proxy } = useCurrentInstance();
const { sys_normal_disable, sys_user_sex } = proxy.useDict("sys_normal_disable", "sys_user_sex");
// 列表信息
const columns = ref([
  { key: 0, label: `用户编号`, visible: true },
  { key: 1, label: `用户名称`, visible: true },
  { key: 4, label: `手机号码`, visible: true },
  { key: 5, label: `状态`, visible: true },
  { key: 6, label: `创建时间`, visible: true },
  { key: 2, label: `邮箱`, visible: true },
  { key: 3, label: `角色`, visible: true }
]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phonenumber: undefined,
    status: undefined,
  },
  rules: {
    userName: [
      { required: true, message: "用户名称不能为空", trigger: "blur" },
      { min: 2, max: 20, message: "用户名称长度必须介于 2 和 20 之间", trigger: "blur" }
    ],
    password: [
      { required: true, message: "用户密码不能为空", trigger: "blur" },
      { min: 5, max: 20, message: "用户密码长度必须介于 5 和 20 之间", trigger: "blur" }
    ],
    email: [{ type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }],
    phonenumber: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }]
  }
});
const userRef = ref<FormInstance>()
const queryRef = ref<FormInstance>()
const { queryParams, form, rules } = toRefs(data);
const title = ref("");
const userList = ref([]);
const roleOptions = ref([]);
const loading = ref(true);
const total = ref(0);
const open = ref(false);
const dateRange = ref([]);
const showSearch = ref(true);

getList();

/** 查询用户列表 */
function getList() {
  loading.value = true;
  listUser(proxy.addDateRange(queryParams.value, dateRange.value)).then((res : any) => {
    loading.value = false;
    userList.value = res.rows;
    total.value = Number(res.total);
  });
}

/** 用户状态修改  */
function handleStatusChange(row) {
  let text = row.status === "0" ? "启用" : "停用";
  proxy.$modal
    .confirm('确认要' + text + '"' + row.userName + '"用户吗?')
    .then(function () {
      return changeUserStatus(row.userId, row.status);
    })
    .then(() => {
      proxy.$modal.msgSuccess(text + "成功");
    })
    .catch(function () {
      row.status = row.status === "0" ? "1" : "0";
    });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const userId = row.userId;
  getUser(userId).then((response : any) => {
    form.value = response.data;
    roleOptions.value = response.roles;
    form.value["roleIds"] = response.roleIds;
    open.value = true;
    title.value = "修改用户";
  });
}

/** 重置操作表单 */
function reset() {
  form.value = {
    userId: undefined,
    userName: undefined,
    password: undefined,
    phonenumber: undefined,
    email: undefined,
    sex: undefined,
    status: "0",
    remark: undefined,
    roleIds: []
  };
  proxy.resetForm(userRef);
}

/** 删除按钮操作 */
function handleDelete(row) {
  // ids是批量删除的id数组
  // const userIds = row.userId || ids.value;
  const userIds = row.userId;
  proxy.$modal
    .confirm('是否确认删除用户编号为"' + userIds + '"的用户？')
    .then(function () {
      return delUser(userIds);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess("删除成功");
    })
    .catch(() => {});
}

/** 重置密码按钮操作 */
function handleResetPwd(row) {
  proxy.$prompt('请输入"' + row.userName + '"的新密码', "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    closeOnClickModal: false,
    inputPattern: /^.{5,20}$/,
    inputErrorMessage: "用户密码长度必须介于 5 和 20 之间",
  }).then(({ value }) => {
    resetUserPwd(row.userId, value).then(response => {
      proxy.$modal.msgSuccess("修改成功，新密码是：" + value);
    });
  }).catch(() => {});
};

// TODO 新增用户
/** 提交按钮 */
function submitForm() {
  // proxy.$refs["userRef"].validate(valid => {
    userRef.value?.validate((valid: boolean) => {
    if (valid) {
      // if (form.value.userId != undefined) {
        updateUser(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      // } else {
      //   addUser(form.value).then(response => {
      //     proxy.$modal.msgSuccess("新增成功");
      //     open.value = false;
      //     getList();
      //   });
      // }
    }
  });
};

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
};

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  // 表单重置并且移除校验结果(el-form-item必须有prop与表单里文本框v-model对应)
  proxy.resetForm(queryRef);
  handleQuery();
};

</script>

<style scoped lang="scss"></style>
