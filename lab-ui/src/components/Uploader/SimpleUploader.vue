<template>
  <div class="app-container">
    <uploader
      class="uploader-app"
      :options="initOptions"
      :file-status-text="fileStatusText"
      :auto-start="false"
      @file-added="onFileAdded"
      @file-success="onFileSuccess"
      @file-progress="onFileProgress"
      @file-error="onFileError"
    >
      <uploader-unsupport></uploader-unsupport>

      <uploader-drop>
        <uploader-btn >上传 </uploader-btn>
        <!--<uploader-btn directory>上传文件夹 </uploader-btn>-->
      </uploader-drop>

      <uploader-list></uploader-list>
    </uploader>
  </div>
</template>

<script setup>
import useCurrentInstance from "@/utils/currentInstance";
import { generateMD5 } from "@/components/Uploader/utils/md5";
import { ElNotification } from "element-plus";
import { addFileToDrive } from "@/api/drive/drive";
import { checkAuth } from "@/api/admin/login";
const { proxy } = useCurrentInstance();

// props


// emits
const emits = defineEmits(['uploadSuccess']);


const drivePath = import.meta.env.VITE_APP_DRIVE_API;

const initOptions = {
  target: drivePath + "/file/breakpoint-upload",
  chunkSize: '5242880',
  forceChunkSize: true,
  fileParameterName: 'file',
  maxChunkRetries: 3,
  // 是否开启服务器分片校验
  testChunks: true,
  // 服务器分片校验函数，秒传及断点续传基础
  checkChunkUploadedByResponse: function (chunk, message) {
    let skip = false
    // console.log("checkChunkUploadedByResponse chunk:", chunk);
    // console.log("checkChunkUploadedByResponse message:", message);
    try {
      let objMessage = JSON.parse(message)
      // console.log("objMessage:", objMessage);
      if (objMessage.code === 200) {
        if (objMessage.data.skipUpload) {
          skip = true
        } else if (objMessage.data.missChunks == null){
          skip = false;
        } else {
          skip = (objMessage.data.missChunks || []).indexOf(chunk.offset.toString()) < 0
        }
      }

    } catch (e) {}
    console.log("skip: " + chunk.offset + " " + skip);
    return skip
  },
  query: (file, chunk) => {
    // console.log("query:", file);
    return {
      ...file.params
    }
  }
}

const fileStatusText = {
  success: '上传成功',
  error: '上传失败',
  uploading: '上传中',
  paused: '已暂停',
  waiting: '等待上传'
}

// const uploaderRef = ref()
// const uploader = computed(() => uploaderRef.value?.uploader)

async function onFileAdded(file) {
  // 判断用户是否已经登录了，登录才可以添加
  await checkAuth();

  // 暂停文件
  // 选择文件后暂停文件上传，上传时手动启动
  file.pause()
  // console.log("onFileAdded file: ", file);
  // panelShow.value = true
  // trigger('fileAdded')
  // 将额外的参数赋值到每个文件上，以不同文件使用不同params的需求
  // file.params = customParams.value
  // 计算MD5
  const md5 = await computeMD5(file)
  startUpload(file, md5)
}
function computeMD5(file) {
  // 文件状态设为"计算MD5"
  // statusSet(file.id, 'md5')

  // 计算MD5时隐藏"开始"按钮
  // nextTick(() => {
  //   document.querySelector(`.file-${file.id} .uploader-file-resume`).style.display = 'none'
  // })
  // 开始计算MD5
  return new Promise((resolve, reject) => {
    generateMD5(file, {
      onProgress(currentChunk, chunks) {
        // 实时展示MD5的计算进度
        nextTick(() => {
          const md5ProgressText = '校验MD5 ' + ((currentChunk / chunks) * 100).toFixed(0) + '%'
          console.log("md5计算中:", md5ProgressText)
          // document.querySelector(`/*.custom-status-${file.id}*/`).innerText = md5ProgressText
        })
      },
      onSuccess(md5) {
        // statusRemove(file.id)
        resolve(md5)
      },
      onError() {
        error(`文件${file.name}读取出错，请检查该文件`)
        file.cancel()
        // statusRemove(file.id)
        reject()
      }
    })
  })
}
// md5计算完毕，开始上传
function startUpload(file, md5) {
  file.uniqueIdentifier = md5
  file.resume()
}

function error(msg) {
  ElNotification({
    title: '错误',
    message: msg,
    type: 'error',
    duration: 2000
  })
}

function onFileProgress(rootFile, file, chunk) {
  console.log(
    `上传中 ${file.name}，chunk：${chunk.startByte / 1024 / 1024} ~ ${
      chunk.endByte / 1024 / 1024
    }`
  )
}

const onFileError = (file) => {
  console.log('error', file)
}
const onFileSuccess = (rootFile, file, response, chunk) => {
  // console.log("上传成功")
  // console.log("rootFile",rootFile)
  // file的relativePath是文件夹的相对路径(如果上传的是文件夹的话)
  // console.log("file",file)
  // console.log("response",JSON.parse(response))
  // console.log("chunk",chunk)
  // addFileToDrive(file.name, file.uniqueIdentifier, file.size).then(() => {
  //   proxy.$modal.msgSuccess("文件上传成功");
  // })

  emits("uploadSuccess", file);
}
</script>

<style scoped lang="scss">
.btn{
  background-color: #1ab394;
}
</style>
