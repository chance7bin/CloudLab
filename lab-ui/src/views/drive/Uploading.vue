<template>
  <div>
    <simple-uploader />
  </div>
</template>

<script setup>
import useCurrentInstance from "@/utils/currentInstance";
import SimpleUploader from "@/components/Uploader/SimpleUploader.vue";
const { proxy } = useCurrentInstance();

</script>

<style scoped lang="scss"></style>
