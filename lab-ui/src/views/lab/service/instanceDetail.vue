<template>
  <div class="app-container">
    <el-skeleton v-if="instanceExist" :rows="5" animated :loading="loading">

      <template #default>
        <!--模型输入参数配置-->
        <div>
          <h1>{{instance.serviceName}}</h1>
          <h3>创建者 : {{instance.serviceCreteBy}}</h3>
          <h3>创建时间 : {{instance.serviceCreateTime}}</h3>

          <div>
            <!--输入参数-->
            <el-alert style="margin-bottom: 10px"  title="输入参数" type="success" :closable="false"/>
            <el-table :data="inputs" stripe style="width: 100%" table-layout="auto">
              <el-table-column type="index" :index="indexMethod" />
              <el-table-column prop="state" label="state" width="180" />
              <el-table-column prop="event" label="event" width="180" />
              <el-table-column prop="dataMIME" label="dataMIME" width="180" />
              <el-table-column label="value" width="200">
                <template #default="scope">
                  <div v-if="scope.row.dataMIME=='file'" class="config-form-item">
                    <el-input v-model="scope.row.value" disabled />
                    <el-button type="success" :icon="Download" circle @click="downloadFile(scope.row.value)"/>
                  </div>
                  <div v-else>
                    <el-input v-model="scope.row.value" disabled />
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <!--输出结果-->
            <el-alert style="margin-bottom: 10px"  title="输出结果" type="warning" :closable="false"/>
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

          <!--模型运行结果部分-->
          <div style="margin-top: 40px;">
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
            <p>用时：{{insInfo['spanTime']}} 秒</p>
          </div>

        </div>
      </template>

    </el-skeleton>
    <el-empty v-else :image-size="200" description="糟糕 X﹏X ... 任务不见了 (；′⌒`)" />
  </div>
</template>

<script setup lang="ts">
import useCurrentInstance from "@/utils/currentInstance";
import { ref } from "vue";
import { getMsInsById } from "@/api/container/msIns";
import { Download} from "@element-plus/icons-vue";
import {checkAuth} from "@/api/admin/user";

const { proxy } = useCurrentInstance();

const route = useRoute();
const insId = route.query && route.query.insId;

const loading = ref<boolean>(true);
const instanceExist = ref<boolean>(true);

proxy.$modal.loading("正在获取实例信息，请稍后...");

let instance = ref();
const insInfo = ref<object>();
const logs = ref<object[]>([]);
const inputs = ref<object[]>([]);
const outputs = ref<object[]>([]);


getMsInsById(insId as string)
  .then((res) => {

    instance.value = res.data;

    // 输入输出参数可视化
    initParams();

    // 运行日志可视化
    initLogs();

    loading.value = false;
    proxy.$modal.closeLoading();
  })
  .catch((res) => {
    instanceExist.value = false;
    proxy.$modal.closeLoading();
  });

// 用于显示的参数信息，如果参数是文件的话存储的是文件名
const params = reactive({});
// 用于存储的参数信息，如果参数是文件的话存储的是文件地址，用于下载
const fileParamsStorage = reactive({});

//输出结果部分
const indexMethod = (index: number) => {
  return index + 1;
}

function initParams() {
  let msrIns = instance.value["msrIns"];
  if (msrIns != null){
    insInfo.value = msrIns;
    logs.value = msrIns["logs"];
    inputs.value = msrIns["inputs"];
    outputs.value = msrIns["outputs"];
  }
}

function initLogs() {

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

// 下载文件
const downloadFile = async (driveFileId) => {
  await checkAuth();
  window.location.href = import.meta.env.VITE_APP_DRIVE_API + "/file/download/" + driveFileId;
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
