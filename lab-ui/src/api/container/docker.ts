import request from "@/utils/request";
// request.defaults.baseURL = import.meta.env.VITE_APP_CONTAINER_API;
const containerPath = import.meta.env.VITE_APP_CONTAINER_API;
const adminPath = import.meta.env.VITE_APP_BASE_API;
const contextPath = adminPath + "/container";

export function listImages() {
  return request({
    url: contextPath + "/container/list/image",
    method: "get"
  });
}

export function listContainers() {
  return request({
    url: contextPath + "/container/list/container",
    method: "get"
  });
}

export function getJupyterContainerById(id: string) {
  return request({
    url: contextPath + "/container/jupyter/item/" + id,
    method: "get"
  });
}
