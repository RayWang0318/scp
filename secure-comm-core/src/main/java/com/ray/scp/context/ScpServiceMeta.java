package com.ray.scp.context;

import java.lang.reflect.Type;

public class ScpServiceMeta {

    private final ScpService<?, ?> instance;
    private final Type reqType;
    private final Type respType;

    public ScpServiceMeta(ScpService<?, ?> instance, Type reqType, Type respType) {
        this.instance = instance;
        this.reqType = reqType;
        this.respType = respType;
    }

    public ScpService<?, ?> getInstance() {
        return instance;
    }

    public Type getReqType() {
        return reqType;
    }

    public Type getRespType() {
        return respType;
    }
}
