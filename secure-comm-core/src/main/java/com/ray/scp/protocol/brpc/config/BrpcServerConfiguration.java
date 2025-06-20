package com.ray.scp.protocol.brpc.config;

import com.ray.scp.config.ServerConfiguration;
import com.ray.scp.sdk.PqcSdk;

public class BrpcServerConfiguration extends ServerConfiguration {

    /**
     * IO线程数，默认是CPU核数
     */
    private final int ioThreadNum;

    /**
     * 工作线程数，默认是CPU核数
     */
    private final int workThreadNum;

    /**
     * 接收client的协议类型，是ProtocolType枚举值，默认为空，表示接收所有支持的类型。
     * 可以使用 brpc的 Options.ProtocolType 枚举
     */
    private final Integer protocolType;

    /**
     * 注册中心地址，当不为空时，server会向该地址注册实例，默认为空。
     */
    private final String namingServiceUrl;

    /**
     * 多个server实例是否共享线程池，默认否
     */
    private final boolean globalThreadPoolSharing;

    private BrpcServerConfiguration(Builder builder) {
        this.ioThreadNum = builder.ioThreadNum;
        this.workThreadNum = builder.workThreadNum;
        this.protocolType = builder.protocolType;
        this.namingServiceUrl = builder.namingServiceUrl;
        this.globalThreadPoolSharing = builder.globalThreadPoolSharing;
        super.setEnableMutualAuth(builder.enableMutualAuth);
        super.setDeviceCert(builder.deviceCert);
        super.setRootCert(builder.rootCert);
        super.setServiceScanPath(builder.serviceScanPath);
        super.setPqcSdk(builder.pqcSdk);
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getIoThreadNum() {
        return ioThreadNum;
    }

    public int getWorkThreadNum() {
        return workThreadNum;
    }

    public Integer getProtocolType() {
        return protocolType;
    }

    public String getNamingServiceUrl() {
        return namingServiceUrl;
    }

    public boolean isGlobalThreadPoolSharing() {
        return globalThreadPoolSharing;
    }

    public static class Builder {
        /**
         * IO线程数，默认是CPU核数
         */
        private int ioThreadNum = Runtime.getRuntime().availableProcessors();

        /**
         * 工作线程数，默认是CPU核数
         */
        private int workThreadNum = Runtime.getRuntime().availableProcessors();

        /**
         * 接收client的协议类型，是ProtocolType枚举值，默认为空，表示接收所有支持的类型。
         */
        private Integer protocolType = null;

        /**
         * 注册中心地址，当不为空时，server会向该地址注册实例，默认为空。
         */
        private String namingServiceUrl = null;

        /**
         * 多个server实例是否共享线程池，默认否
         */
        private boolean globalThreadPoolSharing = false;

        private boolean enableMutualAuth = false;

        private byte[] deviceCert;

        private byte[] rootCert;

        private String serviceScanPath;

        private PqcSdk pqcSdk;

        public Builder() {
        }

        public Builder ioThreadNum(int ioThreadNum) {
            this.ioThreadNum = ioThreadNum;
            return this;
        }

        public Builder workThreadNum(int workThreadNum) {
            this.workThreadNum = workThreadNum;
            return this;
        }

        public Builder protocolType(Integer protocolType) {
            this.protocolType = protocolType;
            return this;
        }

        public Builder namingServiceUrl(String namingServiceUrl) {
            this.namingServiceUrl = namingServiceUrl;
            return this;
        }

        public Builder globalThreadPoolSharing(boolean globalThreadPoolSharing) {
            this.globalThreadPoolSharing = globalThreadPoolSharing;
            return this;
        }

        public Builder enableMutualAuth(boolean enableMutualAuth) {
            this.enableMutualAuth = enableMutualAuth;
            return this;
        }

        public Builder deviceCert(byte[] deviceCert) {
            this.deviceCert = deviceCert;
            return this;
        }

        public Builder rootCert(byte[] rootCert) {
            this.rootCert = rootCert;
            return this;
        }

        public Builder serviceScanPath(String serviceScanPath) {
            this.serviceScanPath = serviceScanPath;
            return this;
        }

        public Builder pqcSdk(PqcSdk pqcSdk) {
            this.pqcSdk = pqcSdk;
            return this;
        }

        public BrpcServerConfiguration build() {
            if(this.pqcSdk == null){
                throw new IllegalArgumentException("PqcSdk can not be null");
            }
            return new BrpcServerConfiguration(this);
        }
    }
}