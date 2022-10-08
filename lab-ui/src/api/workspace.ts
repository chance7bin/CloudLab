import request from "@/utils/request";

export function initWorkspace() {
  return request({
    url: "/workspace/initialization",
    method: "get"
  });
}
