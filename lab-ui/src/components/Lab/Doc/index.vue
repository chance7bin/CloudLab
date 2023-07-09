<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script setup>
const { proxy } = getCurrentInstance();

const url = ref('http://doc.binb.vip/binb-vue');

function goto() {
  // window.open(url.value)
  proxy.$modal.alertWarning("正在建设中...");
}
</script>