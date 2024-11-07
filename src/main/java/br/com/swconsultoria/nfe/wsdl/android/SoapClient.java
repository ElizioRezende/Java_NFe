package br.com.swconsultoria.nfe.wsdl.android;

import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.exception.NfeException;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class SoapClient {

    private static OkHttpClient _okHttpClient;

    private ConfiguracoesNfe config;

    //TODO Trocar certificado
    public SoapClient(ConfiguracoesNfe config) throws NfeException {
        this.config = config;

        startClient();
    }

    public String nfeAutorizacaoLote(String url, String xml) throws NfeException {
        return post(url, xml, "NFeAutorizacao4", "enviNFe", "retEnviNFe");
    }

    public String nfeRetAutorizacaoLote(String url, String xml) throws NfeException {
        return post(url, xml, "NFeRetAutorizacao4", "retEnviNFe", "retConsReciNFe");
    }

    public String nfeConsultaNF(String url, String xml) throws NfeException {
        return post(url, xml, "NFeConsultaProtocolo4", "consSitNFe", "retConsSitNFe");
    }

    public String nfeRecepcaoEvento(String url, String xml) throws NfeException {
        return post(url, xml, "NFeRecepcaoEvento4", "envEvento", "retEnvEvento");
    }

    private String post(String url, String xmlBody, String pathUrl, String tagStart, String tagEnd) throws NfeException {

        xmlBody = xmlBody.substring(xmlBody.indexOf("<" + tagStart));

        String xml = "<?xml version='1.0' encoding='utf-8'?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">" +
            "<soapenv:Header/>" +
            "<soapenv:Body>" +
            "<ns1:nfeDadosMsg xmlns:ns1=\"http://www.portalfiscal.inf.br/nfe/wsdl/" + pathUrl + "\">" +
            xmlBody +
            "</ns1:nfeDadosMsg>" +
            "</soapenv:Body>" +
            "</soapenv:Envelope>";

        MediaType mediaType = MediaType.parse("application/soap+xml");

        RequestBody body = RequestBody.create(mediaType, xml);

        Request request = new Request.Builder()
            .url(url)
            .method("POST", body)
            .addHeader("SOAPAction", "#POST")
            .addHeader("Content-Type", "application/soap+xml")
            .build();

        String result = newCall(request);
        if (result != null) {

            String start = "<" + tagEnd;
            String end = "</" + tagEnd + ">";

            result = "<?xml version='1.0' encoding='UTF-8'?>" +
                result.substring(result.indexOf(start), result.indexOf(end) + end.length());
        }

        return result;
    }

    private String newCall(Request request) throws NfeException {
        Response response;
        try {
            response = _okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            throw new NfeException("Erro ao acessar servidor Sefaz", e);
        }

        return getHttpResponse(response);
    }

    private String getHttpResponse(Response response) {

        ResponseBody body = response.body();

        if (response.isSuccessful() && body != null) {
            InputStreamReader inputReader = new InputStreamReader(body.byteStream());
            BufferedReader bufferedReader = new BufferedReader(inputReader);

            StringBuilder result = new StringBuilder();
            bufferedReader.lines().forEach(result::append);

            try { inputReader.close(); } catch (Exception ignored) { }

            try { bufferedReader.close(); } catch (Exception ignored) { }

            return result.toString();
        } else {
            return null;
        }
    }

    private void startClient() throws NfeException {
        if (_okHttpClient == null) {
            try {
                Certificado certificado = config.getCertificado();

                KeyStore keyStore = CertificadoService.getKeyStore(certificado);

                KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                keyFactory.init(keyStore, certificado.getSenha().toCharArray());

                X509Certificate certificate = CertificadoService.getCertificate(certificado, keyStore);

                X509TrustManager trustManager = new X509TrustManager() {

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[] { certificate };
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                };

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyFactory.getKeyManagers(), new TrustManager[]{trustManager}, null);
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                OkHttpClient.Builder builder = new OkHttpClient().newBuilder().sslSocketFactory(sslSocketFactory, trustManager);

                Integer timeout = config.getTimeout();
                if (timeout != null && timeout > 0) {
                    builder.connectTimeout(timeout, TimeUnit.MILLISECONDS).readTimeout(timeout, TimeUnit.MILLISECONDS);
                }

                _okHttpClient = builder.build();

            } catch (CertificadoException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
                throw new NfeException("Erro ao criar Client", e);
            }
        }
    }
}
