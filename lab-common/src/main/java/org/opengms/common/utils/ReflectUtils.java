package org.opengms.common.utils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射工具类
 *
 * @author 7bin
 * @date 2022/11/07
 */
public class ReflectUtils {

    /**
     * 根据属性名设置对象属性值
     * @param obj 对象实例
     * @param prop 属性名
     * @param value 属性值
     * @author 7bin
     **/
    public static void setValueByProp(Object obj, String prop, String value) throws IntrospectionException, InvocationTargetException, IllegalAccessException {

        // 获得属性描述器 如属性在父类：object.getClass().getSuperclass()
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(prop, obj.getClass());
        // 获得set方法
        Method setMethod = propertyDescriptor.getWriteMethod();
        // 调用指定对象set方法
        setMethod.invoke(obj, getValue(value,setMethod.getParameterTypes()[0]));

    }

    private static Object getValue(Object value,Class type){
        if (value != null){
            if (type.isAssignableFrom(String[].class)) {
                return toStringArray(value);
            }
            if (type.isAssignableFrom(Integer[].class)) {
                return toIntegerArray(value);
            } else if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class) ) {
                return toInteger(value);
            }
            if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
                return toDouble(value);
            } else if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {
                return toBoolean(value);
            } else if (type.isAssignableFrom(String.class)) {
                return toString(value);
            }
            // 在此处添加新的转换规则
            // ...
        }
        return null;
    }

    private static String[] toStringArray(Object value){
        return value.toString().split(",");
    }

    private static Integer[] toIntegerArray(Object value){
        String[] stringArray = toStringArray(value);
        Integer[] intArray = new Integer[stringArray.length];
        for (int i = 0; i < stringArray.length; i++){
            intArray[i] = Integer.parseInt(stringArray[i]);
        }
        return intArray;
    }

    private static Integer toInteger(Object value){
        return Integer.parseInt(value.toString());
    }

    private static Double toDouble(Object value){
        return Double.parseDouble(value.toString());
    }

    private static String toString(Object value){
        return value.toString();
    }


    private static Boolean toBoolean(Object value){
        return Boolean.parseBoolean(value.toString());
    }

}
