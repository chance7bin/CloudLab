package org.opengms.admin.entity.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户登录对象
 *
 * @author bin
 * @date 2022/08/26
 */
@ApiModel(description = "用户登录对象")
@Data
public class LoginDTO {
    /**
     * 用户名
     */
    @ApiModelProperty("用户名")
    private String username;

    /**
     * 用户密码
     */
    @ApiModelProperty("用户密码")
    private String password;

    /**
     * 验证码
     */
    @ApiModelProperty("验证码")
    private String code;

    /**
     * 唯一标识
     */
    @ApiModelProperty("唯一标识")
    private String uuid;


}
