package br.com.swconsultoria.nfe;

import br.com.swconsultoria.certificado.Certificado;
import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.certificado.exception.CertificadoException;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AssinaturaEnum;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.util.ObjetoUtil;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.content.x509.XMLX509Certificate;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe Responsavel Por Assinar O Xml.
 *
 * @author Samuel Oliveira - samuel@swconsultoria.com.br - www.swconsultoria.com.br
 */
public class Assinar {

    private static PrivateKey privateKey;
    private static KeyInfo keyInfo;
    Assinar assinarXMLsCertfificadoA1;

    /**
     * @param stringXml
     * @param tipoAssinatura ('NFe' para nfe normal , 'infInut' para inutilizacao, 'evento'
     *                       para eventos)
     * @return String do Xml Assinado
     * @throws NfeException
     */
    public static String assinaNfe(ConfiguracoesNfe config, String stringXml, AssinaturaEnum tipoAssinatura) throws NfeException {

        stringXml = stringXml.replaceAll("\r\n", "").replaceAll("\n", "").replaceAll(System.lineSeparator(), ""); // Erro quando tem salto de linha.
        stringXml = stringXml.replaceAll("\\s+<", "<"); // Erro Espaço antes do final da Tag.
        stringXml = assinaDocNFe(config, stringXml, tipoAssinatura);
        stringXml = stringXml.replaceAll("&#13;", ""); // Java 11

        return stringXml;
    }

    private static String assinaDocNFe(ConfiguracoesNfe config, String xml, AssinaturaEnum tipoAssinatura) throws NfeException {

        try {
            Document document = documentFactory(xml);
            if (!CertificadoService.isAndroid) {
                XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");
                ArrayList<Transform> transformList = signatureFactory(signatureFactory);
                loadCertificates(config, signatureFactory);

                for (int i = 0; i < document.getDocumentElement().getElementsByTagName(tipoAssinatura.getTipo()).getLength(); i++) {
                    assinarNFe(tipoAssinatura, signatureFactory, transformList, privateKey, keyInfo, document, i);
                }
            } else  {
                X509Certificate x509Certificate = loadCertificatesAndroid(config);

                for (int i = 0; i < document.getElementsByTagName(tipoAssinatura.getTipo()).getLength(); i++) {
                    assinarNFeAndroid(tipoAssinatura, privateKey, document, x509Certificate);
                }
            }

            return outputXML(document);
        } catch (SAXException | IOException | ParserConfigurationException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | KeyStoreException | UnrecoverableEntryException |
                 CertificadoException | MarshalException | XMLSignatureException | XMLSecurityException |
                 CertificateEncodingException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            throw new NfeException("Erro ao Assinar Nfe" + e.getMessage(),e);
        }
    }

    private static void assinarNFeAndroid(
        AssinaturaEnum tipoAssinatura, PrivateKey privateKey, Document document, X509Certificate x509Certificate
    ) throws XMLSecurityException, CertificateEncodingException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Element element = (Element) document.getElementsByTagName(tipoAssinatura.getTag()).item(0);
        String id = "#"+ element.getAttribute("Id");
        element.setIdAttribute("Id", true);

        org.apache.xml.security.signature.XMLSignature xmlSignature = new org.apache.xml.security.signature.XMLSignature(
            document, id, org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1,
            Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS
        );

        Transforms transforms = new Transforms(document);
        transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
        transforms.addTransform(Transforms.TRANSFORM_C14N_OMIT_COMMENTS);

        //Passar bytes nulo pra converter base64 para o android - Antes: xmlSignature.addKeyInfo(x509Certificate);
        org.apache.xml.security.keys.content.X509Data x509data =
            new org.apache.xml.security.keys.content.X509Data(xmlSignature.getDocument());

        XMLX509Certificate certBase64 = new XMLX509Certificate(x509data.getDocument(), (byte[]) null);
        byte[] bytesCert = x509Certificate.getEncoded();
        String text64 = XmlNfeUtil.encodeToStringAndroidBase64(bytesCert);
        certBase64.addText(XMLUtils.ignoreLineBreaks() ? text64 : "\n" + text64 + "\n");

        x509data.add(certBase64);

        xmlSignature.getKeyInfo().add(x509data);
        //

        xmlSignature.addDocument(id, transforms);

        document.getDocumentElement().appendChild(xmlSignature.getElement());

        xmlSignature.sign(privateKey);
    }

    private static X509Certificate loadCertificatesAndroid(ConfiguracoesNfe config)
        throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificadoException {

        Certificado certificado = config.getCertificado();
        KeyStore keyStore = CertificadoService.getKeyStore(certificado);

        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(certificado.getNome(),
            new KeyStore.PasswordProtection(ObjetoUtil.verifica(certificado.getSenha()).orElse("").toCharArray()));
        privateKey = pkEntry.getPrivateKey();

        return CertificadoService.getCertificate(certificado, keyStore);
    }

    private static void assinarNFe(AssinaturaEnum tipoAssinatura, XMLSignatureFactory fac, ArrayList<Transform> transformList,
                                   PrivateKey privateKey, KeyInfo ki, Document document, int indexNFe) throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, MarshalException, XMLSignatureException {

        NodeList elements = document.getElementsByTagName(tipoAssinatura.getTag());

        org.w3c.dom.Element el = (org.w3c.dom.Element) elements.item(indexNFe);
        String id = el.getAttribute("Id");
        el.setIdAttribute("Id", true);
        Reference ref = fac.newReference("#" + id, fac.newDigestMethod(DigestMethod.SHA1, null), transformList, null,
                null);

        SignedInfo si = fac.newSignedInfo(
                fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));

        XMLSignature signature = fac.newXMLSignature(si, ki);

        DOMSignContext dsc;

        if (tipoAssinatura.equals(AssinaturaEnum.INUTILIZACAO)) {
            dsc = new DOMSignContext(privateKey, document.getFirstChild());
        } else {
            dsc = new DOMSignContext(privateKey,
                    document.getDocumentElement().getElementsByTagName(tipoAssinatura.getTipo()).item(indexNFe));
        }

        dsc.setBaseURI("ok");

        signature.sign(dsc);
    }

    private static ArrayList<Transform> signatureFactory(XMLSignatureFactory signatureFactory)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        ArrayList<Transform> transformList = new ArrayList<Transform>();
        Transform envelopedTransform = signatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        Transform c14NTransform = signatureFactory.newTransform("http://www.w3.org/TR/2001/REC-xml-c14n-20010315", (TransformParameterSpec) null);

        transformList.add(envelopedTransform);
        transformList.add(c14NTransform);
        return transformList;
    }

    private static Document documentFactory(String xml) throws SAXException, IOException, ParserConfigurationException {

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        return docBuilder.parse(new InputSource(new StringReader(xml)));
    }

    private static void loadCertificates(ConfiguracoesNfe config, XMLSignatureFactory signatureFactory)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificadoException {

        Certificado certificado = config.getCertificado();
        KeyStore keyStore = CertificadoService.getKeyStore(certificado);

        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(certificado.getNome(),
                new KeyStore.PasswordProtection(ObjetoUtil.verifica(certificado.getSenha()).orElse("").toCharArray()));
        privateKey = pkEntry.getPrivateKey();

        KeyInfoFactory keyInfoFactory = signatureFactory.getKeyInfoFactory();
        List<X509Certificate> x509Content = new ArrayList<X509Certificate>();

        x509Content.add(CertificadoService.getCertificate(certificado, keyStore));
        X509Data x509Data = keyInfoFactory.newX509Data(x509Content);
        keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509Data));
    }

    private static String outputXML(Document doc) throws NfeException {

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()){
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.transform(new DOMSource(doc), new StreamResult(os));
            String xml = os.toString();
            xml = xml.replaceAll("\\r\\n", "");
            xml = xml.replaceAll(" standalone=\"no\"", "");
            return xml;
        } catch (TransformerException | IOException e) {
            throw new NfeException("Erro ao Transformar Documento:" + e.getMessage(),e);
        }
    }
}