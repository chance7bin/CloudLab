import request from "@/utils/request";

const contextPath = import.meta.env.VITE_APP_BASE_API;

// 获取路由
export const getRouters = () => {
    return request({
        url: contextPath + "/getRouters",
        method: "get"
    });
};

// 根据角色ID查询菜单下拉树结构
export function roleMenuTreeSelect(roleId) {
    return request({
        url: contextPath + "/system/menu/roleMenuTreeSelect/" + roleId,
        method: "get"
    });
}

// 查询菜单下拉树结构
export function menuTreeSelect() {
    return request({
        url: contextPath + "/system/menu/menuTreeSelect",
        method: "get"
    });
}
