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

              <!--输出结果-->
              <div>
                <el-alert style="margin-bottom: 10px"  title="输出结果" type="success" :closable="false"/>
                <div v-if="showResult">
                  <el-table :data="outputs" stripe style="width: 100%" table-layout="auto">
                    <el-table-column type="index" :index="indexMethod" />
                    <el-table-column prop="state" label="state" width="180" />
                    <el-table-column prop="event" label="event" width="180" />
                    <el-table-column prop="dataMIME" label="dataMIME" width="180" />
                    <el-table-column label="result" width="200">
                      <template #default="scope">
                        <div v-if="scope.row.dataMIME=='file'" class="config-form-item">
                          <el-input v-model="scope.row.driveFileId" disabled />
                          <el-button type="success" :icon="Download" circle @click="downloadFile(scope.row.driveFileId)"/>
                        </div>
                        <div v-else>
                          <el-input v-model="scope.row.value" disabled />
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>

            </el-form>
          </div>
        </div>

        <el-button style="margin-top: 20px; float: right" type="primary" round plain @click="invoke" :disabled="forbidInvoke">运行</el-button>

        <!--模型运行结果部分-->
        <div style="margin-top: 40px;" v-if="showProcess">
          <h3>模型运行日志</h3>
          <!--<el-progress :percentage="30" :indeterminate="true" :duration="2" status="success"/>-->
          <!--日志信息-->
          <div v-if="logs.length != 0" >
            <ul>
              <li
                v-for='(log, index) in logs'
                :style="logColor(log)"
                style="white-space: pre-wrap;"
              >
                {{ log.type }}
                {{ log.state != null ? ' -- ' + log.state : '' }}
                {{ log.event != null ? ' -- ' + log.event : '' }}
                {{ log.message != null ? ' -- ' + log.message : '' }}
              </li>
            </ul>
          </div>
          <p v-if="showResult">用时：{{insInfo['spanTime']}} 秒</p>
        </div>

      </template>
    </el-skeleton>
    <el-empty v-else :image-size="200" description="糟糕 X﹏X ... 模型服务不见了 (；′⌒`)" />


    <el-dialog class="config-dialog" v-model="fileDialogVisible" width="70%" title="选择文件" draggable>
      <!--<file-select-modal-->
      <!--  :container-name="containerName"-->
      <!--  :container-id="containerId"-->
      <!--  @selectedItem="fillInputValue"-->
      <!--&gt;</file-select-modal>-->
      <div class="drive-layout">
        <my-folder
            @selectedItem="fillInputValue"
        ></my-folder>
      </div>
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
import { getModelServiceById, invokeService } from "@/api/container/modelService";
import {getMsInsById} from "@/api/container/msIns";
import { onBeforeUnmount, ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { FolderOpened , Download} from "@element-plus/icons-vue";
import { checkAuth } from "@/api/admin/user";
import { ElMessageBox } from "element-plus";
import type {InvokeDTO} from "@/api/container/modelService";


const { proxy } = useCurrentInstance();

const route = useRoute();
const msId = route.query && route.query.msId;

let timer;
onBeforeUnmount(() => {
  // 在页面销毁时，销毁定时器
  clearInterval(timer)
})


const loading = ref<boolean>(true);
const serviceExist = ref<boolean>(true);
proxy.$modal.loading("正在初始化服务，请稍后...");

const collapseAll = ref<string[]>([]);
const modelService = ref();
//模型mdl对象
const mdlModelClass = ref({});

//参数设置
const containerName = ref("");
const containerId = ref("");
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

    if (modelService.value["deployStatus"] == "ERROR"){
      proxy.$modal.msgError("模型服务部署失败, 无法调用...");
    } else if (modelService.value["deployStatus"] != "FINISHED"){
      proxy.$modal.msgWarning("模型服务正在初始化中, 请稍后...");
    }

    //参数设置页面初始化
    initParams();

    loading.value = false;
    proxy.$modal.closeLoading();
  })
  .catch(res => {
    // console.log(res);
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
        // {parameter id 标识: value}
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

  // getJupyterContainerById(modelService.value["containerId"]).then(res => {
  //   containerName.value = res.data["containerName"]
  //   containerId.value = res.data["containerId"]
  // });

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
  params[currentEvent] = currentSelectedItem["filename"];
  fileParamsStorage[currentEvent] = currentSelectedItem["driveFileId"];
  // fileParamsStorage[currentEvent] = currentSelectedItem["fileId"];
  fileDialogVisible.value = false;
}


// 运行服务
const forbidInvoke = ref(false);
let currentInsId = "";
const showProcess = ref(false);
const showResult = ref(false);
const insInfo = ref<object>();
const logs = ref<object[]>([]);
const outputs = ref<object[]>([]);

const invoke = async () => {

  if (modelService.value["deployStatus"] == "ERROR"){
    proxy.$modal.msgError("模型服务部署失败, 无法调用...");
    return;
  } else if (modelService.value["deployStatus"] != "FINISHED"){
    proxy.$modal.msgWarning("模型服务正在初始化中, 请稍后...");
    return;
  }

  const valid = await submitForm(ruleFormRef.value);
  if (valid){

    //将输入数据绑定到mdl中
    copyData2Mdl();


    ElMessageBox({
      title: "系统提示",
      message: "是否开始执行任务",
      showCancelButton: true,
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
      beforeClose: (action, instance, done) => {
        if (action === "confirm") {
          instance.confirmButtonLoading = true;
          instance.confirmButtonText = "任务初始化中...";

          let invokeDTO : InvokeDTO = {
            msId: modelService.value.msId,
            imageId: modelService.value.imageId,
            msName: modelService.value.msName,
            modelClass: modelService.value.modelClass,
            deployStatus: modelService.value.deployStatus,
            serviceType: modelService.value.serviceType
          }

          invokeService(invokeDTO)
            .then((res) => {
              proxy.$modal.notifySuccess("开始运行");
              done();
              setTimeout(() => {
                instance.confirmButtonLoading = false;
              }, 300);

              // forbidInvoke.value = true;
              // console.log("invokeService: ",data["data"]);
              showProcess.value = true;
              showResult.value = false;
              currentInsId = res["data"];

              // getMsInsById(currentInsId).then((data) => {
              //   console.log("getMsInsById: ",data);
              // });

              // 运行服务后开启定时器，定时请求该任务的实例信息
              timer = setInterval(()=>{
                // 这里调用调用需要执行的方法
                getMsInsById(currentInsId).then((res) => {
                  // console.log("getMsInsById: ",res);
                  let msrIns = res["data"]["msrIns"];

                  if (msrIns != null){
                    insInfo.value = msrIns;
                    logs.value = msrIns["logs"];
                    outputs.value = msrIns["outputs"];
                  }

                  //任务结束删除定时任务
                  if (msrIns == null || msrIns["status"] == "FINISHED" || msrIns["status"] == "ERROR"){
                    clearInterval(timer)
                    showResult.value = true;
                  }
                });
              }, 1000) // 每1秒执行1次
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

  } else {
    proxy.$modal.alertError("服务配置未填写完整");
  }
}

// getMsInsById("8d317950-32cd-4c61-a7fd-094865f2be89").then((data) => {
//   console.log("getMsInsById: ",data);
// });

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


//模型运行结果
const startTaskListener = () => {

}

const logColor = (log) => {
  let color = "#67c23a";
  if (log['dataFlag'] == 'ERROR'){
    color = "#f56c6c"
  }
  if (log["type"] == "ON_POST_ERROR_INFO") {
    color = "#f56c6c"
  }
  if (log["type"] == "ON_POST_WARNING_INFO") {
    color = "#e6a23c"
  }
  if (log["type"] == "ON_POST_MESSAGE_INFO") {
    color = "#909399"
  }

  return "color:" + color;
}

//输出结果部分
const indexMethod = (index: number) => {
  return index + 1;
}

// 下载文件
const downloadFile = async (driveFileId) => {
  await checkAuth();
  let downloadUrl = import.meta.env.VITE_APP_DRIVE_API + "/file/download/" + driveFileId;
  window.location.href = downloadUrl;
  // proxy.download(downloadUrl);
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

$layout-height: 60vh;
.drive-layout{
  min-height: $layout-height;
  max-height: $layout-height;
  overflow: scroll;
  overflow-x: hidden;
  border: none;
  border-radius: 15px;
  box-shadow: rgba(0, 0, 0, 0.35) 0px 5px 15px;
}
</style>