<template>
  <el-tree :data="folderTree" :props="defaultProps" @node-click="handleNodeClick" />
</template>

<script setup lang="ts">
import useCurrentInstance from "@/utils/currentInstance";
import { listWorkspaceDirContainChildren } from "@/api/drive/drive";

const { proxy } = useCurrentInstance();

const props = defineProps({
  containerName: {
    type: String,
    required: true
  }
})

const emit = defineEmits(["selectedItem"]);




interface Tree {
  label: string,
  relativePath: string,
  children?: Tree[]
}

const handleNodeClick = (data: Tree) => {
  // console.log(data)
  emit("selectedItem", data)
}

const folderTree = ref<Tree[]>([]);

// "jupyter_cus_5.0_8268889755334766592"
listWorkspaceDirContainChildren(props.containerName).then(res => {
  // let data = res.data;
  folderTree.value = res.data;
});


const defaultProps = {
  children: 'children',
  label: 'label',
  relativePath:'relativePath'
}

</script>

<style scoped lang="scss">

</style>