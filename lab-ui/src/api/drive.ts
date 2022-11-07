import request from "@/utils/request";

export function listWorkspaceDir(params?: any) {
  return request({
    url: "/drive/workspace",
    method: "get",
    params: params
  });
}

export function listWorkspaceDirContainChildren(containerName: string) {
  return request({
    url: "/drive/workspace/" + containerName,
    method: "get"
  });
}
