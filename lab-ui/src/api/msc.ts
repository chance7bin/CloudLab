import request from "@/utils/request";

export function invoke() {
  return request({
    url: "/msc/invoke",
    method: "post"
  });
}
