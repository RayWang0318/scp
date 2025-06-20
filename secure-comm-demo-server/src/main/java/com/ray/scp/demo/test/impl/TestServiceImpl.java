package com.ray.scp.demo.test.impl;

import com.ray.scp.context.ScpService;
import com.ray.scp.demo.test.TestService;

public class TestServiceImpl implements TestService,ScpService<String, String> {
    /**
     * 加密通道方法
     *
     * @param request
     * @return
     */
    @Override
    public String channel(String request) {
        return "receive hello world form client,response from server!";
    }
}
