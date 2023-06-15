import request from "@/utils/request";

const contextPath = import.meta.env.VITE_APP_BASE_API + "/system/role";

// 查询角色列表
export function listRole(query) {
    return request({
        url: contextPath + "/list",
        method: "get",
        params: query
    });
}

// 查询角色详细
export function getRole(roleId) {
    return request({
        url: contextPath + "/" + roleId,
        method: "get"
    });
}

// 新增角色
export function addRole(data) {
    return request({
        url: contextPath,
        method: "post",
        data: data
    });
}

// 修改角色
export function updateRole(data) {
    return request({
        url: contextPath,
        method: "put",
        data: data
    });
}

// 删除角色
export function delRole(roleId) {
    return request({
        url: contextPath + "/" + roleId,
        method: "delete"
    });
}

// 角色状态修改
export function changeRoleStatus(roleId, status) {
    const data = {
        roleId,
        status
    };
    return request({
        url: contextPath + "/status",
        method: "put",
        data: data
    });
}

// 查询角色已授权用户列表
export function allocatedUserList(query) {
    return request({
        url: contextPath + "/authUser/allocatedList",
        method: "get",
        params: query
    });
}

// 取消用户授权角色
export function authUserCancel(data) {
    return request({
        url: contextPath + "/authUser/cancel",
        method: "put",
        data: data
    });
}
