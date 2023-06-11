package org.opengms.admin.controller;

import org.opengms.admin.controller.common.BaseController;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.po.system.SysDictData;
import org.opengms.admin.service.ISysDictService;
import org.opengms.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典信息
 *
 * @author 7bin
 * @date 2023/06/10
 */
@RestController
@RequestMapping("/system/dict")
public class SysDictController extends BaseController {

    @Autowired
    private ISysDictService dictService;


    /**
     * 根据字典类型查询字典数据信息
     */
    @GetMapping(value = "/data/type/{dictType}")
    public ApiResponse dictType(@PathVariable String dictType)
    {
        List<SysDictData> data = dictService.selectDictDataByType(dictType);
        if (StringUtils.isNull(data))
        {
            data = new ArrayList<SysDictData>();
        }
        return success(data);
    }


}
