import request from "@/utils/request";

const contextPath = import.meta.env.VITE_APP_BASE_API;

// 获取路由
export const getRouters = () => {
  return request({
    url: contextPath + "/getRouters",
    method: "get"
  });
};
