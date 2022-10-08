<template>
  <i-frame v-model:src="url"></i-frame>
</template>

<script setup lang="ts">
import iFrame from '@/components/iFrame/index.vue'

// const url = ref(import.meta.env.VITE_APP_BASE_API + "/swagger-ui/index.html")
const url = ref("http://127.0.0.1:8810/lab?token=123456")


</script>
