package com.ray.scp.protocol.brpc.interceptor;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ray.scp.enums.ErrorCode;
import com.ray.scp.exceptions.ScpException;
import com.ray.scp.protocol.brpc.data.DataTransferProto;
import com.ray.scp.sdk.PqcSdk;
import com.ray.scp.sdk.keys.DilithiumKeyPair;
import com.ray.scp.session.SessionHolder;
import com.ray.scp.session.SessionRegistry;
import com.baidu.brpc.exceptions.RpcException;
import com.baidu.brpc.interceptor.AbstractInterceptor;
import com.baidu.brpc.interceptor.InterceptorChain;
import com.baidu.brpc.protocol.Request;
import com.baidu.brpc.protocol.Response;

/**
 * 数字信封的编码和解码拦截器 client 端用
 */
public class CodecInterceptor extends AbstractInterceptor {

    private PqcSdk pqcSdk;

    public CodecInterceptor(PqcSdk pqcSdk) {
        this.pqcSdk = pqcSdk;
    }

    // 拦截请求和返回值，对payload部分进行数字信封的加密解密
    @Override
    public void aroundProcess(Request request, Response response, InterceptorChain interceptorChain) throws RpcException {
        String method = getAbsoluteMethodName(request);
        if (method.equals("cn.pqctech.scp.protocol.brpc.service.DataTransferService/SendEncrypted")) {
            Object[] args = request.getArgs();
            if (args != null && args.length > 0 && args[0] instanceof DataTransferProto.EncryptedDataRequest) {
                DataTransferProto.EncryptedDataRequest protoRequest = (DataTransferProto.EncryptedDataRequest) args[0];
                String sessionId = protoRequest.getSessionId();
                byte[] originalPayload = protoRequest.getPayload().toByteArray();

                SessionHolder sessionHolder = SessionRegistry.getInstance().getSession(sessionId);
                if (ObjectUtil.isEmpty(sessionHolder)) {
                    throw ScpException.of(ErrorCode.SESSION_NOT_EXIST);
                }

                byte[] encryptedPayload = pqcSdk.encryptEnvelope(
                        originalPayload,
                        sessionHolder.getSharedSecret(),
                        sessionHolder.getCiphertext(),
                        sessionHolder.getCertificate(),
                        DilithiumKeyPair.getInstance().getPrivateKey()
                );

                // 替换为新的请求对象
                DataTransferProto.EncryptedDataRequest newRequest = protoRequest.toBuilder()
                        .setPayload(com.google.protobuf.ByteString.copyFrom(encryptedPayload))
                        .build();
                request.setArgs(new Object[]{ newRequest });
            }

            // 执行原调用链
            interceptorChain.intercept(request, response);

            // 解密响应
            Object value = response.getResult();
            if (value instanceof DataTransferProto.EncryptedDataResponse) {
                DataTransferProto.EncryptedDataResponse encryptedResp = (DataTransferProto.EncryptedDataResponse) value;
                String sessionId = encryptedResp.getSessionId();
                SessionHolder sessionHolder = SessionRegistry.getInstance().getSession(sessionId);
                int success = encryptedResp.getSuccess();
                if (success == 0) {
                    byte[] encryptedPayload = encryptedResp.getPayload().toByteArray();
                    if (ArrayUtil.isNotEmpty(encryptedPayload)) {
                        byte[] decrypted = pqcSdk.decryptEnvelope(
                                encryptedPayload,
                                sessionHolder.getSharedSecret(),
                                sessionHolder.getCiphertext(),
                                sessionHolder.getServerDilithiumPublicKey()
                        );
                        if (ArrayUtil.isEmpty(decrypted)) {
                            throw ScpException.of(ErrorCode.DIGITAL_ENVELOPE_DECRYPTION_ERROR);
                        }

                        // 构造新的响应对象
                        DataTransferProto.EncryptedDataResponse newResp = encryptedResp.toBuilder()
                                .setPayload(com.google.protobuf.ByteString.copyFrom(decrypted))
                                .build();
                        response.setResult(newResp);
                    }
                } else {
                    throw ScpException.of(ErrorCode.getErrorCode(success));
                }
            }

        } else {
            interceptorChain.intercept(request, response);
        }
    }

    private String getAbsoluteMethodName(Request request) {
        return request.getServiceName() + "/" + request.getMethodName();
    }
}
