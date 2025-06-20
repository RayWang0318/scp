package com.ray.scp.sdk.keys;

import com.ray.scp.constants.ScpConstant;
import com.ray.scp.sdk.PqcSdk;
import com.ray.scp.sdk.impl.PqcSdkClientProvider;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Dilithium密钥对，单例模式
 */
public class DilithiumKeyPair {

    private String publicKey = "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";

    private String privateKey = "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";

    // 私有构造函数，防止外部 new
    private DilithiumKeyPair() {
        initKeys();
    }

    // 单例实例，volatile 确保多线程下可见性
    private static volatile DilithiumKeyPair instance;

    /**
     * 获取唯一实例（双重检查锁，线程安全，懒加载）
     */
    public static DilithiumKeyPair getInstance() {
        if (instance == null) {
            synchronized (DilithiumKeyPair.class) {
                if (instance == null) {
                    instance = new DilithiumKeyPair();
                }
            }
        }
        return instance;
    }

    // Getter 和 Setter
    public byte[] getPublicKey() {
        return Base64.getDecoder().decode(publicKey);
    }

    public byte[] getPrivateKey() {
        return Base64.getDecoder().decode(privateKey);
    }

    private void initKeys() {
        try {
            PqcSdk pqcSdk = new PqcSdkClientProvider();
            Map<String, byte[]> keyMap = pqcSdk.generatePqcSignKeyPair();

            Optional.ofNullable(keyMap.get(ScpConstant.PK_LABEL))
                    .filter(pk -> pk.length > 0)
                    .ifPresent(pk -> this.publicKey = Base64.getEncoder().encodeToString(pk));

            Optional.ofNullable(keyMap.get(ScpConstant.SK_LABEL))
                    .filter(sk -> sk.length > 0)
                    .ifPresent(sk -> this.privateKey = Base64.getEncoder().encodeToString(sk));

        } catch (Exception ignore) {
            // 可替换为日志框架记录
            System.err.println("Dilithium密钥初始化失败，使用默认密钥对：" + ignore.getMessage());
            // e.printStackTrace(); // 调试时可以打开
        }
    }

    public static void main(String[] args) {
        DilithiumKeyPair instance1 = DilithiumKeyPair.getInstance();
        instance1.getPublicKey();
        instance1.getPrivateKey();
    }
}