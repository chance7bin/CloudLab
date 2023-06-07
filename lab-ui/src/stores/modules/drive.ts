import usePermissionStore from "@/stores/modules/permission";
import useTagsViewStore from "@/stores/modules/tagsView";

const useDriveStore = defineStore("drive", {
  state: () => ({
    currentPath: ""
  }),
  actions: {
    setCurrentPath(path: string) {
      this.currentPath = path;
    },
    getCurrentPath() {
      return this.currentPath;
    }
  }
});
export default useDriveStore;
