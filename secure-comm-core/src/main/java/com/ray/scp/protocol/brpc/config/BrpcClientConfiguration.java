package com.ray.scp.protocol.brpc.config;

import com.ray.scp.config.ClientConfiguration;
import com.ray.scp.sdk.PqcSdk;
import com.baidu.brpc.client.channel.ChannelType;
import com.baidu.brpc.loadbalance.LoadBalanceStrategy;
import com.baidu.brpc.protocol.Options;

public class BrpcClientConfiguration extends ClientConfiguration {

    /**
     * 与server交互的协议类型，是ProtocolType枚举值，默认是PROTOCOL_BAIDU_STD_VALUE。
     */
    private final int protocolType;

    /**
     * 与server连接超时时间，单位毫秒，默认是1000ms。
     */
    private final int connectTimeoutMillis;

    /**
     * 读超时时间，单位毫秒，默认是1000ms。
     */
    private final int readTimeoutMillis;

    /**
     * 写超时时间，单位毫秒，默认是1000ms。
     */
    private final int writeTimeoutMillis;

    /**
     * 与server连接类型，是ChannelType枚举值，默认是POOLED_CONNECTION。
     */
    private final ChannelType channelType;

    /**
     * 最大连接数，仅当channelType是POOLED_CONNECTION时有效，默认是8。
     */
    private final int maxTotalConnections;

    /**
     * 最小空闲连接数，仅当channelType是POOLED_CONNECTION时有效，默认是8。
     */
    private final int minIdleConnections;

    /**
     * 负载均衡类型，是LoadBalanceStrategy，默认是LOAD_BALANCE_FAIR。
     */
    private final int loadBalanceType;

    /**
     * IO线程数，默认是CPU核数。
     */
    private final int ioThreadNum;

    /**
     * 工作线程数，默认是CPU核数。
     */
    private final int workThreadNum;

    /**
     * 多个client实例是否共享线程池，默认是false。
     */
    private final boolean globalThreadPoolSharing;

    /**
     * 健康检查间隔，单位毫秒，默认是3000ms。
     */
    private final int healthyCheckIntervalMillis;

    private BrpcClientConfiguration(Builder builder) {
        this.protocolType = builder.protocolType;
        this.connectTimeoutMillis = builder.connectTimeoutMillis;
        this.readTimeoutMillis = builder.readTimeoutMillis;
        this.writeTimeoutMillis = builder.writeTimeoutMillis;
        this.channelType = builder.channelType;
        this.maxTotalConnections = builder.maxTotalConnections;
        this.minIdleConnections = builder.minIdleConnections;
        this.loadBalanceType = builder.loadBalanceType;
        this.ioThreadNum = builder.ioThreadNum;
        this.workThreadNum = builder.workThreadNum;
        this.globalThreadPoolSharing = builder.globalThreadPoolSharing;
        this.healthyCheckIntervalMillis = builder.healthyCheckIntervalMillis;
        super.setEnableMutualAuth(builder.enableMutualAuth);
        super.setDeviceCert(builder.deviceCert);
        super.setRootCert(builder.rootCert);
        super.setPqcSdk(builder.pqcSdk);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int protocolType = Options.ProtocolType.PROTOCOL_BAIDU_STD_VALUE;
        private int connectTimeoutMillis = 1000;
        private int readTimeoutMillis = 50000;
        private int writeTimeoutMillis = 1000;
        private ChannelType channelType = ChannelType.POOLED_CONNECTION;
        private int maxTotalConnections = 1000;
        private int minIdleConnections = 10;
        private int loadBalanceType = LoadBalanceStrategy.LOAD_BALANCE_FAIR;
        private int ioThreadNum = Runtime.getRuntime().availableProcessors();
        private int workThreadNum = Runtime.getRuntime().availableProcessors();
        private boolean globalThreadPoolSharing = false;
        private int healthyCheckIntervalMillis = 3000;
        private boolean enableMutualAuth = false;
        private byte[] deviceCert = null;
        private byte[] rootCert = null;
        private PqcSdk pqcSdk = null;

        public Builder protocolType(int protocolType) {
            this.protocolType = protocolType;
            return this;
        }

        public Builder connectTimeoutMillis(int connectTimeoutMillis) {
            this.connectTimeoutMillis = connectTimeoutMillis;
            return this;
        }

        public Builder readTimeoutMillis(int readTimeoutMillis) {
            this.readTimeoutMillis = readTimeoutMillis;
            return this;
        }

        public Builder writeTimeoutMillis(int writeTimeoutMillis) {
            this.writeTimeoutMillis = writeTimeoutMillis;
            return this;
        }

        public Builder channelType(ChannelType channelType) {
            this.channelType = channelType;
            return this;
        }

        public Builder maxTotalConnections(int maxTotalConnections) {
            this.maxTotalConnections = maxTotalConnections;
            return this;
        }

        public Builder minIdleConnections(int minIdleConnections) {
            this.minIdleConnections = minIdleConnections;
            return this;
        }

        public Builder loadBalanceType(int loadBalanceType) {
            this.loadBalanceType = loadBalanceType;
            return this;
        }

        public Builder ioThreadNum(int ioThreadNum) {
            this.ioThreadNum = ioThreadNum;
            return this;
        }

        public Builder workThreadNum(int workThreadNum) {
            this.workThreadNum = workThreadNum;
            return this;
        }

        public Builder globalThreadPoolSharing(boolean globalThreadPoolSharing) {
            this.globalThreadPoolSharing = globalThreadPoolSharing;
            return this;
        }

        public Builder healthyCheckIntervalMillis(int healthyCheckIntervalMillis) {
            this.healthyCheckIntervalMillis = healthyCheckIntervalMillis;
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

        public Builder pqcSdk(PqcSdk pqcSdk) {
            this.pqcSdk = pqcSdk;
            return this;
        }

        public BrpcClientConfiguration build() {
            if(this.pqcSdk == null){
                throw new IllegalArgumentException("PqcSdk can not be null");
            }
            return new BrpcClientConfiguration(this);
        }
    }

    // Getter 方法
    public int getProtocolType() { return protocolType; }
    public int getConnectTimeoutMillis() { return connectTimeoutMillis; }
    public int getReadTimeoutMillis() { return readTimeoutMillis; }
    public int getWriteTimeoutMillis() { return writeTimeoutMillis; }
    public ChannelType getChannelType() { return channelType; }
    public int getMaxTotalConnections() { return maxTotalConnections; }
    public int getMinIdleConnections() { return minIdleConnections; }
    public int getLoadBalanceType() { return loadBalanceType; }
    public int getIoThreadNum() { return ioThreadNum; }
    public int getWorkThreadNum() { return workThreadNum; }
    public boolean isGlobalThreadPoolSharing() { return globalThreadPoolSharing; }
    public int getHealthyCheckIntervalMillis() { return healthyCheckIntervalMillis; }
}