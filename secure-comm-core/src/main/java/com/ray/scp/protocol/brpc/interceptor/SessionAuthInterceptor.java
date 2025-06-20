package com.ray.scp.protocol.brpc.interceptor;

import com.ray.scp.enums.ErrorCode;
import com.ray.scp.exceptions.ScpException;
import com.ray.scp.session.SessionRegistry;
import com.baidu.brpc.exceptions.RpcException;
import com.baidu.brpc.interceptor.Interceptor;
import com.baidu.brpc.interceptor.InterceptorChain;
import com.baidu.brpc.protocol.Request;
import com.baidu.brpc.protocol.Response;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SessionAuthInterceptor implements Interceptor {

    private static final Set<String> WHITE_LIST = new HashSet<>(Arrays.asList(
            "cn.pqctech.scp.protocol.brpc.service.HandshakeService/ConnectInit",
            "cn.pqctech.scp.protocol.brpc.service.HandshakeService/ExchangePostQuantumKey",
            "cn.pqctech.scp.protocol.brpc.service.HandshakeService/VerifyChallenge"
    ));

    @Override
    public boolean handleRequest(Request request) {
        return true;
    }

    @Override
    public void handleResponse(Response response) {

    }

    @Override
    public void aroundProcess(Request request, Response response, InterceptorChain interceptorChain) throws RpcException {
        String serviceName = getAbsoluteMethodName(request);
        if (WHITE_LIST.contains(serviceName)) {
            interceptorChain.intercept(request, response);
            return;
        }

        String sessionId = extractSessionId(request);
        if (!isValidSession(sessionId)) {
            // 抛出自定义异常，框架会包装并传回客户端
            throw ScpException.of(ErrorCode.SESSION_INVALID);
        }

        // session有效，继续调用后续拦截器或业务处理
        interceptorChain.intercept(request, response);
    }

    /**
     * 从 request 中提取 sessionId
     */
    private String extractSessionId(Request request) {
        // 根据实际 Request 结构解析出 sessionId，比如从 header 或 protobuf message 中
        Map<String, Object> kvAttachment = request.getKvAttachment();
        if (kvAttachment == null || !kvAttachment.containsKey("sessionId")) {
            // 可以打日志或抛业务异常
            System.err.println("Missing sessionId in kvAttachment");
            return null;
        }

        Object sessionIdObj = kvAttachment.get("sessionId");
        return sessionIdObj != null ? sessionIdObj.toString() : null;
    }

    private boolean isValidSession(String sessionId) {
        SessionRegistry instance = SessionRegistry.getInstance();
        // 你自己的 session 管理器逻辑
        return sessionId != null && instance.contains(sessionId);
    }

    /**
     * 获取方法名
     * @param request
     * @return
     */
    private String getAbsoluteMethodName(Request request) {
        return request.getServiceName() + "/" + request.getMethodName();
    }
}
