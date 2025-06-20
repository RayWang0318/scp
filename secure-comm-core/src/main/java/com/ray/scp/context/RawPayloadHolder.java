package com.ray.scp.context;

public class RawPayloadHolder {

    private final byte[] rawPayload;

    public RawPayloadHolder(byte[] rawPayload) {
        this.rawPayload = rawPayload;
    }

    public byte[] getRawPayload() {
        return rawPayload;
    }
}
