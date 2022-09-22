package org.opengms.admin.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.opengms.admin.annotation.Anonymous;
import org.opengms.admin.annotation.Log;
import org.opengms.admin.entity.po.system.SysOperLog;
import org.opengms.admin.entity.po.system.SysUser;
import org.opengms.admin.enums.BusinessType;
import org.opengms.admin.service.ISysOperLogService;
import org.opengms.admin.entity.dto.ApiResponse;
import org.opengms.admin.entity.dto.TableDataInfo;
import org.opengms.common.redis.RedisCache;
import org.opengms.admin.exception.ServiceException;
import org.opengms.common.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.opengms.admin.controller.common.BaseController;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @author bin
 * @date 2022/08/22
 */
@Api(tags = "测试接口")
@Slf4j
@RestController
@RequestMapping(value="/test")
public class TestController extends BaseController{

    @ApiOperation(value = "hello world" )
    @GetMapping("/hello")
    public String Hello(){
        return "第一个接口";
    }

    @Log(title = "测试@Log注解", businessType = BusinessType.OTHER)
    @GetMapping("/log")
    public String helloLog(){
        return "测试@Log注解，记录操作日志";
    }


    @Autowired
    private RedisCache redisCache;

    @Anonymous
    @GetMapping("/helloRedis")
    public String helloRedis(){
        // 测试写入一个string类型键值,过期时间20S
        redisCache.set("key1", "Gangbb", 20);
        return "测试redis";
    }

    @Cacheable("cache1")
    @GetMapping("/helloRedis2")
    public String helloRedis2(){
        return "xxredis";
    }

    @Cacheable("cache2")
    @GetMapping("/helloRedis3")
    public JSONObject helloRedis3(){
        return getTestDetail();
    }


    @GetMapping("/args")
    public String argsTest(@Validated @RequestBody SysUser sysUser){
        return "success";
    }

    public JSONObject getTestDetail() {
        JSONObject retJson = new JSONObject();
        String retCode = "1";
        String retMsg = "操作失败！";
        JSONObject bizDataJson = new JSONObject();
        try {
            SysUser user = new SysUser();
            user.setUserId(22L);
            user.setUserName("bin");
            user.setEmail("78280@qq.com");

            String key = "user::"+user.getUserId();
            //向Redis中缓存数据，-1为设置永久时效
            // redisUtils.set(key, user,-1);

            bizDataJson = JSONObject.parseObject(JSON.toJSONString(user));
            retCode = "0";
            retMsg = "操作成功！";
        } catch (Exception e) {
            log.error(String.valueOf(e));
        }
        retJson.put("retCode", retCode);
        retJson.put("retMsg", retMsg);
        retJson.put("bizData", bizDataJson);
        return retJson;
    }

    @GetMapping("/message")
    public String test() {
        return MessageUtils.message("user.login.success");
    }

    @GetMapping("/exception")
    public ApiResponse exception() {

        int a = 1;
        int b = 0;

        if (b == 0){
            throw new ServiceException(MessageUtils.message("service.division.byZero"));
        }

        return ApiResponse.success(a / 0.5);
    }


    @Autowired
    private ISysOperLogService sysOperLogService;

    /**
     * 查询列表，分页。 返回 TableDataInfo
     * @return
     */
    // @Anonymous
    @GetMapping("/page")
    public TableDataInfo testPageHelper(){

        startPage();
        List<SysOperLog> sysOperationLogs = sysOperLogService.selectAll();

        return getDataTable(sysOperationLogs);
    }

}
