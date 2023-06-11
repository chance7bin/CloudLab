package org.opengms.admin.service;

import org.opengms.admin.entity.po.system.SysDictData;
import org.opengms.admin.entity.po.system.SysDictType;

import java.util.List;

/**
 * 数据字典服务层
 *
 * @author 7bin
 * @date 2023/06/10
 */
public interface ISysDictService {

    /**
     * 根据字典类型查询字典类型信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    SysDictType selectDictTypeByType(String dictType);

    /**
     * 根据字典类型查询字典数据信息
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    List<SysDictData> selectDictDataByType(String dictType);

}
