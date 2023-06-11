package org.opengms.admin.entity.po.system;

import lombok.Data;

/**
 * 用户和角色关联 sys_user_role
 *
 * @author 7bin
 * @date 2023/06/11
 */
@Data
public class SysUserRole {

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;

}
