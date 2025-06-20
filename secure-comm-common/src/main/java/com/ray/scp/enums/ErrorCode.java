package com.ray.scp.enums;

public enum ErrorCode {

    SUCCESS(0, "success"),
    SERVER_CONFIG_ERROR(10001, "server配置文件错误"),
    INTERNAL_SERVER_ERROR(10002, "服务器内部错误"),

    SESSION_INVALID(10003, "Session 不存在或已超时，请重新连接"),
    SERIALIZATION_ERROR(10004, "序列化错误"),
    CLIENT_CONFIG_ERROR(10005, "client配置文件错误"),
    AUTH_FAILED(10006, "认证失败"),
    KYBER_KEY_PAIR_GENERATE_FAILED(10007, "Kyber密钥对生成失败"),
    EXCHANGE_KEY_FAILED(10008, "密钥协商失败"),
    SESSION_EXPIRED(10009, "Session已过期，请重新连接"),
    VERIFY_CHALLENGE_FAILED(10010, "验证挑战失败"),
    SESSION_NOT_EXIST(10011, "会话不存在！"),
    SERVICE_NOT_FOUND(10012, "服务不存在！"),
    DIGITAL_ENVELOPE_DECRYPTION_ERROR(10013, "数字信封解密错误"),
    BRPC_CALL_FAILED (10014, "brpc调用失败"),
    BRPC_RESPONSE_NULL (10015, "brpc调用返回结果为空"),

    ;

    private int code;

    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ErrorCode getErrorCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}
