package com.ray.scp.session;


import com.ray.scp.cache.ExpiringCache;

import java.util.Map;

public class SessionHolder {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * kyber-768 协商的 共享密钥
     */
    private byte[] sharedSecret;

    /**
     * kyber-768 协商的 共享密钥 密文
     */
    private byte[] ciphertext;

    /**
     * 用户信息,从证书里面解析出来的，比如公司的基本信息
     */
    private Map<String, Object> userInfo;

    /**
     * 创建时间 毫秒
     */
    private long createTime;

    /**
     * 是否就绪
     */
    private boolean readyFlag;

    /**
     * 服务端 dilithium 公钥，协商过程中client端从sever端获取，client端后续用于给消息签名
     */
    private byte[] serverDilithiumPublicKey;

    /**
     * 客户端 dilithium 公钥，协商过程中sever端从client端获取，server端后续用于给消息签名
     */
    private byte[] clientDilithiumPublicKey;

    /**
     * kyber 公钥
     */
    private byte[] kyberPublicKey;

    /**
     * kyber 私钥
     */
    private byte[] kyberPrivateKey;

    /**
     * server端或者client的后量子证书
     */
    private byte[] certificate;

    /**
     * 防止重放攻击，过期时间5分钟
     */
    private final ExpiringCache<String, Boolean> usedNonces = new ExpiringCache<String, Boolean>( 5 * 60 * 1000);
    /**
     * 过期时间 毫秒 1d 过期
     */
//    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;


    public SessionHolder() {
        this.createTime = System.currentTimeMillis();
        this.readyFlag = false;
    }

    /**
     * 判断会话是否过期
     * @return
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - createTime > EXPIRE_TIME;
    }

    /**
     * 判断nonce是否已经使用过
     * @param nonce
     * @return
     */
    public boolean isUsedNonce(String nonce) {
        return usedNonces.containsKey(nonce);
    }

    /**
     * 记录已经使用过的nonce
     * @param nonce
     */
    public void addUsedNonce(String nonce) {
        usedNonces.put(nonce, true);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Map<String, Object> getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Map<String, Object> userInfo) {
        this.userInfo = userInfo;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public boolean isReadyFlag() {
        return readyFlag;
    }

    public void setReadyFlag(boolean readyFlag) {
        this.readyFlag = readyFlag;
    }

    public ExpiringCache<String, Boolean> getUsedNonces() {
        return usedNonces;
    }

    public byte[] getServerDilithiumPublicKey() {
        return serverDilithiumPublicKey;
    }

    public void setServerDilithiumPublicKey(byte[] serverDilithiumPublicKey) {
        this.serverDilithiumPublicKey = serverDilithiumPublicKey;
    }

    public byte[] getClientDilithiumPublicKey() {
        return clientDilithiumPublicKey;
    }

    public void setClientDilithiumPublicKey(byte[] clientDilithiumPublicKey) {
        this.clientDilithiumPublicKey = clientDilithiumPublicKey;
    }

    public byte[] getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(byte[] sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public byte[] getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(byte[] ciphertext) {
        this.ciphertext = ciphertext;
    }

    public byte[] getKyberPublicKey() {
        return kyberPublicKey;
    }

    public void setKyberPublicKey(byte[] kyberPublicKey) {
        this.kyberPublicKey = kyberPublicKey;
    }

    public byte[] getKyberPrivateKey() {
        return kyberPrivateKey;
    }

    public void setKyberPrivateKey(byte[] kyberPrivateKey) {
        this.kyberPrivateKey = kyberPrivateKey;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }
}
