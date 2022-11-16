package org.opengms.common;

import com.google.gson.Gson;

/**
 * Gson 工具类
 *
 * @author 7bin
 * @date 2022/11/13
 */
public class GsonUtils {

    public final static Gson gsonInstance = new Gson();

    /**
     *  json 字符转转对象
     * @param json: json 字符串
     * @param classOfT: 对象类型
     * @return: T
     **/
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gsonInstance.fromJson(json, classOfT);
    }

    /**
     *  对象转 json
     * @param obj: 对象
     * @return: java.lang.String
     **/
    public static String toJsonStr(Object obj){
        return gsonInstance.toJson(obj);
    }

}
