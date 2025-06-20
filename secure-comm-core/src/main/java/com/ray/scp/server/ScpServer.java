package com.ray.scp.server;

import com.ray.scp.config.ServerConfiguration;

public abstract class ScpServer {

    /**
     * 配置信息
     */
    protected ServerConfiguration serverConfiguration;

    /**
     * 端口号
     */
    protected int port = 8000;

    public ScpServer() {
    }

    public ScpServer(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    public void setServerConfiguration(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    public ScpServer(int port, ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public abstract void start();
}
