package org.opengms.admin.controller.common;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.opengms.common.constant.HttpStatus;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.LoginUser;
import org.opengms.admin.entity.dto.PageDTO;
import org.opengms.admin.entity.dto.TableDataInfo;
import org.opengms.admin.entity.bo.TableSupport;
import org.opengms.admin.utils.SecurityUtils;
import org.opengms.common.utils.DateUtils;
import org.opengms.admin.utils.PageUtils;
import org.opengms.common.utils.StringUtils;
import org.opengms.common.utils.sql.SqlUtil;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * web层通用数据处理
 * 
 * @author 7bin
 */
@Slf4j
public class BaseController
{
    // protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 将前台传递过来的日期格式的字符串，自动转化为Date类型
     */
    @InitBinder
    public void initBinder(WebDataBinder binder)
    {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport()
        {
            @Override
            public void setAsText(String text)
            {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * 设置请求分页数据
     */
    protected void startPage()
    {
        PageUtils.startPage();
    }

    /**
     * 设置请求排序数据
     */
    protected void startOrderBy()
    {
        PageDTO pageDTO = TableSupport.buildPageRequest();
        if (StringUtils.isNotEmpty(pageDTO.getOrderBy()))
        {
            String orderBy = SqlUtil.escapeOrderBySql(pageDTO.getOrderBy());
            PageHelper.orderBy(orderBy);
        }
    }

    /**
     * 清理分页的线程变量
     */
    protected void clearPage()
    {
        PageUtils.clearPage();
    }

    /**
     * 响应请求分页数据
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TableDataInfo getDataTable(List<?> list)
    {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }


    /**
     * 返回成功
     */
    public ApiResponse success()
    {
        return ApiResponse.success();
    }

    /**
     * 返回失败消息
     */
    public ApiResponse error()
    {
        return ApiResponse.error();
    }

    /**
     * 返回成功消息
     */
    public ApiResponse success(String message)
    {
        return ApiResponse.success(message);
    }

    /**
     * 返回成功消息
     */
    public ApiResponse success(Object data)
    {
        return ApiResponse.success(data);
    }

    /**
     * 返回失败消息
     */
    public ApiResponse error(String message)
    {
        return ApiResponse.error(message);
    }

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    protected ApiResponse affectRows(int rows)
    {
        return rows > 0 ? ApiResponse.success() : ApiResponse.error();
    }

    /**
     * 响应返回结果
     *
     * @param result 结果
     * @return 操作结果
     */
    protected ApiResponse affectRows(boolean result)
    {
        return result ? success() : error();
    }

    /**
     * 页面跳转
     */
    public String redirect(String url)
    {
        return StringUtils.format("redirect:{}", url);
    }

    /**
     * 获取用户缓存信息
     */
    public LoginUser getLoginUser()
    {
        return SecurityUtils.getLoginUser();
    }

    /**
     * 获取登录用户id
     */
    public Long getUserId()
    {
        return getLoginUser().getUserId();
    }


    /**
     * 获取登录用户名
     */
    public String getUsername()
    {
        return getLoginUser().getUsername();
    }
}
