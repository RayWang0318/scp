package com.ray.scp.protocol.brpc;


import cn.hutool.core.util.ObjectUtil;
import com.ray.scp.enums.ErrorCode;
import com.ray.scp.exceptions.ScpException;
import com.ray.scp.protocol.brpc.config.BrpcServerConfiguration;
import com.ray.scp.protocol.brpc.interceptor.SessionAuthInterceptor;
import com.ray.scp.protocol.brpc.service.impl.DataTransferServiceImpl;
import com.ray.scp.protocol.brpc.service.impl.HandshakeServiceImpl;
import com.ray.scp.sdk.PqcSdk;
import com.ray.scp.server.ScpServer;
import com.baidu.brpc.interceptor.Interceptor;
import com.baidu.brpc.server.RpcServer;
import com.baidu.brpc.server.RpcServerOptions;

import java.util.ArrayList;
import java.util.List;

public class ScpBrpcServer extends ScpServer {

    private RpcServer rpcServer;

    private PqcSdk pqcSdk;

    public ScpBrpcServer() {
        super();
        init();
    }

    public ScpBrpcServer(int port, BrpcServerConfiguration serverConfiguration) {
        super(port, serverConfiguration);
        this.serverConfiguration = serverConfiguration;
        this.pqcSdk = serverConfiguration.getPqcSdk();
        init();
    }

    private void init(){
        if(ObjectUtil.isEmpty(serverConfiguration)){
            serverConfiguration = BrpcServerConfiguration.builder().build();
        }
        RpcServerOptions rpcServerOptions = buildRpcServerOptions();
        // 设置拦截器
        List<Interceptor> interceptorList = new ArrayList<>();
        SessionAuthInterceptor sessionAuthInterceptor = new SessionAuthInterceptor();
        interceptorList.add(sessionAuthInterceptor);
        rpcServer = new RpcServer(port, rpcServerOptions, interceptorList);
        rpcServer.registerService(new HandshakeServiceImpl(pqcSdk, this.serverConfiguration));
        rpcServer.registerService(new DataTransferServiceImpl(pqcSdk, this.serverConfiguration));
    }

    @Override
    public void start() {
        rpcServer.start();
    }

    private RpcServerOptions buildRpcServerOptions(){
        if (!(serverConfiguration instanceof BrpcServerConfiguration)) {
            // 如果不是 BrpcServerConfiguration 实例，说明配置有误，抛出异常
            throw ScpException.of(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "Server configuration is not an instance of BrpcServerConfiguration. Cannot build RpcServerOptions.");
        }

        // 强制转换为 BrpcServerConfiguration 类型，这样就可以访问其特有方法
        BrpcServerConfiguration brpcConfig = (BrpcServerConfiguration) serverConfiguration;
        RpcServerOptions options = new RpcServerOptions();
        options.setIoThreadNum(brpcConfig.getIoThreadNum());
        options.setWorkThreadNum(brpcConfig.getWorkThreadNum());
        options.setProtocolType(brpcConfig.getProtocolType());
        options.setNamingServiceUrl(brpcConfig.getNamingServiceUrl());
        options.setGlobalThreadPoolSharing(brpcConfig.isGlobalThreadPoolSharing());
        return options;
    }
}
