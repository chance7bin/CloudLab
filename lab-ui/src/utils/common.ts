import lodash from "lodash";
import { HTTP_STATUS } from "@/utils/globalConstants";
import errorCode from "@/utils/errorCode";

/**
 * 通用ts方法封装处理
 */

/**
 * 参数处理
 * @param {*} params  参数
 */
export function tansParams(params: { [x: string]: any }) {
  let result = "";
  for (const propName of Object.keys(params)) {
    const value = params[propName];
    var part = encodeURIComponent(propName) + "=";
    if (value !== null && value !== "" && typeof value !== "undefined") {
      if (typeof value === "object") {
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== "" && typeof value[key] !== "undefined") {
            let params = propName + "[" + key + "]";
            var subPart = encodeURIComponent(params) + "=";
            result += subPart + encodeURIComponent(value[key]) + "&";
          }
        }
      } else {
        result += part + encodeURIComponent(value) + "&";
      }
    }
  }
  return result;
}

// 验证是否为blob格式
export async function blobValidate(data: any) {
  try {
    const text = await data.text();
    JSON.parse(text);
    return false;
  } catch (error) {
    return true;
  }
}

// 返回项目路径
export function getNormalPath(p) {
  if (p.length === 0 || !p || p == "undefined") {
    return p;
  }
  let res = p.replace("//", "/");
  if (res[res.length - 1] === "/") {
    return res.slice(0, res.length - 1);
  }
  return res;
}

// 验证接口的返回结果,带提示框
export function validateApiResultWithTips(proxy: Record<string, any>, res: any, successMsg: string, errorMsg: string) {
  return new Promise((resolve) => {
    if (res.code == HTTP_STATUS.SUCCESS) {
      if (errorMsg != null && errorMsg != "") {
        proxy.$modal.msgSuccess(successMsg);
      } else {
        proxy.$modal.msgSuccess("成功");
      }
      resolve(res.data);
    } else if (res.code == HTTP_STATUS.UNAUTHORIZED) {
      proxy.$modal.msgError(errorCode["401"]);
    } else if (res.code == HTTP_STATUS.FORBIDDEN) {
      proxy.$modal.msgError(errorCode["403"]);
    } else if (res.code == HTTP_STATUS.NOT_FOUND) {
      proxy.$modal.msgError(errorCode["404"]);
    } else {
      if (errorMsg != null && errorMsg != "") {
        proxy.$modal.msgError(errorMsg);
      } else {
        proxy.$modal.msgError(errorCode["default"]);
      }
    }
  });
}

// 验证接口的返回结果
export function validateApiResult(res: any) {
  return new Promise((resolve, reject) => {
    if (res.code == HTTP_STATUS.SUCCESS) {
      resolve(res.data);
    } else {
      reject();
    }
  });
}
