import request from "@/utils/request";
// request.defaults.baseURL = import.meta.env.VITE_APP_DRIVE_API;
const drivePath = import.meta.env.VITE_APP_DRIVE_API;
const adminPath = import.meta.env.VITE_APP_BASE_API;
const contextPath = adminPath + "/drive";

export function listWorkspaceDir(params?: any) {
  return request({
    url: contextPath + "/workspace",
    method: "get",
    params: params
  });
}

export function listWorkspaceDirContainChildren(containerName: string) {
  return request({
    url: contextPath + "/workspace/" + containerName,
    method: "get"
  });
}

export function addFile(fileName: string, md5: string) {
  return request({
    url: drivePath + "/file",
    method: "post",
    data: { fileName, md5 }
  });
}
