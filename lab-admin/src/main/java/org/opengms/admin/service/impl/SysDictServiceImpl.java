package org.opengms.admin.service.impl;

import org.opengms.admin.entity.po.system.SysDictData;
import org.opengms.admin.entity.po.system.SysDictType;
import org.opengms.admin.mapper.SysDictDataMapper;
import org.opengms.admin.mapper.SysDictTypeMapper;
import org.opengms.admin.service.ISysDictService;
import org.opengms.admin.utils.DictUtils;
import org.opengms.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 7bin
 * @date 2023/06/10
 */
@Service
public class SysDictServiceImpl implements ISysDictService {

    @Autowired
    private SysDictTypeMapper dictTypeMapper;

    @Autowired
    private SysDictDataMapper dictDataMapper;

    @Override
    public SysDictType selectDictTypeByType(String dictType) {
        return dictTypeMapper.selectDictTypeByType(dictType);
    }


    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        List<SysDictData> dictDatas = DictUtils.getDictCache(dictType);
        if (StringUtils.isNotEmpty(dictDatas))
        {
            return dictDatas;
        }
        dictDatas = dictDataMapper.selectDictDataByType(dictType);
        if (StringUtils.isNotEmpty(dictDatas))
        {
            DictUtils.setDictCache(dictType, dictDatas);
            return dictDatas;
        }
        return null;
    }
}
