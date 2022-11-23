package org.opengms.container.utils;

import com.github.pagehelper.PageHelper;
import org.opengms.common.utils.sql.SqlUtil;
import org.opengms.container.entity.bo.TableSupport;
import org.opengms.container.entity.dto.PageDTO;

/**
 * 分页工具类
 * 
 * @author 7bin
 */
public class PageUtils extends PageHelper
{
    /**
     * 设置请求分页数据
     */
    public static void startPage()
    {
        PageDTO pageDTO = TableSupport.buildPageRequest();
        Integer pageNum = pageDTO.getPageNum();
        Integer pageSize = pageDTO.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDTO.getOrderBy());
        Boolean reasonable = pageDTO.getReasonable();
        startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }
}
