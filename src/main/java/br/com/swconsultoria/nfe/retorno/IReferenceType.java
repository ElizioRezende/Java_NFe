package br.com.swconsultoria.nfe.retorno;

public interface IReferenceType {

    byte[] getDigestValue();

    void setDigestValue(byte[] value);
}