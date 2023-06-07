<template>
  <div id="global-uploader">
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
        <uploader-btn >选择文件</uploader-btn>
        <span style="margin-left: 10px">（支持上传一个或多个文件）</span>
        <!--<uploader-btn directory>上传文件夹 </uploader-btn>-->
      </uploader-drop>
      <!--<uploader-btn id="global-uploader-btn" ref="uploadBtnRef">选择文件</uploader-btn>-->
      <!--<span>（支持上传一个或多个文件）</span>-->


      <uploader-list>
        <template #default="{ fileList }">
          <div class="file-panel">
            <!--<div class="file-title">-->
            <!--  <div class="title">文件列表</div>-->
            <!--</div>-->

            <ul class="file-list">
              <li
                  v-for="file in fileList"
                  :key="file.id"
                  class="file-item"
              >
                <uploader-file
                    ref="files"
                    :class="['file_' + file.id, customStatus]"
                    :file="file"
                    :list="true"
                ></uploader-file>
              </li>
              <div v-if="!fileList.length" class="no-file">
                <!--<Icon icon="ri:file-3-line" width="16" /> 暂无待上传文件-->
                暂无待上传文件
              </div>
            </ul>
          </div>
        </template>
      </uploader-list>
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


// TODO 上传组件还有bug 上传成功时动作按钮没有隐藏；后端出现错误上传失败时背景色没变红

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
    // console.log("skip: " + chunk.offset + " " + skip);
    return skip
  },
  query: (file, chunk) => {
    // console.log("query:", file);
    return {
      ...file.params
    }
  }
}

const customStatus = ref('')

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
  statusSet(file.id, 'md5')

  // 计算MD5时隐藏"开始"按钮
  nextTick(() => {
    // document.querySelector(`.file_${file.id} .uploader-file-resume`).style.display = 'none'
    document.querySelector(`.file_${file.id} .uploader-file-actions`).style.display = 'none'
  })
  // 开始计算MD5
  return new Promise((resolve, reject) => {
    generateMD5(file, {
      onProgress(currentChunk, chunks) {
        // 实时展示MD5的计算进度
        nextTick(() => {
          const md5ProgressText = '校验MD5 ' + ((currentChunk / chunks) * 100).toFixed(0) + '%'
          document.querySelector(`.custom-status-${file.id}`).innerText = md5ProgressText
        })
      },
      onSuccess(md5) {
        statusRemove(file.id)
        resolve(md5)
      },
      onError() {
        error(`文件${file.name}读取出错，请检查该文件`)
        file.cancel()
        statusRemove(file.id)
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

function onFileProgress(rootFile, file, chunk) {
  console.log(
    `上传中 ${file.name}，chunk：${chunk.startByte / 1024 / 1024} ~ ${
      chunk.endByte / 1024 / 1024
    }`
  )
}

const onFileError = (rootFile, file, response, chunk) => {
  // console.log('error', file)
  error(response)
}
function error(msg) {
  ElNotification({
    title: '错误',
    message: msg,
    type: 'error',
    duration: 2000
  })
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

  // 服务端自定义的错误（即http状态码为200，但是是错误的情况），这种错误是Uploader无法拦截的
  let res = JSON.parse(response)
  console.log("onFileSuccess res:", res);
  if (res.code !== 200) {
    error(res.message)
    // 文件状态设为“失败”
    statusSet(file.id, 'failed')
    return
  }

  emits("uploadSuccess", file);
}


/**
 * 新增的自定义的状态: 'md5'、'merging'、'transcoding'、'failed'
 * @param id
 * @param status
 */
function statusSet(id, status) {
  const statusMap = {
    md5: {
      text: '校验MD5',
      bgc: '#fff'
    },
    failed: {
      text: '上传失败',
      bgc: '#e2eeff'
    }
  }

  customStatus.value = status
  nextTick(() => {
    const statusTag = document.createElement('span')
    statusTag.className = `custom-status-${id} custom-status`
    statusTag.innerText = statusMap[status].text
    statusTag.style.backgroundColor = statusMap[status].bgc

    // custom-status 样式不生效
    // 由于 style脚本 设置了 scoped，深层的样式修改不了
    // 通过给当前组件设置一个id，在该id下设置样式，就可以保证样式不全局污染
    // statusTag.style.position = 'absolute';
    // statusTag.style.top = '0';
    // statusTag.style.left = '0';
    // statusTag.style.right = '0';
    // statusTag.style.bottom = '0';
    // statusTag.style.zIndex = '1';

    const statusWrap = document.querySelector(`.file_${id} .uploader-file-status`)
    statusWrap.appendChild(statusTag)
  })
}
function statusRemove(id) {
  customStatus.value = ''
  nextTick(() => {
    const statusTag = document.querySelector(`.custom-status-${id}`)
    document.querySelector(`.file_${id} .uploader-file-actions`).style.display = 'block'
    statusTag.remove()
  })
}
</script>

<style lang="scss">

#global-uploader {

  .btn{
    background-color: #1ab394;
  }

  $blue: #108ee9;
  .file-panel {
    background-color: #fff;
    border: 1px solid #e2e2e2;
    border-radius: 7px 7px 0 0;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);

    .file-title {
      display: flex;
      height: 40px;
      line-height: 40px;
      padding: 0 15px;
      border-bottom: 1px solid #ddd;

      .operate {
        flex: 1;
        text-align: right;

        .el-button {
          --el-button-hover-link-text-color: #{$blue};

          + .el-button {
            margin-left: 8px;
          }
        }
      }
    }

    .file-list {

      list-style-type: none;
      position: relative;
      height: 240px;
      overflow-x: hidden;
      overflow-y: auto;
      background-color: #fff;
      transition: all 0.3s;

      padding: 0;
      margin: 0;

      .file-item {
        background-color: #fff;
      }
    }

    &.collapse {
      .file-title {
        background-color: #e7ecf2;
      }
      .file-list {
        height: 0;
      }
    }
  }

  .no-file {
    position: absolute;
    top: 45%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: #999;

    svg {
      vertical-align: text-bottom;
    }
  }

  .uploader-file {
    &.md5 {
      .uploader-file-resume {
        display: none;
      }
    }
  }

  .uploader-file-icon {
    &:before {
      content: '' !important;
    }

    &[icon='image'] {
      background: url(./images/image-icon.png);
    }
    &[icon='audio'] {
      background: url(./images/audio-icon.png);
      background-size: contain;
    }
    &[icon='video'] {
      background: url(./images/video-icon.png);
    }
    &[icon='document'] {
      background: url(./images/text-icon.png);
    }
    &[icon='unknown'] {
      background: url(./images/zip.png) no-repeat center;
      background-size: contain;
    }
  }

  .uploader-file-actions > span {
    margin-right: 6px;
  }

  .custom-status {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 1;
  }

}

#global-uploader-btn {
  //position: absolute;
  //clip: rect(0, 0, 0, 0);
}

</style>
