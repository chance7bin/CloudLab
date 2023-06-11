import request from "@/utils/request";
const contextPath = import.meta.env.VITE_APP_BASE_API + "/system/dict";

// 数据字典信息

// 根据字典类型查询字典数据信息
export function getDicts(dictType) {
  return request({
    url: contextPath + "/data/type/" + dictType,
    method: "get"
  });
}
