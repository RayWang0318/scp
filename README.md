
# Secure Communication Protocol (安全通信协议)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)  
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/RayWang0318/scp/actions)

---

## 项目简介 Overview

`secure-comm-root` 是一个基于 **bRPC** 通信框架，结合 **后量子密码学（Kyber）** 与 **国密算法（SM4）** 的安全通信协议项目。协议定义在应用层，确保在现有高性能 RPC 框架之上实现强抗量子攻击的加密通信，适合企业和开发者构建安全可靠的分布式系统。

---

## 核心功能 Features

- **后量子密钥交换（Kyber）**  
  利用后量子公钥算法，保障量子计算环境下的密钥安全。
  
- **国密 SM4 对称加密**  
  会话密钥采用国密 SM4 算法，加密通信内容。
  
- **基于 bRPC 通信**  
  应用层协议运行于高性能的 bRPC 框架之上，支持多服务调用。
  
- **数字信封机制**  
  会话密钥采用数字信封方式安全传输，提升密钥管理安全性。
  
- **模块化设计**  
  清晰分层，方便扩展和维护。

---

## 握手流程 Handshake Flow

下图展示了安全通信协议的握手流程，双方通过后量子算法 Kyber 交换密钥，并使用数字信封保护会话密钥，完成安全会话建立。

![握手流程](docs/握手流程.png)

---

## 项目结构 Project Structure

```
secure-comm-root/
├── secure-comm-common/            # 通用工具与基础组件
├── secure-comm-core/              # 核心加密实现（Kyber，SM4 等）
├── secure-comm-demo-client/       # 客户端示例
├── secure-comm-demo-server/       # 服务端示例
├── secure-comm-server-starter/    # 服务端启动模块
└── pom.xml                        # Maven 父工程配置
```

---

## 技术选型 Technical Details

| 技术            | 说明                               |
|-----------------|----------------------------------|
| JDK             | 8 及以上版本                       |
| 构建工具        | Apache Maven                     |
| 加密库          | 包含 Kyber 与 SM4 的后量子国密库  |
| 通信框架        | bRPC（基于 Google Protobuf）     |

---

## 快速开始 Build & Run

### 1. 克隆项目

```bash
git clone https://github.com/RayWang0318/scp.git
cd secure-comm-root
```

### 2. 编译构建

```bash
mvn clean install
```

### 3. 启动示例服务

#### 启动服务端

```bash
cd secure-comm-demo-server
# 运行服务端主类，例如：
mvn spring-boot:run
```

#### 启动客户端

```bash
cd secure-comm-demo-client
# 运行客户端主类，例如：
mvn exec:java -Dexec.mainClass="cn.pqctech.scp.MyClient"
```

> **提示**：详细启动步骤请参考各模块的 `README.md` 或源码注释。

---

## 使用示例 Usage Examples

### 客户端示例（bRPC）

```java
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

```

---

### 服务端示例（bRPC）

```java
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

```

---

## 许可证 License

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 开源许可。

---

## 贡献 Contributing

欢迎贡献代码和提出建议：

- 提交 Issue 反馈问题或需求
- 提交 Pull Request 改进功能或修复 Bug

请遵循项目编码规范，确保代码质量。

---

## 联系方式 Contact

如有疑问或建议，请联系
**QQ: 744429927
**邮箱**: 744429927@qq.com
**GitHub**: [https://github.com/RayWang0318/scp](https://github.com/RayWang0318/scp)

---

## 致谢 Acknowledgements

感谢开源社区提供的 Kyber 与国密算法实现，为本项目奠定了坚实的加密基础。
