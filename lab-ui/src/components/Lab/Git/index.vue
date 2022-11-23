<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script setup>
const { proxy } = getCurrentInstance();

const url = ref('https://gitee.com/y_project/7bin-Vue');

function goto() {
  // window.open(url.value)
  proxy.$modal.alertWarning("正在建设中...");
}
</script>
