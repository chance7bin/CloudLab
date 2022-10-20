import request from "@/utils/request";

export function initWorkspace(imageName: string) {
  return request({
    url: "/workspace/initialization/" + imageName,
    method: "get"
  });
}
