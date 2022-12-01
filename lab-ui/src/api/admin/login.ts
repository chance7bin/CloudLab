import request from "@/utils/request";
const contextPath = import.meta.env.VITE_APP_BASE_API;

// 登录方法
export function login(username: string, password: string, code: string, uuid: string) {
  const data = {
    username,
    password,
    code,
    uuid
  };
  return request({
    url: contextPath + "/login",
    headers: {
      isToken: false
    },
    method: "post",
    data: data
  });
}

// 注册方法
export function register(data: any) {
  return request({
    url: contextPath + "/register",
    headers: {
      isToken: false
    },
    method: "post",
    data: data
  });
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: contextPath + "/getInfo",
    method: "get"
  });
}

// 退出方法
export function logout() {
  return request({
    url: contextPath + "/logout",
    method: "post"
  });
}

// 获取验证码
export function getCodeImg() {
  return request({
    url: contextPath + "/captchaImage",
    headers: {
      isToken: false
    },
    method: "get",
    timeout: 20000
  });
}

// 判断用户是否登录过期
export function checkAuth() {
  return request({
    url: contextPath + "/user/auth",
    method: "get"
  });
}
