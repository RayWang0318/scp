package com.ray.scp.config;

import com.ray.scp.sdk.PqcSdk;

public abstract class ClientConfiguration {

    protected PqcSdk pqcSdk;

    /**
     * 是否开启 证书双向认证
     */
    protected boolean enableMutualAuth = false;

    /**
     * 设备证书
     */
    protected byte[] deviceCert;

    /**
     * ca 根证书
     */
    protected byte[] rootCert;

    public ClientConfiguration() {
    }

    public boolean isEnableMutualAuth() {
        return enableMutualAuth;
    }

    public void setEnableMutualAuth(boolean enableMutualAuth) {
        this.enableMutualAuth = enableMutualAuth;
    }

    public byte[] getDeviceCert() {
        return deviceCert;
    }

    public void setDeviceCert(byte[] deviceCert) {
        this.deviceCert = deviceCert;
    }

    public byte[] getRootCert() {
        return rootCert;
    }

    public void setRootCert(byte[] rootCert) {
        this.rootCert = rootCert;
    }

    public PqcSdk getPqcSdk() {
        return pqcSdk;
    }

    public void setPqcSdk(PqcSdk pqcSdk) {
        this.pqcSdk = pqcSdk;
    }
}
