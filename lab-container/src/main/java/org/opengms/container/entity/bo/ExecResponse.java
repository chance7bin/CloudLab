package org.opengms.container.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调用exec的结果
 *
 * @author 7bin
 * @date 2023/05/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecResponse {

    Integer exitCode;

    String response;

    String error;

}
