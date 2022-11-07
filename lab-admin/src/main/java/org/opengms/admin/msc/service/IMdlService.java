package org.opengms.admin.msc.service;

import org.dom4j.Attribute;
import org.opengms.admin.msc.entity.bo.mdl.ModelClass;
import org.opengms.common.utils.ReflectUtils;

import java.util.List;

/**
 * mdl操作服务层
 *
 * @author 7bin
 * @date 2022/11/07
 */
public interface IMdlService {

    // 解析mdl文件
    ModelClass parseMdlFile(String mdlFilePath) throws Exception;

    // 设置xml的属性
    void setElementAttributes(List<Attribute> attributes, Object obj) throws Exception;


    // 获取Parameter步骤的参数值
    String getParameter(ModelClass modelClass, String state, String event);
}
