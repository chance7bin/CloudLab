package org.opengms.admin.msc.enums;

/**
 * 数据类型
 *
 * @author 7bin
 * @date 2022/10/21
 */
public enum DataMIME {

    TEXT("text"), // 文本类型

    FILE("file"), // 文件类型

    UNKNOWN("unknown"), // 未知类型

    ;

    private final String info;

    DataMIME(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public static DataMIME getDataMIMEByValue(String value) {
        for(DataMIME dataMIME:DataMIME.values()){
            if(dataMIME.getInfo().equals(value)){
                return dataMIME;
            }
        }
        return null;
    }
}
