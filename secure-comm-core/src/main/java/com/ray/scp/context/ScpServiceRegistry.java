package com.ray.scp.context;

import java.util.concurrent.ConcurrentHashMap;

public class ScpServiceRegistry {

    // k: 类全名（例如 cn.pqctech.scp.demo.test.impl.TestServiceImpl）
    private final ConcurrentHashMap<String, ScpServiceMeta> context = new ConcurrentHashMap<>();

    private ScpServiceRegistry() {}

    private static class Holder {
        private static final ScpServiceRegistry INSTANCE = new ScpServiceRegistry();
    }

    public static ScpServiceRegistry getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 注册一个服务（用类名作为 key）
     */
    public void register(String className, ScpServiceMeta serviceMeta) {
        context.put(className, serviceMeta);
    }

    /**
     * 获取注册的服务元信息
     */
    public ScpServiceMeta get(String className) {
        return context.get(className);
    }

    /**
     * 判断是否已注册
     */
    public boolean contains(String className) {
        return context.containsKey(className);
    }

    /**
     * 获取所有已注册服务元信息
     */
    public ConcurrentHashMap<String, ScpServiceMeta> getAll() {
        return context;
    }
}