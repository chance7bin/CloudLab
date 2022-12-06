import request from "@/utils/request";
// request.defaults.baseURL = import.meta.env.VITE_APP_CONTAINER_API;
const containerPath = import.meta.env.VITE_APP_CONTAINER_API;
const adminPath = import.meta.env.VITE_APP_BASE_API;
const contextPath = adminPath + "/container";

export function initWorkspace(imageName: string, containerName) {
  return request({
    url: contextPath + "/workspace/initialization/" + imageName + "/" + containerName,
    method: "get",
    timeout: 30000
  });
}

export function listWorkspaceDirContainChildren(containerId: string) {
  return request({
    url: contextPath + "/workspace/dir/" + containerId,
    method: "get"
  });
}

export function getJupyterContainerById(id: string) {
  return request({
    url: contextPath + "/workspace/jupyter/item/" + id,
    method: "get"
  });
}
