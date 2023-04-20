package org.opengms.container.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.container.entity.po.MsrIns;

/**
 * 模型运行实例mapper
 *
 * @author 7bin
 * @date 2022/12/06
 */
@Mapper
public interface MsrInsMapper extends BaseMapper<MsrIns>{

    int updateMsrInsLogs(MsrIns msrIns);

    int updateMsrInsLogsAndStatus(MsrIns msrIns);


    MsrIns selectByMsriId(String msriId);

}
