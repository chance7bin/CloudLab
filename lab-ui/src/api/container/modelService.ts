import request from "@/utils/request";
// request.defaults.baseURL = import.meta.env.VITE_APP_CONTAINER_API;
const containerPath = import.meta.env.VITE_APP_CONTAINER_API;
const adminPath = import.meta.env.VITE_APP_BASE_API;
const contextPath = adminPath + "/container";

export function invokeService(modelService: any) {
  return request({
    url: contextPath + "/service/invoke",
    method: "post",
    data: modelService
  });
}

interface Service {
  containerId: string | null;
  containerName: string;
  msName: string;
  relativeDir: string;
  mdlFilePath: string;
  encapScriptPath: string;
}
export function createModelService(service: Service) {
  return request({
    url: contextPath + "/service",
    method: "post",
    data: service
  });
}

export function selectServiceList() {
  return request({
    url: contextPath + "/service/list",
    method: "get"
  });
}

export function getModelServiceById(msId: string) {
  return request({
    url: contextPath + "/service/" + msId,
    method: "get"
  });
}
