package org.opengms.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.admin.entity.po.system.SysDictData;

import java.util.List;

/**
 * @author 7bin
 * @date 2023/06/10
 */
@Mapper
public interface SysDictDataMapper {

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    List<SysDictData> selectDictDataByType(String dictType);

}
