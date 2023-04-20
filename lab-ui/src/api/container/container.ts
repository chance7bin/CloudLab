import request from "@/utils/request";
// request.defaults.baseURL = import.meta.env.VITE_APP_CONTAINER_API;
const containerPath = import.meta.env.VITE_APP_CONTAINER_API;
const adminPath = import.meta.env.VITE_APP_BASE_API + "/container";
const contextPath = adminPath + "/container";
import type { QueryParams } from "@/utils/customType";

export function listContainers(queryParams: QueryParams) {
  return request({
    url: contextPath + "/list",
    method: "get",
    params: queryParams
  });
}

interface NewEnv {
  /** 基于当前容器创建新环境 */
  containerId: string;
  /** 新环境名称 */
  envName: string;
  /** 新环境版本 */
  tag: string;
}

export type { NewEnv };

export function createNewEnv(newEnv: NewEnv) {
  return request({
    url: contextPath + "/newEnv",
    method: "post",
    data: newEnv
  });
}
