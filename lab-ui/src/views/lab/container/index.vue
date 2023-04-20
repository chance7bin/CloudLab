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
<!--      <el-table-column label="启动时间" prop="started" width="180" />-->
      <el-table-column label="创建时间" prop="created" width="180" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button link type="primary" @click="enterWorkspace(scope.$index, scope.row)">进入</el-button>
          <el-button link type="primary" @click="refresh(scope.$index, scope.row)">刷新</el-button>
          <el-button link type="primary" @click="openCreateEnvDialog(scope.$index, scope.row)">创建新环境</el-button>
<!--          <el-button link type="primary">重启</el-button>-->
          <el-button link type="primary">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getContainerList"
    />



    <el-dialog
        v-model="createEnvDialog"
        title="创建新环境"
        width="15%"
    >

      <el-form
          ref="ruleFormRef"
          :model="ruleForm"
          status-icon
          :rules="rules"
          class="demo-ruleForm"
      >
        <el-form-item label="名称" prop="imageName">
          <el-input v-model="ruleForm.imageName" />
        </el-form-item>
        <el-form-item label="版本" prop="tag">
          <el-input v-model="ruleForm.tag" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createEnvDialog = false">取消</el-button>
          <el-button type="primary" @click="submitForm(ruleFormRef)">
            创建
          </el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script name="container" setup lang="ts">
import {listContainers, createNewEnv} from "@/api/container/container";
import type {NewEnv} from "@/api/container/container";
import useCurrentInstance from "@/utils/currentInstance";
import { ref } from "vue";
import { getWorkspaceByContainerId } from "@/api/container/workspace";
import { notEmptyString } from "@/utils/stringUtils";
import {ElMessageBox, ElMessage} from "element-plus";
import type { FormInstance, FormRules } from 'element-plus'
import {validAlphabets, validateName, validEmail} from "@/utils/validate";

const { proxy } = useCurrentInstance();
const router = useRouter();

const containers = ref<any[]>([]);

const total = ref<number>(0);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10
  }
});
const { queryParams } = toRefs(data);

const createEnvDialog = ref(false)

getContainerList();
function getContainerList() {
  listContainers(queryParams.value).then((res: any) => {
    containers.value = res.rows;
    total.value = Number(res.total);
  });
}

function refresh(index, row) {
  getWorkspaceByContainerId(row.containerId).then((res) => {
    getContainerList();
  });
}


const ruleFormRef = ref<FormInstance>()
const ruleForm = reactive({
  imageName: '',
  tag: '1.0'
})

let createEnvParams : NewEnv = {
  containerId: "",
  envName: "",
  tag: ""
}


// 名称校验器
const nameValidator = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入完整'))
  }
  else {
    let validate = validateName(value);
    if (validate){
      callback();
    } else {
      callback("不能包含非法字符")
    }
    return validate;
  }
}


const rules = reactive<FormRules>({
  imageName: [{ required: true,  validator: nameValidator, trigger: 'blur' }],
  tag: [{ required: true,  validator: nameValidator, trigger: 'blur' }],
})

const submitForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate((valid) => {
    if (valid) {

      createEnvParams.envName = ruleForm.imageName;
      createEnvParams.tag = ruleForm.tag;

      createNewEnv(createEnvParams)
          .then(() => {
            proxy.$modal.msgSuccess("新环境正在初始化，请稍后...");
            createEnvDialog.value = false;
          })
          .catch(() => {
            // createEnvDialog.value = false;
          });

    } else {
      // console.log('error submit!')
      return false
    }
  })
}

function openCreateEnvDialog(index, row) {
  createEnvParams.containerId = row.containerId;
  createEnvDialog.value = true;
}


function enterWorkspace(index, row) {
  // console.log(row)
  let query = { containerId: row.containerId };
  router.push({ path: "/workspace/instance", query: query });
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

.dialog-footer-button {
  /*display: flex;*/
  justify-content: right;
}
</style>
