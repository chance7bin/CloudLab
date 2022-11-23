import request from "@/utils/request";
// request.defaults.baseURL = import.meta.env.VITE_APP_CONTAINER_API;
const containerPath = import.meta.env.VITE_APP_CONTAINER_API;
const adminPath = import.meta.env.VITE_APP_BASE_API;
const contextPath = adminPath + "/container";

export function initWorkspace(imageName: string) {
  return request({
    url: contextPath + "/workspace/initialization/" + imageName,
    method: "get"
  });
}
