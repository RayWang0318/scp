package com.ray.scp.context;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * ScpService 是一个用于加密通信请求处理的通用接口，所有服务处理器均需实现此接口。
 *
 * <p>
 * 设计目标是提供一种标准化的服务调用模型，使得在不依赖 Spring 等框架的情况下，
 * 也能通过类加载器扫描所有实现类，并根据类的完整限定名（如 {@code cn.xxx.YourServiceImpl}）进行路由分发。
 * </p>
 *
 * <p>
 * 使用方式：
 * <ul>
 *   <li>实现该接口，并指定泛型 {@code Req} 为请求类型，{@code Resp} 为响应类型</li>
 *   <li>类名必须唯一，建议以业务含义命名</li>
 *   <li>通过反射获取所有实现类，结合类的全限定名进行映射注册</li>
 * </ul>
 * </p>
 *
 * <p>
 * 示例实现：
 * <pre>{@code
 * public class cn.pqctech.scp.service.impl.SecureChannelService
 *     implements ScpService<EncryptedDataRequest, EncryptedDataResponse> {
 *
 *     @Override
 *     public EncryptedDataResponse channel(EncryptedDataRequest request) {
 *         // 加解密处理逻辑...
 *     }
 * }
 * }</pre>
 * </p>
 *
 * <p><b>备注：</b> 推荐使用类似 SPI 或 classpath 扫描机制注册所有实现类。</p>
 *
 * @param <Req> 请求类型
 * @param <Resp> 响应类型
 */
@FunctionalInterface
public interface ScpService<Req, Resp> {

    /**
     * 加密通道方法
     * @param request
     * @return
     */
    Resp channel(Req request);

    /**
     * 默认提供类型捕获能力，可被覆盖
     */
    default Type getRequestType() {
        return extractGenericTypes()[0];
    }

    default Type getResponseType() {
        return extractGenericTypes()[1];
    }

    /**
     * 提取当前类所实现的泛型接口的类型参数
     */
    default Type[] extractGenericTypes() {
        // 向上寻找泛型信息
        for (Type type : getClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                if (pt.getRawType() == ScpService.class) {
                    return pt.getActualTypeArguments();
                }
            }
        }
        throw new IllegalStateException("无法解析泛型类型: " + getClass().getName());
    }
}
