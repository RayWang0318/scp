package com.ray.scp.sdk;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ray.scp.envelop.*;
import org.bouncycastle.asn1.bc.BCObjectIdentifiers;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;

import java.io.IOException;
import java.util.*;

public interface PqcSdk {

    /**
     * 生成后量子密钥对 Dilithium3
     * @return publicKey 和 privateKey
     */
    Map<String, byte[]> generatePqcSignKeyPair();

    /**
     * 生成后量子证书生成请求，
     * @param  privateKey 私钥
     * @param  publicKey  公钥
     * @param  dn         证书dn项
     * @return 返回证书请求的asn1结构
     */
    default byte[] generateP10Request(byte[] privateKey , byte[] publicKey, String dn){
        return null;
    }

    /**
     * 生成后量子证书
     * @param certificateRequest 签名请求
     * @return 签名后的证书
     */
    default byte[] generateCertificate(byte[] certificateRequest){
        return null;
    }

    /**
     * 验证 后量子证书
     * parse certificate x509 解析身份信息,签名公钥
     * @param certificate 后量子证书
     * @param rootCertificate 根证书
     * @return true or false
     */
    default boolean validateCertificate(byte[] certificate, byte[] rootCertificate){
        return false;
    }

    /**
     * 生成后量子密钥对 kyber768
     * @return publicKey 和 privateKey
     */
    Map<String, byte[]> generatePqcKemKeyPair();

    /**
     * 密钥协商
     * @param publicKey kyber公钥
     * @return sharedSecret 和  ciphertext
     */
    Map<String, byte[]> kemEncaps(byte[] publicKey);

    /**
     * 获取 会话密钥
     * @param ciphertext 协商出来的会话密钥密文
     * @param privateKey kyber私钥
     * @return sharedSecret 会话密钥
     */
    byte[] kemDecaps(byte[] ciphertext, byte[] privateKey);

    /**
     * 加密数字信封
     * 数字信封结构：
     * {
     *   "SignedAndEnvelopedData": {
     *     "version": 1,
     *     "recipientInfos": [
     *       {
     *         "version": 1,
     *         "issuerAndSerialNumber": {
     *           "issuer": "CN=CA Authority, O=Org, C=CN",
     *           "serialNumber": "12345678ABCDEF"
     *         },
     *         "keyEncryptionAlgorithm": {
     *           "algorithm": "1.3.6.1.4.1.22554.5.6.2",  // kyber-768
     *           "parameters": null
     *         },
     *         "encryptedKey": "BASE64(后量子密钥协商 ciphertext kyber-768)"
     *       }
     *     ],
     *     "digestAlgorithms": [
     *       {
     *         "algorithm": "1.2.156.10197.1.401",  // SM3
     *         "parameters": null
     *       }
     *     ],
     *     "encryptedContentInfo": {
     *       "contentType": "1.2.156.10197.6.1.4.2.1",
     *       "contentEncryptionAlgorithm": {
     *         "algorithm": "1.2.156.10197.1.104", // SM4
     *         "parameters": null
     *       },
     *       "encryptedContent": "BASE64(使用SM4加密后的原文)"
     *     },
     *     "certificates": [
     *       "BASE64(X.509证书1)"
     *       // 可选，多个证书（比如签名者证书、公钥证书）
     *     ],
     *     "signerInfos": [
     *       {
     *         "version": 1,
     *         "issuerAndSerialNumber": {
     *           "issuer": "CN=Sign CA, O=Org, C=CN",
     *           "serialNumber": "5566778899"
     *         },
     *         "digestAlgorithm": {
     *           "algorithm": "1.2.156.10197.1.401", // SM3
     *           "parameters": null
     *         },
     *         "digestEncryptionAlgorithm": {
     *           "algorithm": "1.3.6.1.4.1.2.267.7", // Dilithium3 签名算法
     *           "parameters": null
     *         },
     *         "encryptedDigest": "BASE64(Dilithium3签名值)",
     *         "authenticatedAttributes": null,     // 可选（如时间戳、数据Hash）
     *         "unauthenticatedAttributes": null    // 可选（扩展用途）
     *       }
     *     ]
     *   }
     * }
     * @param message        需要加密的消息
     * @param sharedSecret   会话密钥
     * @param ciphertext     会话密钥kyber密文
     * @param certificate     后量子证书 x509
     * @return SignedAndEnvelopedData 数字信封
     */
    default byte[] encryptEnvelope(byte[] message, byte[] sharedSecret, byte[] ciphertext, byte[] certificate, byte[] dilithiumPrivateKey){
        if (message == null || message.length == 0 || sharedSecret == null || sharedSecret.length == 0 || ciphertext == null || ciphertext.length == 0) {
            return null;
        }
        EncryptedContentInfo encryptedContentInfo = buildEncryptedContentInfo(message, sharedSecret);
        List<String> certificates = new ArrayList<>();
        String certificateBase64 = null;
        if(certificate != null && certificate.length > 0){
            certificateBase64 = Base64.getEncoder().encodeToString(certificate);
            certificates.add(certificateBase64);
        }
        List<RecipientInfo> recipientInfos = buildRecipientInfo(certificate, ciphertext);

        AlgorithmIdentifier digestAlgorithms = new AlgorithmIdentifier();
        digestAlgorithms.setAlgorithm(GMObjectIdentifiers.sm3.getId());
        digestAlgorithms.setParameters(null);
        List<AlgorithmIdentifier> digestAlgorithmIdentifiers = new ArrayList<>();
        digestAlgorithmIdentifiers.add(digestAlgorithms);

        List<SignerInfo> signerInfos = buildSignerInfo(encryptedContentInfo, certificate, dilithiumPrivateKey);

        ObjectMapper objectMapper = new ObjectMapper();
        byte[] result = null;
        // 构建返回对象
        SignedAndEnvelopedData signedAndEnvelopedData = new SignedAndEnvelopedData();
        signedAndEnvelopedData.setVersion(1);
        signedAndEnvelopedData.setRecipientInfos(recipientInfos);
        signedAndEnvelopedData.setDigestAlgorithmIdentifiers(digestAlgorithmIdentifiers);
        signedAndEnvelopedData.setEncryptedContentInfo(encryptedContentInfo);
        signedAndEnvelopedData.setCertificate(certificates);
        signedAndEnvelopedData.setSignerInfos(signerInfos);
        try {
            // 序列化
            result = objectMapper.writeValueAsBytes(signedAndEnvelopedData);
        } catch (JsonProcessingException e) {
            System.err.println("SignedAndEnvelopedData 对象序列化错误。");
        }
        return result;
    }

    default EncryptedContentInfo buildEncryptedContentInfo(byte[] message, byte[] sharedSecret) {
        EncryptedContentInfo encryptedContentInfo = new EncryptedContentInfo();
        encryptedContentInfo.setContentType(PKCSObjectIdentifiers.encryptedData.getId());
        AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier();
        algorithmIdentifier.setAlgorithm(GMObjectIdentifiers.sms4_ecb.getId());
        algorithmIdentifier.setParameters(null);
        encryptedContentInfo.setContentEncryptionAlgorithm(algorithmIdentifier);
        byte[] encrypt = sm4Encrypt(message, sharedSecret);
        encryptedContentInfo.setEncryptedContent(Base64.getEncoder().encodeToString(encrypt));
        return encryptedContentInfo;
    }

    default List<RecipientInfo> buildRecipientInfo(byte[] certificate, byte[] ciphertext) {
        List<RecipientInfo> recipientInfos = new ArrayList<>();
        RecipientInfo recipientInfo = new RecipientInfo();
        recipientInfo.setVersion(1);
        if(certificate != null && certificate.length > 0){
            IssuerAndSerialNumber issuerAndSerialNumber = parseCertIssuerAndSerialNumber(certificate);
            recipientInfo.setIssuerAndSerialNumber(issuerAndSerialNumber);
        }
        AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier();
        algorithmIdentifier.setAlgorithm(BCObjectIdentifiers.kyber768.getId());
        algorithmIdentifier.setParameters(null);
        recipientInfo.setKeyEncryptionAlgorithm(algorithmIdentifier);
        recipientInfo.setEncryptedKey(Base64.getEncoder().encodeToString(ciphertext));
        recipientInfos.add(recipientInfo);
        return recipientInfos;
    }

    default IssuerAndSerialNumber parseCertIssuerAndSerialNumber(byte[] certificate) {
        if(certificate == null || certificate.length == 0){
            return null;
        }
        IssuerAndSerialNumber issuerAndSerialNumber = new IssuerAndSerialNumber();
        try {
            X509CertificateHolder certHolder = new X509CertificateHolder(certificate);
            String issuer = certHolder.getIssuer().toString();
            issuerAndSerialNumber.setIssuer(issuer);
            issuerAndSerialNumber.setSerialNumber(certHolder.getSerialNumber().toString());
        } catch (IOException e) {
            System.err.println("解析证书失败："+e.getMessage());
        }
        return issuerAndSerialNumber;
    }

    default List<SignerInfo> buildSignerInfo(EncryptedContentInfo encryptedContentInfo, byte[] certificate, byte[] dilithiumPrivateKey){
        List<SignerInfo> signerInfoList = new ArrayList<>();
        AlgorithmIdentifier digestAlgorithm = new AlgorithmIdentifier();
        digestAlgorithm.setAlgorithm(GMObjectIdentifiers.sm3.getId());
        digestAlgorithm.setParameters(null);
        AlgorithmIdentifier digestEncryptionAlgorithm = new AlgorithmIdentifier();
        digestEncryptionAlgorithm.setAlgorithm(BCObjectIdentifiers.dilithium3.getId());
        digestEncryptionAlgorithm.setParameters(null);
        // 构建 authenticatedAttributes
        List<Map<String, Object>> authenticatedAttributes = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,Object> messageDigestMap = new HashMap<>();
        byte[] contentDigest = null;
        try {
            contentDigest = sm3Digest(objectMapper.writeValueAsBytes(encryptedContentInfo));
        } catch (JsonProcessingException e) {
            System.err.println("EncryptedContentInfo 序列化报错");
        }
        messageDigestMap.put(CMSAttributes.messageDigest.getId(), contentDigest);
        Map<String,Object> certDigestMap = new HashMap<>();
        byte[] certDigest = sm3Digest(certificate);
        certDigestMap.put(PKCSObjectIdentifiers.id_aa_signingCertificateV2.getId(), certDigest);
        authenticatedAttributes.add(messageDigestMap);
        authenticatedAttributes.add(certDigestMap);

        // 对authenticatedAttributes进行签名
        byte[] authenticatedAttributesBytes = null;
        try {
            authenticatedAttributesBytes = objectMapper.writeValueAsBytes(authenticatedAttributes);
        } catch (JsonProcessingException e) {
            System.err.println("对authenticatedAttributes 序列化报错");
        }
        byte[] signedAttributes = this.pqcSign(authenticatedAttributesBytes, dilithiumPrivateKey);
        String encryptedDigest = Base64.getEncoder().encodeToString(signedAttributes);

        // 构建 SignerInfo
        SignerInfo signerInfo = new SignerInfo();
        signerInfo.setVersion(1);
        signerInfo.setIssuerAndSerialNumber(parseCertIssuerAndSerialNumber(certificate));
        signerInfo.setDigestAlgorithm(digestAlgorithm);
        signerInfo.setDigestEncryptionAlgorithm(digestEncryptionAlgorithm);
        signerInfo.setAuthenticatedAttributes(authenticatedAttributes);
        signerInfo.setEncryptedDigest(encryptedDigest);
        signerInfoList.add(signerInfo);
        return signerInfoList;
    }

    /**
     * 解密数字信封
     * @param signedAndEnvelopedData  数字信封
     * @param sharedSecret            会话密钥
     * @param ciphertext              会话密钥kyber密文
     * @return message                解密后的消息
     */
    default byte[] decryptEnvelope(byte[] signedAndEnvelopedData, byte[] sharedSecret, byte[] ciphertext, byte[] dilithiumPublicKey){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SignedAndEnvelopedData envelopedData = objectMapper.readValue(signedAndEnvelopedData, SignedAndEnvelopedData.class);

            // 验签
            List<SignerInfo> signerInfos = envelopedData.getSignerInfos();
            if (CollUtil.isNotEmpty(signerInfos)) {
                SignerInfo signerInfo = signerInfos.get(0);
                boolean verified = verifySignature(signerInfo, dilithiumPublicKey);
                if (!verified) {
                    System.err.println("数字信封验签失败: 签名校验不通过");
                    return null;
                }
            }

            // 解密内容
            EncryptedContentInfo encryptedContentInfo = envelopedData.getEncryptedContentInfo();
            String encryptedContentBase64 = encryptedContentInfo.getEncryptedContent();

            if (StrUtil.isEmpty(encryptedContentBase64)) {
                System.err.println("encryptedContent 为空");
                return null;
            }

            byte[] encryptedContentBytes = Base64.getDecoder().decode(encryptedContentBase64);

            // 解密处理
            return this.sm4Decrypt(encryptedContentBytes, sharedSecret);

        } catch (IOException e) {
            System.err.println("反序列化数字信封失败");
        } catch (Exception e) {
            System.err.println("数字信封处理异常");
        }

        return null;
    }

    default boolean verifySignature(SignerInfo signerInfo, byte[] dilithiumPublicKey) {
        boolean result = false;
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] message = null;
        try {
            message = objectMapper.writeValueAsBytes(signerInfo.getAuthenticatedAttributes());
        } catch (JsonProcessingException e) {
            System.err.println("verifySignature error:" + e.getMessage());
        }
        String encryptedDigest = signerInfo.getEncryptedDigest();
        byte[] signature = Base64.getDecoder().decode(encryptedDigest);
        result = this.pqcVerify(message, signature, dilithiumPublicKey);
        return result;
    }

    /**
     * 验证签名
     * @param message
     * @param signature
     * @param publicKey
     * @return
     */
    boolean pqcVerify(byte[] message, byte[] signature, byte[] publicKey);

    /**
     * 签名
     * @param message
     * @param privateKey
     * @return
     */
    byte[] pqcSign(byte[] message, byte[] privateKey);

    /**
     * sm4 加密
     * @param message
     * @param sharedSecret
     * @return
     */
    byte[] sm4Encrypt(byte[] message, byte[] sharedSecret);

    /**
     * sm4 解密
     * @param encryptMessage
     * @param sharedSecret
     * @return
     */
    byte[] sm4Decrypt(byte[] encryptMessage, byte[] sharedSecret);

    /**
     * sm3 摘要
     * @param message
     * @return
     */
    byte[] sm3Digest(byte[] message);

    /**
     * 通过会话密钥获取sm4密钥 kyber协商密钥32字节，sm4密钥16字节，统一截取前16字节作为sm4密钥
     * @param sharedSecret
     * @return
     */
    default byte[] getSm4KeyBySharedSecret(byte[] sharedSecret){
        if (sharedSecret == null || sharedSecret.length < 16) {
            return null;
        }
        byte[] sm4Key = new byte[16];
        System.arraycopy(sharedSecret, 0, sm4Key, 0, 16);
        return sm4Key;
    }

    /**
     * 通过会话密钥获取sm3密钥 kyber协商密钥32字节，可以作为sm3 hmac的密钥
     * @param sharedSecret
     * @return
     */
    default byte[] getHmacKeyBySharedSecret(byte[] sharedSecret){
        return sharedSecret;
    }
}
