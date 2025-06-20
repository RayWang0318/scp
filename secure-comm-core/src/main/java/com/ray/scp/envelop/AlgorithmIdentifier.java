package com.ray.scp.envelop;

public class AlgorithmIdentifier {

    private String algorithm;
    // 可为 null 或 BASE64 编码的 IV、OID 等
    private String parameters;

    public AlgorithmIdentifier() {
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
