package org.opengms.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.opengms.admin.entity.po.system.SysMenu;

import java.util.List;

/**
 * @author bin
 * @date 2022/09/14
 */
@Mapper
public interface SysMenuMapper {

    /**
     * 根据用户ID查询菜单
     *
     * @return 菜单列表
     */
    List<SysMenu> selectMenuTreeAll();

    /**
     * 根据用户ID查询菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectMenuTreeByUserId(Long userId);

}
