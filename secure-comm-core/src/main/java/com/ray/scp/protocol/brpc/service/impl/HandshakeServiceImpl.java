package com.ray.scp.protocol.brpc.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.ray.scp.config.ServerConfiguration;
import com.ray.scp.constants.ScpConstant;
import com.ray.scp.enums.ErrorCode;
import com.ray.scp.exceptions.ScpException;
import com.ray.scp.protocol.brpc.handshake.HandshakeProto;
import com.ray.scp.protocol.brpc.service.HandshakeService;
import com.ray.scp.sdk.PqcSdk;
import com.ray.scp.sdk.keys.DilithiumKeyPair;
import com.ray.scp.session.SessionHolder;
import com.ray.scp.session.SessionRegistry;
import com.google.protobuf.ByteString;

import java.util.Map;

public class HandshakeServiceImpl implements HandshakeService {

    private PqcSdk pqcSdk;

    private ServerConfiguration serverConfiguration;

    private SessionRegistry sessionRegistry;

    public HandshakeServiceImpl() {
    }

    public HandshakeServiceImpl(PqcSdk pqcSdk, ServerConfiguration serverConfiguration) {
        this.pqcSdk = pqcSdk;
        this.serverConfiguration = serverConfiguration;
        this.sessionRegistry = SessionRegistry.getInstance();
    }

    /**
     * 第一步：客户端初始化握手，发送证书和 Dilithium 公钥
     *
     * @param request
     * @return
     */
    @Override
    public HandshakeProto.ConnectInitResponse ConnectInit(HandshakeProto.ConnectInitRequest request) {
        SessionHolder sessionHolder = new SessionHolder();
        sessionHolder.setSessionId(request.getSessionId());
        // 客户端开启双向鉴别，则需要验证证书
        if(BooleanUtil.isTrue(request.getEnableMutualAuth())){
            byte[] cert = request.getCert().toByteArray();
            if(!pqcSdk.validateCertificate(cert, serverConfiguration.getRootCert())){
                throw ScpException.of(ErrorCode.AUTH_FAILED);
            }
        }
        sessionHolder.setClientDilithiumPublicKey(request.getClientDilithiumPublicKey().toByteArray());
        sessionHolder.setCertificate(request.getCert().toByteArray());
        sessionRegistry.addSession(sessionHolder.getSessionId(), sessionHolder);
        HandshakeProto.ConnectInitResponse.Builder responseBuilder = HandshakeProto.ConnectInitResponse.newBuilder()
                .setSessionId(sessionHolder.getSessionId())
                .setEnableMutualAuth(this.serverConfiguration.isEnableMutualAuth())
                .setSuccess(ErrorCode.SUCCESS.getCode())
                .setServerDilithiumPublicKey(ByteString.copyFrom(DilithiumKeyPair.getInstance().getPublicKey()));

        byte[] deviceCert = this.serverConfiguration.getDeviceCert();
        if (deviceCert != null) {
            responseBuilder.setCert(ByteString.copyFrom(deviceCert));
        }

        return responseBuilder.build();
    }

    /**
     * 第二步：客户端发送 Kyber 公钥，服务端返回加密密钥密文
     *
     * @param request
     * @return
     */
    @Override
    public HandshakeProto.PostQuantumKeyResponse ExchangePostQuantumKey(HandshakeProto.PostQuantumKeyRequest request) {
        SessionHolder sessionHolder = getSessionHolder(request.getSessionId());
        byte[] kyberPublicKey = request.getClientKyberPublicKey().toByteArray();
        byte[] signature = request.getSignature().toByteArray();
        if(!pqcSdk.pqcVerify(kyberPublicKey, signature, sessionHolder.getClientDilithiumPublicKey())){
            throw ScpException.of(ErrorCode.AUTH_FAILED);
        }
        sessionHolder.setKyberPublicKey(kyberPublicKey);
        Map<String, byte[]> keyMap = pqcSdk.kemEncaps(kyberPublicKey);
        byte[] ciphertext = keyMap.get(ScpConstant.CIPHERTEXT_LABEL);
        byte[] sharedSecret = keyMap.get(ScpConstant.SHARDED_SECRET_LABEL);
        sessionHolder.setSharedSecret(sharedSecret);
        sessionHolder.setCiphertext(ciphertext);
        byte[] responseSignature = pqcSdk.pqcSign(ciphertext, DilithiumKeyPair.getInstance().getPrivateKey());
        boolean b = pqcSdk.pqcVerify(ciphertext, responseSignature, DilithiumKeyPair.getInstance().getPublicKey());
        if(!b){
            throw ScpException.of(ErrorCode.AUTH_FAILED);
        }


        HandshakeProto.PostQuantumKeyResponse response = HandshakeProto.PostQuantumKeyResponse.newBuilder()
                .setSessionId(sessionHolder.getSessionId())
                .setCiphertext(ByteString.copyFrom(ciphertext))
                .setSignature(ByteString.copyFrom(responseSignature))
                .setSuccess(ErrorCode.SUCCESS.getCode())
                .build();
        return response;
    }

    /**
     * 第三步：服务端发送 challenge，客户端解密并回应，确认双方 sessionKey 一致
     *
     * @param request
     * @return
     */
    @Override
    public HandshakeProto.ChallengeResponse VerifyChallenge(HandshakeProto.ChallengeRequest request) {
        SessionHolder sessionHolder = getSessionHolder(request.getSessionId());
        byte[] encryptedChallenge = request.getEncryptedChallenge().toByteArray();
        byte[] sharedSecret = sessionHolder.getSharedSecret();
        byte[] decryptedChallenge = pqcSdk.sm4Decrypt(encryptedChallenge, sharedSecret);
        HandshakeProto.ChallengeResponse response = HandshakeProto.ChallengeResponse.newBuilder()
                .setSessionId(sessionHolder.getSessionId())
                .setDecryptedChallenge(ByteString.copyFrom(decryptedChallenge))
                .setSuccess(ErrorCode.SUCCESS.getCode())
                .build();
        sessionHolder.setReadyFlag(true);
        return response;
    }

    /**
     * 获取sessionHolder
     * @param sessionId
     * @return
     */
    private SessionHolder getSessionHolder(String sessionId) {
        SessionHolder sessionHolder = sessionRegistry.getSession(sessionId);
        if(sessionHolder == null){
            throw ScpException.of(ErrorCode.SESSION_NOT_EXIST);
        }
        return sessionHolder;
    }
}
