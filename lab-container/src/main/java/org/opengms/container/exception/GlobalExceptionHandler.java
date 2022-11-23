package org.opengms.container.exception;

import lombok.extern.slf4j.Slf4j;
import org.opengms.common.utils.StringUtils;
import org.opengms.container.entity.dto.ApiResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bin
 * @date 2022/08/24
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                           HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址[ {} ],不支持[ {} ]请求", requestURI, e.getMethod());
        return ApiResponse.error(e.getMessage());
    }
    
    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public ApiResponse handleServiceException(ServiceException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址[ {} ], 发生业务异常.", requestURI, e);
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) ? ApiResponse.error(code, e.getMessage()) : ApiResponse.error(e.getMessage());
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse handleRuntimeException(RuntimeException e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址[ {} ], 发生未知异常.", requestURI, e);
        return ApiResponse.error(e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse handleException(Exception e, HttpServletRequest request)
    {
        String requestURI = request.getRequestURI();
        log.error("请求地址[ {} ], 发生系统异常.", requestURI, e);
        return ApiResponse.error(e.getMessage());
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse handleBindException(BindException e, HttpServletRequest request)
    {
        // log.error(e.getMessage(), e);
        // String message = e.getAllErrors().get(0).getDefaultMessage();
        // return ApiResponse.error(message);
        String requestURI = request.getRequestURI();
        ApiResponse apiResponse = handleBindingResult(e.getBindingResult());
        log.error("请求地址[ {} ], 发生参数校验异常 'BindException' : {}", requestURI , apiResponse.get("msg"));
        return apiResponse;
    }

    /**
     * 自定义验证异常 参数校验异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request)
    {
        // log.error(e.getMessage(), e);
        // String message = e.getBindingResult().getFieldError().getDefaultMessage();
        // return ApiResponse.error(message);
        String requestURI = request.getRequestURI();
        ApiResponse apiResponse = handleBindingResult(e.getBindingResult());
        log.error("请求地址[ {} ], 发生参数校验异常 'MethodArgumentNotValidException' : {}", requestURI , apiResponse.get("msg"));
        return apiResponse;
    }

    /**
     * 处理参数校验异常信息
     * @param result
     * @return
     */
    private ApiResponse handleBindingResult(BindingResult result) {
        List<String> errorList = new ArrayList<>();
        if (result.hasErrors()) {
            List<FieldError> list = result.getFieldErrors();
            for (FieldError error : list) {
                String message = "<"+ error.getField() + ">校验不通过：" + error.getDefaultMessage();
                errorList.add(message);
            }
        }
        if (errorList.size() == 0) {
            return ApiResponse.error();
        }
        // A0002-参数校验异常
        return ApiResponse.error(result.getObjectName()+ ":" + errorList.toString());
    }
    
}
