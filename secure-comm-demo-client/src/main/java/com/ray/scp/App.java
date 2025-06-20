package com.ray.scp;

import com.ray.scp.protocol.brpc.ScpBrpcClient;
import com.ray.scp.protocol.brpc.config.BrpcClientConfiguration;
import com.ray.scp.sdk.impl.PqcSdkClientProvider;

import java.nio.charset.StandardCharsets;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) {
        BrpcClientConfiguration.Builder builder = BrpcClientConfiguration.builder();
        // ca根证书
        builder.rootCert(null);
        // 设备证书
        builder.deviceCert(null);
        // 双向认证
        builder.enableMutualAuth(false);
        builder.pqcSdk(new PqcSdkClientProvider());
        BrpcClientConfiguration configuration = builder.build();
        ScpBrpcClient scpBrpcClient = new ScpBrpcClient("127.0.0.1", 8000, configuration);
        try {
            boolean b = scpBrpcClient.initClient();
            if (b) {
                // 需要发送的服务完整名称 example: cn.pqctech.scp.context.ScpService
                String response = scpBrpcClient.sendSecureMessage("com.ray.scp.demo.test.impl.TestServiceImpl", "hello world!".getBytes(StandardCharsets.UTF_8), String.class);
                System.out.println(response);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
