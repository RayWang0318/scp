package com.ray.scp.envelop;

public class EncryptedContentInfo {
    private String contentType;
    private AlgorithmIdentifier contentEncryptionAlgorithm;
    private String encryptedContent;

    public EncryptedContentInfo() {
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public AlgorithmIdentifier getContentEncryptionAlgorithm() {
        return contentEncryptionAlgorithm;
    }

    public void setContentEncryptionAlgorithm(AlgorithmIdentifier contentEncryptionAlgorithm) {
        this.contentEncryptionAlgorithm = contentEncryptionAlgorithm;
    }

    public String getEncryptedContent() {
        return encryptedContent;
    }

    public void setEncryptedContent(String encryptedContent) {
        this.encryptedContent = encryptedContent;
    }
}
