import request from "@/utils/request";

export function invokeService(modelService: any) {
  return request({
    url: "/msc/service/invoke",
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
    url: "/msc/service",
    method: "post",
    data: service
  });
}

export function selectServiceList() {
  return request({
    url: "/msc/service/list",
    method: "get"
  });
}

export function getModelServiceById(msId: string) {
  return request({
    url: "/msc/service/" + msId,
    method: "get"
  });
}
