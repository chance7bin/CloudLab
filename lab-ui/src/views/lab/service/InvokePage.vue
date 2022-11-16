<template>
  <div class="app-container">
    <el-skeleton v-if="serviceExist" :rows="5" animated :loading="loading">
      <template #default>
        <!--模型输入参数配置-->
        <div>
          <h1>{{mdlModelClass.name}}</h1>
          <h3>描述 : {{mdlModelClass.attributeSet.description.name}}</h3>
          <h3>创建者 : {{modelService.createBy}}</h3>
          <div>
            <!--输入参数-->
            <h3 style="background-color: #f0f9eb; padding: 10px; color: #67c23a; font-weight: bold;">
              <el-icon class="header-icon" style="vertical-align: middle">
                <Setting />
              </el-icon>
              参数设置</h3>
            <el-form
              ref="ruleFormRef"
              :model="params"
              :rules="rules"
              label-width="120px"
              class="config-form"
              size="default"
              status-icon
            >
              <el-collapse v-model="collapseAll" v-for="(state, index) in mdlModelClass.behavior.stateGroup.states" :key="index">
                <el-collapse-item :name="state.id">
                  <template #title>
                    <div style="color:#409eff; font-size: 16px; font-weight: bold;">
                      <!--<el-icon class="header-icon">-->
                      <!--  <info-filled />-->
                      <!--</el-icon>-->
                      {{ state.name }}</div>
                  </template>
                  <el-alert style="margin-bottom: 10px" v-if="state.description" :title="state.description" type="info" show-icon :closable="false"/>

                  <div v-for="(event, index) in state.events" :key="index">
                    <el-form-item v-if="event.interaction" :label="event.name" :prop="event.inputParameter.id">
                      <div v-if="event.inputParameter.dataMIME == 'text'" class="config-form-item">
                        <el-input v-model="params[event.inputParameter.id]" />
                      </div>
                      <div v-else class="config-form-item">
                        <el-input v-model="params[event.inputParameter.id]" disabled />
                        <el-button type="warning" :icon="FolderOpened" circle @click="selectFile(event.inputParameter.id)"/>
                      </div>
                    </el-form-item>
                  </div>


                </el-collapse-item>
              </el-collapse>
            </el-form>
          </div>
        </div>

        <el-button style="margin-top: 20px; float: right" type="primary" round plain @click="invoke">运行</el-button>

      </template>
    </el-skeleton>
    <el-empty v-else :image-size="200" description="糟糕 X﹏X ... 模型服务不见了 (；′⌒`)" />


    <el-dialog class="config-dialog" v-model="fileDialogVisible" width="20%" title="选择文件" draggable>
      <file-select-modal
        :container-name="containerName"
        @selectedItem="fillInputValue"
      ></file-select-modal>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="confirmInputValue"> 确定 </el-button>
        </div>
      </template>
    </el-dialog>
  </div>

</template>

<script setup lang="ts">
import useCurrentInstance from "@/utils/currentInstance";
import { getModelServiceById, invokeService } from "@/api/msc";
import { ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { FolderOpened } from "@element-plus/icons-vue";
import { getJupyterContainerById } from "@/api/docker";

const { proxy } = useCurrentInstance();
const route = useRoute();
const msId = route.query && route.query.msId;

const loading = ref<boolean>(true);
const serviceExist = ref<boolean>(true);
proxy.$modal.loading();

const collapseAll = ref<string[]>([]);
const modelService = ref({});
//模型mdl对象
const mdlModelClass = ref({});

//参数设置
const containerName = ref("");
const fileDialogVisible = ref(false);
const ruleFormRef = ref<FormInstance>();

// 用于显示的form信息，如果输入参数是文件的话存储的是文件名
const params = reactive({});
// 用于传输的form信息，如果输入的参数是文件的话存储的是文件地址
const fileParamsStorage = reactive({});
const rules = reactive<FormRules>({
  // serviceName: [{ required: true, message: "请输入服务名称", trigger: "change" }],
  // mdlFile: [{ required: true, message: "请选择mdl文件", trigger: "change" }],
  // encapsulationFile: [{ required: true, message: "请选择封装脚本", trigger: "change" }]
});

getModelServiceById(msId as string)
  .then(res => {
    // console.log("getModelServiceById:", res);
    modelService.value = res.data;
    mdlModelClass.value = res.data.modelClass;
    // console.log("modelClass:", mdlModelClass.value);

    //参数设置页面初始化
    initParams();

    loading.value = false;
    proxy.$modal.closeLoading();
  })
  .catch(res => {
    serviceExist.value = false;
    proxy.$modal.closeLoading();
  });


function initParams() {
  const states = mdlModelClass.value["behavior"].stateGroup.states;
  for (const state of states) {

    //默认展开所有面板
    collapseAll.value.push(state["id"]);

    for (const event of state["events"]) {

      if (event["interaction"]){
        // 设置params
        // {id: value}
        params[event["inputParameter"]["id"]] = "";
        fileParamsStorage[event["inputParameter"]["id"]] = "";
        const required = event["required"];
        rules[event["inputParameter"]["id"]] = required ?
          [{ required: true, message: "该参数未设置", trigger: "change" }] :
          [{ required: false}]
      }

    }
  }
  // console.log("params:", params);
  // console.log("rules:", rules);

  getJupyterContainerById(modelService.value["containerId"]).then(res => {containerName.value = res.data["containerName"]});

}


// 选择参数
// 当前选择的文件属于哪个步骤
let currentEvent = "";
let currentSelectedItem = {};
const selectFile = (key) => {
  fileDialogVisible.value = true;
  currentEvent = key;
  // currentEvent = "mdlFile";
}
const fillInputValue = (data) => {
  // console.log("fillInputValue:", data);
  currentSelectedItem = data;
}
const confirmInputValue = () => {
  params[currentEvent] = currentSelectedItem["label"];
  fileParamsStorage[currentEvent] = currentSelectedItem["relativePath"];
  fileDialogVisible.value = false;
}


// 运行服务
const invoke = async () => {
  const valid = await submitForm(ruleFormRef.value);
  if (valid){

    //将输入数据绑定到mdl中
    copyData2Mdl();

    invokeService(modelService.value);

    proxy.$modal.alert("运行");


  } else {
    proxy.$modal.alertError("服务配置未填写完整");
  }
}

//提交表单校验
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  return await formEl.validate();
};


//将模型运行参数赋值到mdl中
const copyData2Mdl = () => {
  const states = mdlModelClass.value["behavior"].stateGroup.states;
  for (const state of states) {
    for (const event of state["events"]) {
      if (event["interaction"]){
        // console.log("inputParameter value:",fileParamsStorage[event["inputParameter"]["id"]]);
        const parameter = event["inputParameter"];
        if (parameter["dataMIME"] == "text"){
          parameter["value"] = params[event["inputParameter"]["id"]];
        } else {
          parameter["value"] = fileParamsStorage[event["inputParameter"]["id"]]
        }
      }
    }
  }

}


</script>

<style scoped lang="scss">
.config-form-item {
  display: flex;
  > * {
    margin-right: 10px;
  }
}
.dialog-footer button:first-child {
  margin-right: 10px;
}
</style>