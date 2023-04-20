import request from "@/utils/request";
// request.defaults.baseURL = import.meta.env.VITE_APP_CONTAINER_API;
const containerPath = import.meta.env.VITE_APP_CONTAINER_API;
const adminPath = import.meta.env.VITE_APP_BASE_API + "/container";
const contextPath = adminPath + "/instance";
import type { QueryParams } from "@/utils/customType";

export function getMsInsById(msriId: string) {
  return request({
    url: contextPath + "/" + msriId,
    method: "get"
  });
}
