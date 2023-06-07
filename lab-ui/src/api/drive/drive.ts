import request from "@/utils/request";
import type { QueryParams } from "@/utils/customType";
import useDriveStore from "@/stores/modules/drive";
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

export function getFileList(parentId: string, queryParams: QueryParams) {
  const query = {
    // parentId 从状态管理器中获取
    // parentId: useDriveStore().getCurrentPath(),
    parentId: parentId,
    pageNum: queryParams.pageNum,
    pageSize: queryParams.pageSize
  };
  return request({
    url: contextPath + "/list",
    method: "get",
    params: query
  });
}

export function addFileToDrive(fileName: string, md5: string, size: string) {
  return request({
    url: drivePath + "/file",
    method: "post",
    data: { fileName, md5, size }
  });
}

interface FileInfoDTO {
  parentId: string;
  filename: string;
  directory: boolean;
  md5: string | null;
  size: string | null;
  type: string | null;
  driveFileId: string | null;
}

export type { FileInfoDTO };

export function addFile(fileInfoDTO: FileInfoDTO) {
  return request({
    url: contextPath + "/file",
    method: "post",
    data: fileInfoDTO
  });
}
