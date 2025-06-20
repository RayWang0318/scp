package com.ray.scp.protocol.brpc.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ray.scp.config.ServerConfiguration;
import com.ray.scp.context.RawPayloadHolder;
import com.ray.scp.context.ScpService;
import com.ray.scp.context.ScpServiceMeta;
import com.ray.scp.context.ScpServiceRegistry;
import com.ray.scp.enums.ErrorCode;
import com.ray.scp.exceptions.ScpException;
import com.ray.scp.protocol.brpc.data.DataTransferProto;
import com.ray.scp.protocol.brpc.service.DataTransferService;
import com.ray.scp.sdk.PqcSdk;
import com.ray.scp.sdk.keys.DilithiumKeyPair;
import com.ray.scp.session.SessionHolder;
import com.ray.scp.session.SessionRegistry;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Set;

public class DataTransferServiceImpl implements DataTransferService {

    private PqcSdk pqcSdk;

    private ServerConfiguration serverConfiguration;

    private SessionRegistry sessionRegistry;

    private ScpServiceRegistry scpServiceRegistry;

    public DataTransferServiceImpl(PqcSdk pqcSdk, ServerConfiguration serverConfiguration) {
        this.pqcSdk = pqcSdk;
        this.serverConfiguration = serverConfiguration;
        this.sessionRegistry = SessionRegistry.getInstance();
        this.scpServiceRegistry = ScpServiceRegistry.getInstance();
        registerService();
    }

    private void registerService(){
        String serviceScanPath = serverConfiguration.getServiceScanPath();
        Reflections reflections = new Reflections(serviceScanPath, Scanners.SubTypes);

        Set<Class<? extends ScpService>> serviceClasses = reflections.getSubTypesOf(ScpService.class);

        for (Class<? extends ScpService> clazz : serviceClasses) {
            try {
                ScpService<?, ?> instance = clazz.getDeclaredConstructor().newInstance();
                Type reqType = instance.getRequestType();
                Type respType = instance.getResponseType();
                scpServiceRegistry.register(clazz.getName(),
                        new ScpServiceMeta(instance, reqType, respType));

            } catch (Exception e) {
                System.err.println("注册失败: " + clazz.getName() + ", 原因: " + e.getMessage());
            }
        }
    }

    /**
     * 发送加密数据请求，并接收加密数据响应
     *
     * @param request
     * @return
     */
    @Override
    public DataTransferProto.EncryptedDataResponse SendEncrypted(DataTransferProto.EncryptedDataRequest request) {
        String sessionId = request.getSessionId();
        SessionHolder sessionHolder = sessionRegistry.getSession(sessionId);
        validateSession(sessionHolder);

        String serviceName = request.getServiceName();
        ScpServiceMeta serviceMeta = scpServiceRegistry.get(serviceName);
        if (ObjectUtil.isEmpty(serviceMeta)) {
            throw ScpException.of(ErrorCode.SERVICE_NOT_FOUND);
        }

        byte[] signedAndEnvelopedData = request.getPayload().toByteArray();
        byte[] decryptedBytes = pqcSdk.decryptEnvelope(
                signedAndEnvelopedData,
                sessionHolder.getSharedSecret(),
                sessionHolder.getCiphertext(),
                sessionHolder.getClientDilithiumPublicKey()
        );

        byte[] respBytes;
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Type reqType = serviceMeta.getReqType();
            Type respType = serviceMeta.getRespType();

            JavaType reqJavaType = objectMapper.getTypeFactory().constructType(reqType);
            JavaType respJavaType = objectMapper.getTypeFactory().constructType(respType);

            Object req = null;

            // 判断解密前非空但解密后是空
            if (signedAndEnvelopedData.length > 0 && (decryptedBytes == null || decryptedBytes.length == 0)) {
                // 可以选择透传原始密文给服务
                req = new RawPayloadHolder(signedAndEnvelopedData);
            } else if (decryptedBytes != null && decryptedBytes.length > 0) {
                req = objectMapper.readValue(decryptedBytes, reqJavaType);
            }

            @SuppressWarnings("unchecked")
            ScpService<Object, Object> service = (ScpService<Object, Object>) serviceMeta.getInstance();
            Object resp = service.channel(req);

            respBytes = objectMapper.writerFor(respJavaType).writeValueAsBytes(resp);
        } catch (IOException e) {
            throw ScpException.of(ErrorCode.SERIALIZATION_ERROR);
        }

        // 返回值使用数字信封
        byte[] encryptEnvelope = pqcSdk.encryptEnvelope(respBytes, sessionHolder.getSharedSecret(), sessionHolder.getCiphertext(), serverConfiguration.getDeviceCert(), DilithiumKeyPair.getInstance().getPrivateKey());

        return DataTransferProto.EncryptedDataResponse.newBuilder()
                .setSessionId(sessionHolder.getSessionId())
                .setPayload(ByteString.copyFrom(encryptEnvelope))
                .setSuccess(ErrorCode.SUCCESS.getCode())
                .build();
    }

    /**
     * 校验当前会话是否有效
     * @return
     */
    private boolean validateSession(SessionHolder sessionHolder) {
        boolean valid = ObjectUtil.isNotEmpty(sessionHolder)
                && !sessionHolder.isExpired()
                && BooleanUtil.isTrue(sessionHolder.isReadyFlag());

        if (!valid) {
//            sessionHolder = null;
        }

        return valid;
    }


}
