import request from "@/utils/request";
import type { QueryParams } from "@/utils/customType";
// request.defaults.baseURL = import.meta.env.VITE_APP_CONTAINER_API;
const containerPath = import.meta.env.VITE_APP_CONTAINER_API;
const adminPath = import.meta.env.VITE_APP_BASE_API + "/container";
const contextPath = adminPath + "/service";

export function invokeService(modelService: any) {
  return request({
    url: contextPath + "/invoke",
    method: "post",
    data: modelService
  });
}

interface Service {
  imageId: string;
  containerId: string;
  msName: string;
  relativeDir: string;
  mdlFilePath: string;
  encapScriptPath: string;
}

export type { Service };

export function createModelService(service: Service) {
  return request({
    url: contextPath,
    method: "post",
    data: service,
    timeout: 2 * 60 * 1000
  });
}

export function selectServiceList(queryParams: QueryParams) {
  return request({
    url: contextPath + "/list",
    method: "get",
    params: queryParams
  });
}

export function getModelServiceById(msId: string) {
  return request({
    url: contextPath + "/" + msId,
    method: "get"
  });
}
