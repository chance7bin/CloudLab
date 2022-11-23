package org.opengms.admin.entity.bo;

import org.opengms.admin.entity.dto.PageDTO;
import org.opengms.common.utils.ServletUtils;
import org.opengms.common.utils.text.Convert;

/**
 * 表格数据处理
 * 
 * @author 7bin
 */
public class TableSupport
{
    /**
     * 当前记录起始索引
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * 每页显示记录数
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 排序列
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * 排序的方向 "desc" 或者 "asc".
     */
    public static final String IS_ASC = "isAsc";

    /**
     * 分页参数合理化
     */
    public static final String REASONABLE = "reasonable";

    /**
     * 封装分页对象
     */
    public static PageDTO getPageDomain()
    {
        PageDTO pageDTO = new PageDTO();
        pageDTO.setPageNum(Convert.toInt(ServletUtils.getParameter(PAGE_NUM), 1));
        pageDTO.setPageSize(Convert.toInt(ServletUtils.getParameter(PAGE_SIZE), 10));
        pageDTO.setOrderByColumn(ServletUtils.getParameter(ORDER_BY_COLUMN));
        pageDTO.setIsAsc(ServletUtils.getParameter(IS_ASC));
        pageDTO.setReasonable(ServletUtils.getParameterToBool(REASONABLE));
        return pageDTO;
    }

    public static PageDTO buildPageRequest()
    {
        return getPageDomain();
    }
}
