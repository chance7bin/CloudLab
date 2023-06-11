package org.opengms.admin.service;

import cn.hutool.extra.spring.SpringUtil;
import org.opengms.common.utils.spring.SpringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

/**
 * @author 7bin
 * @date 2023/06/11
 */
@Service
public class TestService implements ITestService{

    @Override
    public void test1(){
        System.out.println("test1");
    }

    @Override
    public void test2(){
        Object o = AopContext.currentProxy();
        SpringUtils.getAopProxy(this).test1();
    }

}
