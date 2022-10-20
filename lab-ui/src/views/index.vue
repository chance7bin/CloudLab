<template>
  <div>
    <div class="app-container home"></div>
  </div>
</template>

<script setup name="Index">
const version = ref('3.8.3')

function goTarget(url) {
  window.open(url, '__blank')
}
</script>

<style scoped lang="scss">
.home {
  background-image: url("../assets/images/home_bg.jpg");
  min-height: calc(100vh - 84px);
  background-size: cover;
}
</style>

