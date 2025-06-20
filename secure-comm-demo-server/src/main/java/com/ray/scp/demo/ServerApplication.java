package com.ray.scp.demo;

import com.ray.scp.protocol.brpc.ScpBrpcServer;
import com.ray.scp.protocol.brpc.config.BrpcServerConfiguration;
import com.ray.scp.sdk.impl.PqcSdkClientProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ray.scp.demo")
public class ServerApplication {

    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(ServerApplication.class, args);
        startScpServer();
    }

    private static void startScpServer(){
        BrpcServerConfiguration.Builder builder = new BrpcServerConfiguration.Builder();
        // 双向认证
        builder.enableMutualAuth(false);
        // ca根证书
        builder.rootCert(null);
        // 设备证书
        builder.deviceCert(null);
        builder.serviceScanPath("com.ray.scp.demo");
        builder.pqcSdk(new PqcSdkClientProvider());
        BrpcServerConfiguration configuration = builder.build();
        ScpBrpcServer scpServer = new ScpBrpcServer(8000, configuration);
        scpServer.start();
    }
}
