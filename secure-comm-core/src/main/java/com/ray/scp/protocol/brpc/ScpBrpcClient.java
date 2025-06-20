package com.ray.scp.protocol.brpc;


import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ray.scp.client.ScpClient;
import com.ray.scp.constants.ScpConstant;
import com.ray.scp.enums.ErrorCode;
import com.ray.scp.exceptions.ScpException;
import com.ray.scp.protocol.brpc.config.BrpcClientConfiguration;
import com.ray.scp.protocol.brpc.data.DataTransferProto;
import com.ray.scp.protocol.brpc.handshake.HandshakeProto;
import com.ray.scp.protocol.brpc.interceptor.CodecInterceptor;
import com.ray.scp.protocol.brpc.service.DataTransferService;
import com.ray.scp.protocol.brpc.service.HandshakeService;
import com.ray.scp.sdk.PqcSdk;
import com.ray.scp.sdk.keys.DilithiumKeyPair;
import com.ray.scp.session.SessionHolder;
import com.ray.scp.session.SessionRegistry;
import com.baidu.brpc.RpcContext;
import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.RpcClientOptions;
import com.baidu.brpc.client.channel.Endpoint;
import com.baidu.brpc.interceptor.Interceptor;
import com.google.protobuf.ByteString;
import com.ray.scp.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScpBrpcClient extends ScpClient {

    private PqcSdk pqcSdk = null;

    public ScpBrpcClient(String host, int port, BrpcClientConfiguration clientConfiguration) {
        super(host, port, clientConfiguration);
        this.pqcSdk = clientConfiguration.getPqcSdk();
    }

    private Endpoint getServerEndpoint(){
        return new Endpoint(host, port);
    }

    @Override
    protected void connectInit() throws ScpException {
        this.sessionHolder = new SessionHolder();
        sessionHolder.setSessionId(RandomUtils.secureRandomString());

        HandshakeProto.ConnectInitRequest.Builder builder = HandshakeProto.ConnectInitRequest.newBuilder()
                .setSessionId(sessionHolder.getSessionId())
                .setEnableMutualAuth(clientConfiguration.isEnableMutualAuth());

        byte[] certBytes = clientConfiguration.getDeviceCert();
        if (certBytes != null) {
            builder.setCert(ByteString.copyFrom(certBytes));
        }
        byte[] dilithiumPublicKey = DilithiumKeyPair.getInstance().getPublicKey();
        if (dilithiumPublicKey != null) {
            builder.setClientDilithiumPublicKey(ByteString.copyFrom(dilithiumPublicKey));
        }
        HandshakeProto.ConnectInitRequest request = builder.build();
        RpcClient rpcClient = getRpcClient();
        HandshakeService proxy = BrpcProxy.getProxy(rpcClient, HandshakeService.class);
        HandshakeProto.ConnectInitResponse response = null;
        try {
            response = proxy.ConnectInit(request);
        } catch (Exception e) {
            e.printStackTrace();
            // 关键修改：在这里抛出 ScpException，避免后续 NPE
            // 原始的 RpcException 才是根本问题，我们应该在这里终止流程
            throw ScpException.of(ErrorCode.BRPC_CALL_FAILED.getCode(), "BRPC ConnectInit failed: " + e.getMessage(), e);
            // 您可能需要定义一个 BRPC_CALL_FAILED 的 ErrorCode
        }

        // 在这里添加 null 检查，以防万一 proxy.ConnectInit 返回 null (虽然 BrpcProxy 一般不会)
        if (response == null) {
            throw ScpException.of(ErrorCode.BRPC_RESPONSE_NULL.getCode(), "BRPC ConnectInit returned null response.");
        }
        if(0 != response.getSuccess()){
            throw ScpException.of(ErrorCode.AUTH_FAILED);
        }
        // 如果服务端开启了双向认证，则这边需要对后量子证书进行身份鉴别，如果没有开启，则直接跳过
        if(BooleanUtil.isTrue(response.getEnableMutualAuth())){
            byte[] cert = response.getCert().toByteArray();
            if(!pqcSdk.validateCertificate(cert, clientConfiguration.getRootCert())){
                throw ScpException.of(ErrorCode.AUTH_FAILED);
            }

        }
        sessionHolder.setServerDilithiumPublicKey(response.getServerDilithiumPublicKey().toByteArray());
        rpcClient.stop();
    }

    @Override
    protected void exchangePostQuantumKey() throws ScpException {
        Map<String, byte[]> kyberKeyPair = pqcSdk.generatePqcKemKeyPair();
        byte[] kyberPublicKey = kyberKeyPair.get(ScpConstant.PK_LABEL);
        byte[] kyberPrivateKey = kyberKeyPair.get(ScpConstant.SK_LABEL);
        if(kyberKeyPair == null || kyberKeyPair.isEmpty()){
            throw ScpException.of(ErrorCode.KYBER_KEY_PAIR_GENERATE_FAILED);
        }
        sessionHolder.setKyberPublicKey(kyberPublicKey);
        sessionHolder.setKyberPrivateKey(kyberPrivateKey);
        byte[] signature = pqcSdk.pqcSign(kyberPublicKey, DilithiumKeyPair.getInstance().getPrivateKey());
        HandshakeProto.PostQuantumKeyRequest request = HandshakeProto.PostQuantumKeyRequest.newBuilder()
                .setSessionId(sessionHolder.getSessionId())
                .setClientKyberPublicKey(ByteString.copyFrom(kyberPublicKey))
                .setSignature(ByteString.copyFrom(signature))
                .build();
        RpcClient rpcClient = getRpcClient();
        HandshakeService proxy = BrpcProxy.getProxy(rpcClient, HandshakeService.class);
        HandshakeProto.PostQuantumKeyResponse response = proxy.ExchangePostQuantumKey(request);
        // 处理 密钥交换返回值
        if(0 != response.getSuccess()){
            throw ScpException.of(ErrorCode.EXCHANGE_KEY_FAILED);
        }
        byte[] ciphertext = response.getCiphertext().toByteArray();
        sessionHolder.setCiphertext(ciphertext);
        boolean verify = pqcSdk.pqcVerify(ciphertext, response.getSignature().toByteArray(), sessionHolder.getServerDilithiumPublicKey());
        if(BooleanUtil.isFalse(verify)){
            throw ScpException.of(ErrorCode.EXCHANGE_KEY_FAILED);
        }
        byte[] sharedSecret = pqcSdk.kemDecaps(ciphertext, kyberPrivateKey);
        sessionHolder.setSharedSecret(sharedSecret);
        rpcClient.stop();
    }

    @Override
    protected void verifyChallenge() throws ScpException {
        byte[] challengePlainBytes = RandomUtils.secureRandomBytes(32);
        byte[] encryptedChallenge = pqcSdk.sm4Encrypt(challengePlainBytes, sessionHolder.getSharedSecret());
        HandshakeProto.ChallengeRequest request = HandshakeProto.ChallengeRequest.newBuilder()
                .setSessionId(sessionHolder.getSessionId())
                .setEncryptedChallenge(ByteString.copyFrom(encryptedChallenge))
                .build();
        RpcClient rpcClient = getRpcClient();
        HandshakeService proxy = BrpcProxy.getProxy(rpcClient, HandshakeService.class);
        HandshakeProto.ChallengeResponse response = proxy.VerifyChallenge(request);
        if(0 != response.getSuccess()){
            throw ScpException.of(ErrorCode.VERIFY_CHALLENGE_FAILED);
        }
        byte[] decryptedChallenge = response.getDecryptedChallenge().toByteArray();
        if(!ArrayUtil.equals(challengePlainBytes, decryptedChallenge)){
            throw ScpException.of(ErrorCode.VERIFY_CHALLENGE_FAILED);
        }
        rpcClient.stop();
    }

    @Override
    protected void saveSession() throws ScpException {
        sessionHolder.setReadyFlag(true);
        SessionRegistry.getInstance().addSession(sessionHolder.getSessionId(), sessionHolder);
    }

    /**
     * 发送加密信息
     * @param service
     * @param requestBytes
     * @return
     */
    @Override
    public byte[] sendSecureMessage(String service, byte[] requestBytes){
        if (BooleanUtil.isFalse(validateSession())) {
            throw ScpException.of(ErrorCode.SESSION_INVALID);
        }

        String sessionId = sessionHolder.getSessionId();

        // ✅ 把 sessionId 放到 kvAttachment 中，供服务端拦截器使用
        RpcContext.getContext().setRequestKvAttachment("sessionId", sessionId);

        DataTransferProto.EncryptedDataRequest request = DataTransferProto.EncryptedDataRequest.newBuilder()
                .setSessionId(sessionId)
                .setServiceName(service)
                .setPayload(ByteString.copyFrom(requestBytes))
                .build();

        RpcClient rpcClient = getRpcClient();
        DataTransferService proxy = BrpcProxy.getProxy(rpcClient, DataTransferService.class);
        DataTransferProto.EncryptedDataResponse encryptedDataResponse = proxy.SendEncrypted(request);
        rpcClient.stop();

        return encryptedDataResponse.getPayload().toByteArray();
    }

    private RpcClient getRpcClient(){
        Endpoint serverEndpoint = getServerEndpoint();
        RpcClientOptions rpcClientOptions = buildRpcClientOptions();
        List<Interceptor> interceptorList = new ArrayList<>();
        CodecInterceptor codecInterceptor = new CodecInterceptor(pqcSdk);
        interceptorList.add(codecInterceptor);
        return new RpcClient(serverEndpoint, rpcClientOptions, interceptorList);
    }

    /**
     * 构建RpcClientOptions
     * @return
     */
    private RpcClientOptions buildRpcClientOptions(){
        if (!(clientConfiguration instanceof BrpcClientConfiguration)) {
            // 如果不是 BrpcServerConfiguration 实例，说明配置有误，抛出异常
            throw ScpException.of(ErrorCode.CLIENT_CONFIG_ERROR.getCode(), "Server configuration is not an instance of BrpcServerConfiguration. Cannot build RpcServerOptions.");
        }

        // 强制转换为 BrpcServerConfiguration 类型，这样就可以访问其特有方法
        BrpcClientConfiguration brpcConfig = (BrpcClientConfiguration) clientConfiguration;
        RpcClientOptions clientOption = new RpcClientOptions();
        clientOption.setProtocolType(brpcConfig.getProtocolType());
        clientOption.setConnectTimeoutMillis(brpcConfig.getConnectTimeoutMillis());
        clientOption.setReadTimeoutMillis(brpcConfig.getReadTimeoutMillis());
        clientOption.setWriteTimeoutMillis(brpcConfig.getWriteTimeoutMillis());
        clientOption.setChannelType(brpcConfig.getChannelType());
        clientOption.setMaxTotalConnections(brpcConfig.getMaxTotalConnections());
        clientOption.setMinIdleConnections(brpcConfig.getMinIdleConnections());
        clientOption.setLoadBalanceType(brpcConfig.getLoadBalanceType());
        clientOption.setIoThreadNum(brpcConfig.getIoThreadNum());
        clientOption.setWorkThreadNum(brpcConfig.getWorkThreadNum());
        clientOption.setGlobalThreadPoolSharing(brpcConfig.isGlobalThreadPoolSharing());
        clientOption.setHealthyCheckIntervalMillis(brpcConfig.getHealthyCheckIntervalMillis());
        return clientOption;
    }

    /**
     * 校验当前会话是否有效
     * @return
     */
    private boolean validateSession() {
        boolean valid = ObjectUtil.isNotEmpty(sessionHolder)
                && !sessionHolder.isExpired()
                && BooleanUtil.isTrue(sessionHolder.isReadyFlag());

        if (!valid) {
//            sessionHolder = null;
        }

        return valid;
    }
}
