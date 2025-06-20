package com.ray.scp.exceptions;

import com.baidu.brpc.exceptions.RpcException;
import com.ray.scp.enums.ErrorCode;

import java.io.Serializable;

/**
 * 安全通信协议异常
 * 继承 RpcException，方便 BRPC 传递错误码和消息给客户端
 */
public class ScpException extends RpcException implements Serializable {

    private static final long serialVersionUID = 4404387110504272361L;

    /**
     * 错误码
     */
    protected Integer errorCode;

    /**
     * 错误信息
     */
    protected String message;

    public ScpException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
        super.setCode(errorCode); // RpcException有errorCode字段，调用setter
    }

    public ScpException(Integer errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
        super.setCode(errorCode);
    }

    public ScpException(String message) {
        super(message);
        this.message = message;
        super.setCode(-1); // 默认错误码
    }

    public ScpException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        super.setCode(-1);
    }

    public ScpException(Throwable cause) {
        super(cause);
        super.setCode(-1);
    }

    public static ScpException of(String message){
        return new ScpException(message);
    }

    public static ScpException of(Integer code, String message){
        return new ScpException(code, message);
    }

    public static ScpException of(String message, Throwable cause){
        return new ScpException(message, cause);
    }

    public static ScpException of(Integer code, String message, Throwable cause){
        return new ScpException(code, message, cause);
    }

    public static ScpException of(Throwable cause){
        return new ScpException(cause);
    }

    public static ScpException of(ErrorCode errorCode){
        return new ScpException(errorCode.getCode(), errorCode.getMessage());
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}