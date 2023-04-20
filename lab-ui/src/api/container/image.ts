import request from "@/utils/request";
// request.defaults.baseURL = import.meta.env.VITE_APP_CONTAINER_API;
const containerPath = import.meta.env.VITE_APP_CONTAINER_API;
const adminPath = import.meta.env.VITE_APP_BASE_API + "/container";
const contextPath = adminPath + "/image";
import type { QueryParams } from "@/utils/customType";

export function listImages(queryParams: QueryParams) {
  return request({
    url: contextPath + "/list",
    // url: containerPath + "/image/list",
    method: "get",
    params: queryParams
  });
}
