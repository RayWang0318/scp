package com.ray.scp.session;

import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {

    // 内部静态类实现懒加载单例
    private static class Holder {
        private static final SessionRegistry INSTANCE = new SessionRegistry();
    }

    // 单例访问方法
    public static SessionRegistry getInstance() {
        return Holder.INSTANCE;
    }

    // 私有构造方法，防止外部实例化
    private SessionRegistry() {}

    // 会话存储结构
    private final ConcurrentHashMap<String, SessionHolder> sessions = new ConcurrentHashMap<>();

    // 添加会话
    public void addSession(String sessionId, SessionHolder session) {
        sessions.put(sessionId, session);
    }

    // 获取会话（并在过期时清除）
    public SessionHolder getSession(String sessionId) {
        SessionHolder session = sessions.get(sessionId);
        if (session != null && session.isExpired()) {
            sessions.remove(sessionId);
            return null;
        }
        return session;
    }

    // 移除会话
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    // 判断是否存在会话
    public boolean contains(String sessionId) {
        SessionHolder session = sessions.get(sessionId);
        if (session != null && session.isExpired()) {
            sessions.remove(sessionId);
            return false;
        }
        return session != null;
    }

    // 当前会话数量
    public int size() {
        return sessions.size();
    }

    // 手动清理过期会话
    public void cleanupExpiredSessions() {
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}