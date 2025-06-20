package com.ray.scp.protocol.brpc.service;

import com.ray.scp.protocol.brpc.data.DataTransferProto;
import com.baidu.brpc.protocol.BrpcMeta;

public interface DataTransferService {

    /**
     * 发送加密数据请求，并接收加密数据响应
     * @param request
     * @return
     */
    @BrpcMeta(serviceName = "cn.pqctech.scp.protocol.brpc.service.DataTransferService", methodName = "SendEncrypted")
    DataTransferProto.EncryptedDataResponse SendEncrypted(DataTransferProto.EncryptedDataRequest request);
}
