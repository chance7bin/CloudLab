<template>
  <div>
    <i-frame v-if="url!==''" v-model:src="url"></i-frame>
    <el-empty v-else :image-size="200" description="糟糕 X﹏X ... 工作空间不见了 (；′⌒`)" />
  </div>
</template>

<script setup lang="ts">
import iFrame from '@/components/iFrame/index.vue'
import { getWorkspaceByContainerId } from "@/api/container/workspace";
import { notEmptyString } from "@/utils/stringUtils";
import useCurrentInstance from "@/utils/currentInstance";
const { proxy } = useCurrentInstance();
const route = useRoute();

const containerId = route.query && route.query.containerId;

// console.log("containerId: ",containerId);

// const url = ref(import.meta.env.VITE_APP_BASE_API + "/swagger-ui/index.html")
// const url = ref("http://127.0.0.1:7006/lab?token=66666")
const url = ref<string>("")

proxy.$modal.loading("正在初始化工作空间，请稍后...");

getWorkspaceByContainerId(containerId as string)
  .then((res) => {
    if (notEmptyString(res.data.containerName)){
      let container = res.data;
      // url.value = "http://172.21.212.240:" + container.hostBindPort + "/lab?token=" + container.jupyterToken;
      url.value = "http://" + container.hostIP + ":" + container.hostBindPort + "/lab?token=" + container.jupyterToken;
      // console.log("jupyter url:",url.value);
    }
    proxy.$modal.closeLoading();
  }

)


</script>
