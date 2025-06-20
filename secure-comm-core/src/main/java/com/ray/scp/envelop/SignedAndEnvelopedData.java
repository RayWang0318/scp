package com.ray.scp.envelop;

import java.util.List;

public class SignedAndEnvelopedData {
    private int version;
    private List<RecipientInfo> recipientInfos;
    private List<AlgorithmIdentifier> digestAlgorithmIdentifiers;
    private EncryptedContentInfo encryptedContentInfo;
    private List<String> certificate;
    private List<SignerInfo> signerInfos;

    public SignedAndEnvelopedData() {
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<RecipientInfo> getRecipientInfos() {
        return recipientInfos;
    }

    public void setRecipientInfos(List<RecipientInfo> recipientInfos) {
        this.recipientInfos = recipientInfos;
    }

    public List<AlgorithmIdentifier> getDigestAlgorithmIdentifiers() {
        return digestAlgorithmIdentifiers;
    }

    public void setDigestAlgorithmIdentifiers(List<AlgorithmIdentifier> digestAlgorithmIdentifiers) {
        this.digestAlgorithmIdentifiers = digestAlgorithmIdentifiers;
    }

    public EncryptedContentInfo getEncryptedContentInfo() {
        return encryptedContentInfo;
    }

    public void setEncryptedContentInfo(EncryptedContentInfo encryptedContentInfo) {
        this.encryptedContentInfo = encryptedContentInfo;
    }

    public List<String> getCertificate() {
        return certificate;
    }

    public void setCertificate(List<String> certificate) {
        this.certificate = certificate;
    }

    public List<SignerInfo> getSignerInfos() {
        return signerInfos;
    }

    public void setSignerInfos(List<SignerInfo> signerInfos) {
        this.signerInfos = signerInfos;
    }
}
