package br.com.swconsultoria.nfe.retorno;

public interface ISignatureType {

    ISignedInfoType getSignedInfo();

    ISignatureValueType getSignatureValue();

    void setNewSignatureValue();

    IKeyInfoType getKeyInfo();
}