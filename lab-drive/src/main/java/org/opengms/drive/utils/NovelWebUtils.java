package org.opengms.drive.utils;


import cn.novelweb.tool.http.Result;
import org.apache.ibatis.annotations.Results;
import org.opengms.drive.entity.dto.ApiResponse;

/**
 * 提供给NovelWeb工具的相关工具类
 *
 * @author 7bin
 */
public class NovelWebUtils {
    /**
     * 上传文件结果转换为本系统的结果
     *
     * @param result 结果
     * @return {@link Result}<{@link Object}>
     */
    public static ApiResponse forReturn(Result<Object> result) {
        if ("200".equals(result.getCode()) || "201".equals(result.getCode())) {
            return ApiResponse.success(result.getMessage());
        } else if ("206".equals(result.getCode())) {
            // "文件部分模块上传错误"
            return ApiResponse.error(result.getMessage(), result.getData());
        } else {
            // "文件上传错误"
            return ApiResponse.error(result.getMessage());
        }
    }

}
