package org.opengms.common;

import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 终端的执行输出
 *
 * @author 7bin
 * @date 2022/12/14
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TerminalRes {
    Integer code;
    String msg;

    public static final Integer SUCCESS_CODE = 1;

    public static final Integer ERROR_CODE = -1;

    public static TerminalRes success(String msg){
        return new TerminalRes(SUCCESS_CODE, msg);
    }

    public static TerminalRes error(String msg){
        return new TerminalRes(ERROR_CODE, msg);
    }


}
