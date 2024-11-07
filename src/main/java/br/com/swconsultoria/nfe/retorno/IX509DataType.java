package br.com.swconsultoria.nfe.retorno;

public interface IX509DataType {

    byte[] getX509Certificate();

    void setX509Certificate(byte[] value);
}