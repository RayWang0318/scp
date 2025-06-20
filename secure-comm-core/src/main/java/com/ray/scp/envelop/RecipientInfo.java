package com.ray.scp.envelop;

public class RecipientInfo {
    private int version;
    private IssuerAndSerialNumber issuerAndSerialNumber;
    private AlgorithmIdentifier keyEncryptionAlgorithm;
    private String encryptedKey;

    public RecipientInfo() {
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

    public AlgorithmIdentifier getKeyEncryptionAlgorithm() {
        return keyEncryptionAlgorithm;
    }

    public void setKeyEncryptionAlgorithm(AlgorithmIdentifier keyEncryptionAlgorithm) {
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }
}