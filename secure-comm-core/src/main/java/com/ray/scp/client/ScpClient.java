package com.ray.scp.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ray.scp.config.ClientConfiguration;
import com.ray.scp.enums.ErrorCode;
import com.ray.scp.exceptions.ScpException;
import com.ray.scp.session.SessionHolder;

import java.io.IOException;

public abstract class ScpClient {

    protected String host;

    protected int port = 8000;

    protected SessionHolder sessionHolder;

    protected ClientConfiguration clientConfiguration;

    public ScpClient(String host, int port, ClientConfiguration clientConfiguration) {
        this.host = host;
        this.port = port;
        this.clientConfiguration = clientConfiguration;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    public void setClientConfiguration(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
    }

    public SessionHolder getSessionHolder() {
        return sessionHolder;
    }

    public void setSessionHolder(SessionHolder sessionHolder) {
        this.sessionHolder = sessionHolder;
    }

    /**
     * todo 需要在new client 的时候检查证书
     * 协议初始化 握手
     * @return
     */
    public boolean initClient(){
        try {
            // ====== 第一步：客户端初始化连接 ======
            connectInit();
            // ====== 第二步：客户端验证服务端证书并发送 PQ 公钥 ======
            exchangePostQuantumKey();
            // ====== 第三步：验证共享的 session key 是否一致 ======
            verifyChallenge();
            // ====== 第四步：保存会话session ======
            saveSession();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected abstract void connectInit() throws ScpException;

    protected abstract void exchangePostQuantumKey() throws ScpException;

    protected abstract void verifyChallenge() throws ScpException;

    protected abstract void saveSession() throws ScpException;

    /**
     * 发送加密信息
     * @param service 需要发送的服务完整名称 example: cn.pqctech.scp.context.ScpService
     * @param message 需要发送的消息内容
     * @return
     */
    public  <T, R> R sendSecureMessage(String service, T message, Class<R> responseType){
        try {
            ObjectMapper mapper = new ObjectMapper();

            // 序列化消息为字节数组
            byte[] requestBytes = mapper.writeValueAsBytes(message);

            // 发送并获取响应（你应替换为真实的发送逻辑）
            byte[] responseBytes = sendSecureMessage(service, requestBytes);

            // 反序列化响应为指定类型
            return mapper.readValue(responseBytes, responseType);
        } catch (IOException e) {
            throw ScpException.of(ErrorCode.SERIALIZATION_ERROR);
        }
    }

    /**
     * 发送加密信息
     * @param service
     * @param requestBytes
     * @return
     */
    public abstract byte[] sendSecureMessage(String service, byte[] requestBytes);
}
