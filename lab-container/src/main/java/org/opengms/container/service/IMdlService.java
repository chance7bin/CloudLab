package org.opengms.container.service;

import org.dom4j.Attribute;
import org.opengms.container.entity.bo.mdl.ModelClass;
import org.opengms.container.entity.po.ModelService;

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

    // 解析mdl文件(注解版)
    ModelClass parseMdlFileWithAnnotation(String mdlFilePath);

    // 设置xml的属性
    // void setElementAttributes(List<Attribute> attributes, Object obj) throws Exception;


    /**
     * 获取Parameter步骤的参数值
     *
     * @param modelService 模型服务
     * @param state state name
     * @param event event name
     * @param serviceDir 服务相对路径
     * @param insDir 实例相对路径
     * @return {@link String} 值类型: 值; 文件类型: 文件名
     * @author 7bin
     **/
    String getParameter(ModelService modelService, String state, String event, String serviceDir, String insDir);
}
