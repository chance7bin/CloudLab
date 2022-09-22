package org.opengms.admin.service;

import org.opengms.admin.entity.dto.RouterDTO;
import org.opengms.admin.entity.po.system.SysMenu;

import java.util.List;

/**
 * @author bin
 * @date 2022/09/14
 */
public interface ISysMenuService {

    /**
     * 根据用户ID查询菜单树信息
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 构建前端路由所需要的菜单
     *
     * @param menus 菜单列表
     * @return 路由列表
     */
    List<RouterDTO> buildMenus(List<SysMenu> menus);

}
