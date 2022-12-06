package org.opengms.container.enums;

import lombok.AllArgsConstructor;

/**
 * 模型封装脚本执行步骤
 *
 * @author 7bin
 * @date 2022/10/20
 */
@AllArgsConstructor
public enum ProcessState {

    INIT("init"),
    ON_ENTER_STATE("onEnterState"),
    ON_FIRE_EVENT("onFireEvent"),
    ON_REQUEST_DATA("onRequestData"),
    ON_RESPONSE_DATA("onResponseData"),
    ON_POST_ERROR_INFO("onPostErrorInfo"),
    ON_POST_WARNING_INFO("onPostWarningInfo"),
    ON_POST_MESSAGE_INFO("onPostMessageInfo"),
    ON_LEAVE_STATE("onLeaveState"),
    ON_FINALIZE("onFinalize"),
    KILL("kill"),
    SOCKET_CLOSE("socketClose"),
    RUN_SCRIPT("runScript"),
    ;

    public String getState() {
        return state;
    }

    private final String state;

    public static ProcessState getStateByValue(String value) {
        for(ProcessState processState:ProcessState.values()){
            if(processState.getState().equals(value)){
                return processState;
            }
        }
        return null;
    }


}
