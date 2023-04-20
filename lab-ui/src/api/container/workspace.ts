import request from "@/utils/request";
// request.defaults.baseURL = import.meta.env.VITE_APP_CONTAINER_API;
const containerPath = import.meta.env.VITE_APP_CONTAINER_API;
const adminPath = import.meta.env.VITE_APP_BASE_API + "/container";
const contextPath = adminPath + "/workspace";

export function initWorkspace(imageId: string, containerName) {
  return request({
    url: contextPath + "/initialization/" + imageId + "/" + containerName,
    method: "get",
    timeout: 30000
  });
}

export function listWorkspaceDirContainChildren(containerId: string) {
  return request({
    url: contextPath + "/dir/" + containerId,
    method: "get"
  });
}

export function getWorkspaceByContainerId(id: string) {
  return request({
    url: contextPath + "/" + id,
    method: "get"
  });
}
