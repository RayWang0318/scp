package com.ray.scp.sdk.impl;

import com.ray.scp.constants.ScpConstant;
import com.ray.scp.exceptions.ScpException;
import com.ray.scp.sdk.PqcSdk;
import org.bouncycastle.jcajce.SecretKeyWithEncapsulation;
import org.bouncycastle.jcajce.spec.KEMExtractSpec;
import org.bouncycastle.jcajce.spec.KEMGenerateSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class PqcSdkClientProvider implements PqcSdk {

    static {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new BouncyCastlePQCProvider());
        init();
    }

    private static void init(){

    }

    /**
     * 生成后量子密钥对 Dilithium3
     *
     * @return publicKey 和 privateKey
     */
    @Override
    public Map<String, byte[]> generatePqcSignKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("Dilithium", "BC");
            keyPairGenerator.initialize(DilithiumParameterSpec.dilithium3, new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            Map<String, byte[]> keyMap = new HashMap<>();
            keyMap.put(ScpConstant.PK_LABEL, keyPair.getPublic().getEncoded());   // X.509 格式
            keyMap.put(ScpConstant.SK_LABEL, keyPair.getPrivate().getEncoded()); // PKCS#8 格式
            return keyMap;
        } catch (Exception e) {
            throw new ScpException("生成 Dilithium 密钥对失败", e);
        }
    }

    /**
     * 生成后量子密钥对 kyber768
     *
     * @return publicKey 和 privateKey
     */
    @Override
    public Map<String, byte[]> generatePqcKemKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("Kyber", "BCPQC");
            kpg.initialize(KyberParameterSpec.kyber768);
            KeyPair kp = kpg.generateKeyPair();

            Map<String, byte[]> map = new HashMap<>();
            map.put(ScpConstant.PK_LABEL, kp.getPublic().getEncoded());
            map.put(ScpConstant.SK_LABEL, kp.getPrivate().getEncoded());
            return map;
        } catch (Exception e) {
            throw new ScpException("Kyber key pair gen failed", e);
        }
    }

    /**
     * 密钥协商
     *
     * @param publicKeyBytes kyber公钥
     * @return sharedSecret 和  ciphertext
     */
    @Override
    public Map<String, byte[]> kemEncaps(byte[] publicKeyBytes) {
        try {
            // 封装
            PublicKey pub = KeyFactory.getInstance("Kyber", "BCPQC")
                    .generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            KeyGenerator kg = KeyGenerator.getInstance("Kyber", "BCPQC");
            KEMGenerateSpec genSpec = new KEMGenerateSpec(pub, "SM4");
            kg.init(genSpec);

            SecretKeyWithEncapsulation skEnc = (SecretKeyWithEncapsulation)kg.generateKey();

            Map<String, byte[]> map = new HashMap<>();
            map.put(ScpConstant.SHARDED_SECRET_LABEL, skEnc.getEncoded());
            map.put(ScpConstant.CIPHERTEXT_LABEL, skEnc.getEncapsulation());
            return map;
        } catch (Exception e) {
            throw new ScpException("KEM encaps failed", e);
        }
    }

    /**
     * 获取 会话密钥
     *
     * @param ciphertext 协商出来的会话密钥密文
     * @param privateKeyBytes kyber私钥
     * @return sharedSecret 会话密钥
     */
    @Override
    public byte[] kemDecaps(byte[] ciphertext, byte[] privateKeyBytes) {
        try {
            PrivateKey priv = KeyFactory.getInstance("Kyber", "BCPQC")
                    .generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

            KeyGenerator kg = KeyGenerator.getInstance("Kyber", "BCPQC");
            KEMExtractSpec extractSpec = new KEMExtractSpec(priv, ciphertext, "SM4");
            kg.init(extractSpec);

            SecretKeyWithEncapsulation skEnc = (SecretKeyWithEncapsulation)kg.generateKey();
            return skEnc.getEncoded();
        } catch (Exception e) {
            throw new ScpException("KEM decaps failed", e);
        }
    }

    /**
     * 验证签名
     *
     * @param message
     * @param signatureBytes
     * @param publicKeyBytes
     * @return
     */
    @Override
    public boolean pqcVerify(byte[] message, byte[] signatureBytes, byte[] publicKeyBytes) {
        try {
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("Dilithium", "BC");
            PublicKey publicKey = keyFactory.generatePublic(pubSpec);

            Signature signature = Signature.getInstance("Dilithium", "BC");
            signature.initVerify(publicKey);
            signature.update(message);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            throw new ScpException("Dilithium 验签失败", e);
        }
    }

    /**
     * 签名
     *
     * @param message
     * @param privateKey
     * @return
     */
    @Override
    public byte[] pqcSign(byte[] message, byte[] privateKey) {
        try {
            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privateKey);
            KeyFactory keyFactory = KeyFactory.getInstance("Dilithium", "BC");
            PrivateKey DilithiumPrivateKey = keyFactory.generatePrivate(privSpec);

            Signature signature = Signature.getInstance("Dilithium", "BC");
            signature.initSign(DilithiumPrivateKey);
            signature.update(message);
            return signature.sign();
        } catch (Exception e) {
            throw new ScpException("Dilithium 签名失败", e);
        }
    }

    /**
     * sm4 加密
     * ecb 模式
     * @param message
     * @param sharedSecret
     * @return
     */
    @Override
    public byte[] sm4Encrypt(byte[] message, byte[] sharedSecret) {
        try {
            byte[] sm4Key = getSm4KeyBySharedSecret(sharedSecret);
            SecretKey key = new SecretKeySpec(sm4Key, "SM4");
            Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5Padding", BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(message);
        } catch (Exception e) {
            throw new ScpException("SM4加密失败", e);
        }
    }

    /**
     * sm4 解密
     * ecb 模式
     * @param encryptMessage
     * @param sharedSecret
     * @return
     */
    @Override
    public byte[] sm4Decrypt(byte[] encryptMessage, byte[] sharedSecret) {
        try {
            byte[] sm4Key = getSm4KeyBySharedSecret(sharedSecret);
            SecretKey key = new SecretKeySpec(sm4Key, "SM4");
            Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5Padding", BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptMessage);
        } catch (Exception e) {
            throw new ScpException("SM4解密失败", e);
        }
    }

    /**
     * sm3 摘要
     *
     * @param message
     * @return
     */
    @Override
    public byte[] sm3Digest(byte[] message) {
        if (message == null || message.length == 0) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SM3", BouncyCastleProvider.PROVIDER_NAME);
            return digest.digest(message);
        } catch (Exception e) {
            throw new ScpException("SM3摘要失败", e);
        }
    }
}
