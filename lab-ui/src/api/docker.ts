import request from "@/utils/request";

export function listImages() {
  return request({
    url: "/container/list/image",
    method: "get"
  });
}

export function listContainers() {
  return request({
    url: "/container/list/container",
    method: "get"
  });
}

export function getJupyterContainerById(id: string) {
  return request({
    url: "/container/jupyter/item/" + id,
    method: "get"
  });
}
