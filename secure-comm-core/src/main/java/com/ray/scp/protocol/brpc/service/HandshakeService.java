package com.ray.scp.protocol.brpc.service;

import com.ray.scp.protocol.brpc.handshake.HandshakeProto;
import com.baidu.brpc.protocol.BrpcMeta;

public interface HandshakeService {

    /**
     * 第一步：客户端初始化握手，发送证书和 Dilithium 公钥
     * @param request
     * @return
     */
    @BrpcMeta(serviceName = "cn.pqctech.scp.protocol.brpc.service.HandshakeService", methodName = "ConnectInit")
    HandshakeProto.ConnectInitResponse ConnectInit(HandshakeProto.ConnectInitRequest request);

    /**
     * 第二步：客户端发送 Kyber 公钥，服务端返回加密密钥密文
     * @param request
     * @return
     */
    @BrpcMeta(serviceName = "cn.pqctech.scp.protocol.brpc.service.HandshakeService", methodName = "ExchangePostQuantumKey")
    HandshakeProto.PostQuantumKeyResponse ExchangePostQuantumKey(HandshakeProto.PostQuantumKeyRequest request);


    /**
     * 第三步：服务端发送 challenge，客户端解密并回应，确认双方 sessionKey 一致
     * @param request
     * @return
     */
    @BrpcMeta(serviceName = "cn.pqctech.scp.protocol.brpc.service.HandshakeService", methodName = "VerifyChallenge")
    HandshakeProto.ChallengeResponse VerifyChallenge(HandshakeProto.ChallengeRequest request);
}
