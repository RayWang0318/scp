package com.ray.scp.envelop;

public class SignerInfo {
    private int version;
    private IssuerAndSerialNumber issuerAndSerialNumber;
    private AlgorithmIdentifier digestAlgorithm;
    private AlgorithmIdentifier digestEncryptionAlgorithm;
    private String encryptedDigest;
    private Object authenticatedAttributes;     // 可定义具体结构或保留为 Object/null
    private Object unauthenticatedAttributes;   // 同上

    public SignerInfo() {
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public IssuerAndSerialNumber getIssuerAndSerialNumber() {
        return issuerAndSerialNumber;
    }

    public void setIssuerAndSerialNumber(IssuerAndSerialNumber issuerAndSerialNumber) {
        this.issuerAndSerialNumber = issuerAndSerialNumber;
    }

    public AlgorithmIdentifier getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(AlgorithmIdentifier digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public AlgorithmIdentifier getDigestEncryptionAlgorithm() {
        return digestEncryptionAlgorithm;
    }

    public void setDigestEncryptionAlgorithm(AlgorithmIdentifier digestEncryptionAlgorithm) {
        this.digestEncryptionAlgorithm = digestEncryptionAlgorithm;
    }

    public String getEncryptedDigest() {
        return encryptedDigest;
    }

    public void setEncryptedDigest(String encryptedDigest) {
        this.encryptedDigest = encryptedDigest;
    }

    public Object getAuthenticatedAttributes() {
        return authenticatedAttributes;
    }

    public void setAuthenticatedAttributes(Object authenticatedAttributes) {
        this.authenticatedAttributes = authenticatedAttributes;
    }

    public Object getUnauthenticatedAttributes() {
        return unauthenticatedAttributes;
    }

    public void setUnauthenticatedAttributes(Object unauthenticatedAttributes) {
        this.unauthenticatedAttributes = unauthenticatedAttributes;
    }
}
