import request from "@/utils/request";
const contextPath = import.meta.env.VITE_APP_BASE_API + "/system/user";
import { parseStrEmpty } from "@/utils/common";

// 判断用户是否登录过期
export function checkAuth() {
  return request({
    url: contextPath + "/auth",
    method: "get"
  });
}

// 用户状态修改
export function changeUserStatus(userId, status) {
  const data = {
    userId,
    status
  };
  return request({
    url: contextPath + "/status",
    method: "put",
    data: data
  });
}

// 查询用户详细
export function getUser(userId) {
  return request({
    url: contextPath + "/" + parseStrEmpty(userId),
    method: "get"
  });
}

// 查询用户列表
export function listUser(query) {
  return request({
    url: contextPath + "/list",
    method: "get",
    params: query
  });
}

// 用户密码重置
export function resetUserPwd(userId, password) {
  const data = {
    userId,
    password
  };
  return request({
    url: contextPath + "/reset/pwd",
    method: "put",
    data: data
  });
}

// 删除用户
export function delUser(userId) {
  return request({
    url: contextPath + "/" + userId,
    method: "delete"
  });
}

// 修改用户
export function updateUser(data) {
    return request({
        url: contextPath + "",
        method: "put",
        data: data
    });
}

// 修改用户个人信息
export function updateUserProfile(data) {
    return request({
        url: contextPath + "/profile",
        method: "put",
        data: data
    });
}

// 查询用户个人信息
export function getUserProfile() {
    return request({
        url: contextPath + "/profile",
        method: "get"
    });
}

// 用户头像上传
export function uploadAvatar(data) {
    return request({
        url: contextPath + "/profile/avatar",
        method: "post",
        data: data
    });
}

// 用户密码重置
export function updateUserPwd(oldPassword, newPassword) {
    const data = {
        oldPassword,
        newPassword
    };
    return request({
        url: contextPath + "/profile/pwd",
        method: "put",
        params: data
    });
}
