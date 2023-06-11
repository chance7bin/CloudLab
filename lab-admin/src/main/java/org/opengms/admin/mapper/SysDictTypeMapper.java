package org.opengms.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.admin.entity.po.system.SysDictType;

/**
 * @author 7bin
 * @date 2023/06/10
 */
@Mapper
public interface SysDictTypeMapper {

    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    SysDictType selectDictTypeByType(String dictType);

}
