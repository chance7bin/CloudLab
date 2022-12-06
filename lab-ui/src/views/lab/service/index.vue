<template>
  <div class="app-container">
    <!--步骤条-->
    <el-steps :active="active" finish-status="success" simple>
      <el-step title="工作空间" :icon="HomeFilled" />
      <el-step title="相关文件" :icon="UploadFilled" />
      <el-step title="服务配置" :icon="Edit" />
    </el-steps>

    <!--工作空间选择-->
    <div v-show="active == 0">
      <el-card class="box-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span style="font-weight: bold">选择工作空间作为服务环境</span>
            <!--<el-button class="button" text>Operation button</el-button>-->
            <el-tag effect="dark" style="margin-left: 20px" :type="selectedContainer == '' ? 'info' : ''">
              {{ selectedContainer == "" ? "未选择" : selectedContainer }}
            </el-tag>
          </div>
        </template>
        <!--容器列表-->
        <el-table :data="containers" stripe highlight-current-row @current-change="handleCurrentContainer">
          <el-table-column label="容器名称" prop="containerName" show-overflow-tooltip width="360" />
          <el-table-column label="镜像名称" prop="imageName" width="200" />
          <el-table-column label="容器状态" prop="status" />
        </el-table>
      </el-card>
    </div>

    <!--相关文件选择-->
    <div v-show="active == 1">
      <el-card class="box-card" shadow="hover">
        <div>
          <my-folder style="border-radius: 15px; box-shadow: rgba(0, 0, 0, 0.35) 0px 5px 15px"></my-folder>
        </div>
      </el-card>
    </div>

    <!--配置文件-->
    <div v-show="active == 2">
      <el-card class="box-card" shadow="hover">
        <el-form
          ref="ruleFormRef"
          :model="configForm"
          :rules="rules"
          label-width="120px"
          class="config-form"
          :size="formSize"
          status-icon
        >
          <el-form-item label="服务名称" prop="serviceName">
            <div class="config-form-item">
              <el-input v-model="configForm.serviceName" />
            </div>
          </el-form-item>
          <el-form-item label="mdl文件" prop="mdlFile">
            <div class="config-form-item">
              <el-input v-model="configForm.mdlFile" disabled />
              <el-button type="warning" :icon="FolderOpened" circle @click="selectMdlFile" />
            </div>
          </el-form-item>
          <el-form-item label="封装脚本" prop="encapsulationFile">
            <div class="config-form-item">
              <el-input v-model="configForm.encapsulationFile" disabled />
              <el-button type="warning" :icon="FolderOpened" circle @click="selectEncapsulationFile" />
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
    <el-dialog class="config-dialog" v-model="configDialogVisible" width="20%" title="选择文件" draggable>
      <file-select-modal
        :container-name="deployPackage.containerName"
        :container-id="deployPackage.containerId"
        @selectedItem="fillInputValue"
      ></file-select-modal>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="confirmInputValue"> 确定 </el-button>
        </div>
      </template>
    </el-dialog>

    <div class="step-control">
      <el-button style="margin-top: 12px" @click="previous" v-show="active > 0" >上一步</el-button>
      <el-button style="margin-top: 12px" @click="next" type="primary">{{active === 2 ? "创建" : "下一步"}}</el-button>
      <!--<el-button style="margin-top: 12px" @click="testInvoke" type="warning">测试</el-button>-->
    </div>
  </div>
</template>

<script setup lang="ts">
import useCurrentInstance from "@/utils/currentInstance";
import { Edit, HomeFilled, UploadFilled, FolderOpened } from "@element-plus/icons-vue";

const { proxy } = useCurrentInstance();
import { listContainers } from "@/api/container/docker";
import type { FormInstance, FormRules } from "element-plus";
import { ElMessage, ElMessageBox } from "element-plus";
import MyFolder from "@/components/Drive/MyFolder.vue";
import { ref } from "vue";
import { createModelService } from "@/api/container/modelService";
import { useRouter } from "vue-router";
const router = useRouter();

interface Config {
  serviceName: string;
  mdlFile: string;
  encapsulationFile: string;
}

interface DeployPackage {
  containerId: string | null;
  containerName: string;
  relativeDir: any;
  config: Config;
}
const deployPackage: DeployPackage = {
  containerId: null,
  containerName: "",
  relativeDir: null,
  config: {
    serviceName: "",
    mdlFile: "",
    encapsulationFile: ""
  }
};

// 控制tab切换
const active = ref<number>(0);
const previous = () => {
  if (active.value <= 0) {
    return;
  } else {
    active.value--;
  }
};

const next = () => {
  if (active.value == 2) {
    //所有步骤都完成了,创建服务
    createService();
    return;
  }
  if (active.value > 2) {
    return;
  } else {
    // 判断该tab是否已经填入参数
    switch (active.value) {
      case 0: {
        if (deployPackage.containerName == "") {
          proxy.$modal.alertWarning("请选择一个工作空间作为服务环境");
          return;
        }
        break;
      }
      case 1: {
        // if (deployPackage.file == null){
        //   proxy.$modal.msgWarning("请选择模型相关文件")
        //   return;
        // }
        break;
      }
      case 2: {
        if (deployPackage.config.serviceName == "") {
          proxy.$modal.msgWarning("请输入模型服务名称");
          return;
        }
        if (deployPackage.config.mdlFile == "") {
          proxy.$modal.msgWarning("请选择mdl文件");
          return;
        }
        if (deployPackage.config.encapsulationFile == "") {
          proxy.$modal.msgWarning("请选择封装文件");
          return;
        }
        break;
      }
    }

    active.value++;
  }
};

// 容器列表
const containers = ref<any[]>([]);
listContainers().then((res) => {
  containers.value = res.data;
  // console.log("images:", images.value);
});

const selectedContainer = ref<string>("");
const handleCurrentContainer = (val) => {
  // console.log("val:", val);
  selectedContainer.value = val.containerName;
  proxy.$modal.msgSuccess("已选择: " + selectedContainer.value);
  deployPackage.containerName = selectedContainer.value;
  deployPackage.containerId = val.containerId;
};

//配置文件设置
const configDialogVisible = ref(false);
const formSize = ref("default");
const ruleFormRef = ref<FormInstance>();
const configForm = reactive({
  serviceName: "",
  mdlFile: "",
  mdlFilePath: "",
  encapsulationFile: "",
  encapsulationFilePath: ""
});

// 服务名称校验器
const nameValidator = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入服务名称'))
  }
  else {
    let reg = new RegExp('[\\\\/:*?"<>|]');
    let validate = !reg.test(value);
    if (validate){
      callback();
    } else {
      callback("服务名称不能包含非法字符")
    }
    return !reg.test(value);
  }
}

const rules = reactive<FormRules>({
  serviceName: [{ required: true, validator: nameValidator, trigger: "change" }],
  mdlFile: [{ required: true, message: "请选择mdl文件", trigger: "change" }],
  encapsulationFile: [{ required: true, message: "请选择封装脚本", trigger: "change" }]
});
// 当前选择的文件属于哪个步骤
let currentEvent = "";
let currentSelectedItem = {};
const selectMdlFile = () => {
  // deployPackage.containerName = "jupyter_cus_5.0_8268889755334766592"
  configDialogVisible.value = true;
  currentEvent = "mdlFile";
}
const selectEncapsulationFile = () => {
  // deployPackage.containerName = "jupyter_cus_5.0_8268889755334766592"
  configDialogVisible.value = true;
  currentEvent = "encapsulationFile";
}
const fillInputValue = (data) => {
  // console.log("fillInputValue:", data);
  currentSelectedItem = data;
}
const confirmInputValue = () => {
  switch (currentEvent) {
    case "mdlFile":{
      configForm.mdlFile = currentSelectedItem["label"];
      configForm.mdlFilePath = currentSelectedItem["relativePath"];
      break;
    }
    case "encapsulationFile":{
      configForm.encapsulationFile = currentSelectedItem["label"];
      configForm.encapsulationFilePath = currentSelectedItem["relativePath"];
      break;
    }
  }
  configDialogVisible.value = false;
}


//提交表单校验
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  return await formEl.validate((valid, fields) => {
    if (valid) {
      // console.log('submit!', configForm.serviceName)
      // 填写服务配置信息
      // deployPackage.config.serviceName = configForm.serviceName;
      // deployPackage.config.mdlFile = configForm.mdlFile;
      // deployPackage.config.encapsulationFile = configForm.encapsulationFile;
    } else {
      proxy.$modal.alertError("服务配置未填写完整");
      // console.log('error submit!', fields)
      return false;
    }
    // return valid;
  });
};

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.resetFields();
};

//创建模型服务
const createService = async () => {
  const valid = await submitForm(ruleFormRef.value);
  // console.log("createService:", valid);

  // 服务配置表单验证成功
  if (valid) {
    let msg = confirmParams();
    if (msg == "true") {

      const deployPackageDTO = {
        containerId: deployPackage.containerId,
        containerName: deployPackage.containerName,
        msName: configForm.serviceName,
        relativeDir: deployPackage.relativeDir,
        mdlFilePath: configForm.mdlFilePath,
        encapScriptPath: configForm.encapsulationFilePath
      };

      proxy.$modal.confirm("是否发布模型服务")
        .then(() => {

          // console.log("deployPackageDTO:", deployPackageDTO);
          createModelService(deployPackageDTO)
            .then(res => {
              ElMessage({
                type: 'success',
                message: '服务发布成功！',
              })

              router.push({path: "/lab/serviceList"});
            });
        })
        .catch(() => {
          ElMessage({
            type: 'info',
            message: '取消操作',
          })
        })



    } else {
      proxy.$modal.alertError(msg);
    }
  }
};

//双重检验前面的参数是否都填了
const confirmParams: () => string = () => {
  if (deployPackage.containerName == "") {
    return "未选择工作空间";
  }
  return "true";
};

</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  //justify-content: space-between;
  align-items: center;
}

.text {
  font-size: 14px;
}

.item {
  margin-bottom: 18px;
}

.box-card {
  //width: 480px;
  //display: flex;
  //justify-content: center;
}

.step-control {
  display: flex;
  justify-content: right;
}
.config-form {
  width: 30%;
  margin: auto;
}
.config-form-item {
  display: flex;
  > * {
    margin-right: 10px;
  }
}
.config-dialog {
  //width: 30vw;
}
.dialog-footer button:first-child {
  margin-right: 10px;
}
</style>
