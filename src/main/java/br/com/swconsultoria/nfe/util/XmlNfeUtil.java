/**
 *
 */
package br.com.swconsultoria.nfe.util;

import br.com.swconsultoria.certificado.CertificadoService;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.retorno.ISignatureType;
import br.com.swconsultoria.nfe.retorno.ITProtNFe;
import br.com.swconsultoria.nfe.schema.consCad.TConsCad;
import br.com.swconsultoria.nfe.schema.distdfeint.DistDFeInt;
import br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEnvEvento;
import br.com.swconsultoria.nfe.schema.envEventoCancNFe.TRetEnvEvento;
import br.com.swconsultoria.nfe.schema_4.consReciNFe.TConsReciNFe;
import br.com.swconsultoria.nfe.schema_4.consSitNFe.TConsSitNFe;
import br.com.swconsultoria.nfe.schema_4.consStatServ.TConsStatServ;
import br.com.swconsultoria.nfe.schema_4.enviNFe.*;
import br.com.swconsultoria.nfe.schema_4.inutNFe.TInutNFe;
import br.com.swconsultoria.nfe.schema_4.inutNFe.TProcInutNFe;
import br.com.swconsultoria.nfe.schema_4.inutNFe.TRetInutNFe;
import br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TRetConsSitNFe;
import lombok.extern.java.Log;

import javax.xml.bind.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.zip.GZIPInputStream;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

/**
 * Classe Responsavel por Metodos referentes ao XML
 *
 * @author Samuel Oliveira
 */
@Log
public class XmlNfeUtil {

    private XmlNfeUtil(){}

    private static final String STATUS = "TConsStatServ";
    private static final String SITUACAO_NFE = "TConsSitNFe";
    private static final String ENVIO_NFE = "TEnviNFe";
    private static final String DIST_DFE = "DistDFeInt";
    private static final String INUTILIZACAO = "TInutNFe";
    private static final String NFEPROC = "TNfeProc";
    private static final String NFE = "TNFe";
    private static final String EVENTO = "TEnvEvento";
    private static final String TPROCEVENTO = "TProcEvento";
    private static final String TCONSRECINFE = "TConsReciNFe";
    private static final String TCONS_CAD = "TConsCad";
    private static final String TPROCINUT = "TProcInutNFe";
    private static final String RETORNO_ENVIO = "TRetEnviNFe";
    private static final String SITUACAO_NFE_RET = "TRetConsSitNFe";
    private static final String RET_RECIBO_NFE = "TRetConsReciNFe";
    private static final String RET_STATUS_SERVICO = "TRetConsStatServ";
    private static final String RET_CONS_CAD = "TRetConsCad";
    private static final String RET_DIST_DFE = "RetDistDFeInt";
    private static final String RET_ENV_EVENTO = "TRetEnvEvento";
    private static final String RET_INUT_NFE = "TRetInutNFe";
    private static final String TPROCCANCELAR = "br.com.swconsultoria.nfe.schema.envEventoCancNFe.TProcEvento";
    private static final String TPROCATORINTERESSADO = "br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TProcEvento";
    private static final String TPROCCANCELARSUBST = "br.com.swconsultoria.nfe.schema.envEventoCancSubst.TProcEvento";
    private static final String TPROCCCE = "br.com.swconsultoria.nfe.schema.envcce.TProcEvento";
    private static final String TPROCEPEC = "br.com.swconsultoria.nfe.schema.envEpec.TProcEvento";
    private static final String TPROCMAN = "br.com.swconsultoria.nfe.schema.envConfRecebto.TProcEvento";
    private static final String TProtNFe = "TProtNFe";
    private static final String TProtEnvi = "br.com.swconsultoria.nfe.schema_4.enviNFe.TProtNFe";
    private static final String TProtCons = "br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TProtNFe";
    private static final String TProtReci = "br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TProtNFe";
    private static final String CANCELAR = "br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEnvEvento";
    private static final String ATOR_INTERESSADO = "br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TEnvEvento";
    private static final String CANCELAR_SUBSTITUICAO = "br.com.swconsultoria.nfe.schema.envEventoCancSubst.TEnvEvento";
    private static final String CCE = "br.com.swconsultoria.nfe.schema.envcce.TEnvEvento";
    private static final String EPEC = "br.com.swconsultoria.nfe.schema.envEpec.TEnvEvento";
    private static final String MANIFESTAR = "br.com.swconsultoria.nfe.schema.envConfRecebto.TEnvEvento";
    private static final String RET_CANCELAR = "br.com.swconsultoria.nfe.schema.envEventoCancNFe.TRetEnvEvento";
    private static final String RET_ATOR_INTERESSADO = "br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TRetEnvEvento";
    private static final String RET_CANCELAR_SUBSTITUICAO = "br.com.swconsultoria.nfe.schema.envEventoCancSubst.TRetEnvEvento";
    private static final String RET_CCE = "br.com.swconsultoria.nfe.schema.envcce.TRetEnvEvento";
    private static final String RET_EPEC = "br.com.swconsultoria.nfe.schema.envEpec.TRetEnvEvento";
    private static final String RET_MANIFESTAR = "br.com.swconsultoria.nfe.schema.envConfRecebto.TRetEnvEvento";

    /**
     * Transforma o String do XML em Objeto
     *
     * @param xml
     * @param classe
     * @return T
     */
    public static <T> T xmlToObject(String xml, Class<T> classe) throws InstantiationException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (CertificadoService.isAndroid) {
            return xmlToObjectAndroid(xml, classe);
        } else {
            return JAXB.unmarshal(new StreamSource(new StringReader(xml)), classe);
        }
    }

    private static <T> T xmlToObjectAndroid(String xml, Class<T> classe) throws InstantiationException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        if (classe.equals(TNFe.class)) {
            return (T) tEnviNFeXmlToObject(xml);
        } else if (classe.equals(TRetEnviNFe.class)) {
            return (T) tRetEnviNFeXmlToObject(xml);
        } else if (classe.equals(br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TRetConsReciNFe.class)) {
            return (T) tRetConsReciNFeXmlToObject(xml);
        } else if (classe.equals(TRetConsSitNFe.class)) {
            return (T) tRetConsSitNFeXmlToObject(xml);
        } else if (classe.equals(TRetEnvEvento.class)) {
            return (T) tRetEnvEventoXmlToObject(xml);
        }

        throw new ClassNotFoundException(classe.getSimpleName());
    }

    private static Gson gsonWithExclusionStrategy() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        ExclusionStrategy strategy = new ExclusionStrategy() {

            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {

                String fieldName = fieldAttributes.getName();

                if (fieldName.equalsIgnoreCase("SignatureValue")) {
                    return true;
                } else if (fieldName.equals("X509Certificate")) {
                    return true;
                } else if (fieldName.equals("DigestValue")) {
                    return true;
                } else if (fieldName.equalsIgnoreCase("digVal")) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return false;
            }
        };
        gsonBuilder.setExclusionStrategies(strategy);

        return gsonBuilder.create();
    }

    private static JSONObject processarXml(String xml, String... forceList) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {

        Class<?> builderClazz = Class.forName("fr.arnaudguyon.xmltojsonlib.XmlToJson$Builder");

        Object obj = builderClazz.getConstructor(String.class).newInstance(xml);

        if (forceList != null && forceList.length > 0) {

            Method forceListMethod = builderClazz.getMethod("forceList", String.class);

            for (String param: forceList) {
                forceListMethod.invoke(obj, param);
            }
        }

        Method buildMethod = builderClazz.getMethod("build");

        Object xmlToJson = buildMethod.invoke(obj);

        Class<?> xmlToJsonClazz = Class.forName("fr.arnaudguyon.xmltojsonlib.XmlToJson");

        Method toJsonMethod = xmlToJsonClazz.getMethod("toJson");

        return (JSONObject) toJsonMethod.invoke(xmlToJson);
    }

    private static TNFe tEnviNFeXmlToObject(String xml) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        JSONObject jsonObject = processarXml(
            xml, "/NFe", "/NFe/infNFe/det", "/NFe/infNFe/autXML", "/NFe/infNFe/pag/detPag"
        ).getJSONArray("NFe").getJSONObject(0);

        String json = jsonObject.toString();

        Gson gson = gsonWithExclusionStrategy();
        TNFe nfe = gson.fromJson(json, TNFe.class);

        setSignatureValues(nfe.getSignature(), jsonObject);

        return nfe;
    }

    private static TRetEnviNFe tRetEnviNFeXmlToObject(String xml) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        JSONObject jsonObject = processarXml(xml).getJSONObject("retEnviNFe");

        String json = jsonObject.toString();

        Gson gson = gsonWithExclusionStrategy();
        TRetEnviNFe nfe = gson.fromJson(json, TRetEnviNFe.class);

        ITProtNFe tProtNFe = nfe.getOriginalProtNFe();

        setRetornoValues(tProtNFe, jsonObject);

        return nfe;
    }

    private static br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TRetConsReciNFe tRetConsReciNFeXmlToObject(String xml) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        JSONObject jsonObject = processarXml(xml).getJSONObject("retConsReciNFe");

        String json = jsonObject.toString();

        Gson gson = gsonWithExclusionStrategy();
        br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TRetConsReciNFe nfe = gson.fromJson(
            json, br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TRetConsReciNFe.class
        );

        ITProtNFe tProtNFe = nfe.getOriginalProtNFe();

        setRetornoValues(tProtNFe, jsonObject);

        return nfe;
    }

    private static TRetConsSitNFe tRetConsSitNFeXmlToObject(String xml) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        JSONObject jsonObject = processarXml(xml).getJSONObject("retConsSitNFe");

        String json = jsonObject.toString();

        Gson gson = gsonWithExclusionStrategy();
        TRetConsSitNFe nfe = gson.fromJson(json, TRetConsSitNFe.class);

        ITProtNFe tProtNFe = nfe.getOriginalProtNFe();

        setRetornoValues(tProtNFe, jsonObject);

        return nfe;
    }

    private static TRetEnvEvento tRetEnvEventoXmlToObject(String xml) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        JSONObject jsonObject = processarXml(xml).getJSONObject("retEnvEvento");

        String json = jsonObject.toString();

        Gson gson = gsonWithExclusionStrategy();
        TRetEnvEvento nfe = gson.fromJson(json, TRetEnvEvento.class);

        ITProtNFe tProtNFe = nfe.getOriginalProtNFe();

        setRetornoValues(tProtNFe, jsonObject);

        return nfe;
    }

    private static void setRetornoValues(ITProtNFe tProtNFe, JSONObject jsonObject) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (tProtNFe != null && jsonObject.has("protNFe")) {

            JSONObject protNFeJson = jsonObject.getJSONObject("protNFe");

            if (protNFeJson.has("infProt") && protNFeJson.getJSONObject("infProt").has("digVal")) {
                String digVal = protNFeJson.getJSONObject("infProt").getString("digVal");
                tProtNFe.getInfProt().setDigVal(decodeAndroidBase64(digVal));
            }

            setSignatureValues(tProtNFe.getSignature(), protNFeJson);
        }
    }

    private static void setSignatureValues(ISignatureType signatureType, JSONObject jsonObject) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (signatureType != null && jsonObject.has("Signature")) {

            JSONObject signature = jsonObject.getJSONObject("Signature");

            String signatureValue = signature.getString("SignatureValue");
            String digestValue = signature.getJSONObject("SignedInfo").getJSONObject("Reference").getString("DigestValue");
            String x509Certificate = signature.getJSONObject("KeyInfo").getJSONObject("X509Data").getString("X509Certificate");

            signatureType.setNewSignatureValue();

            signatureType.getSignatureValue().setValue(decodeAndroidBase64(signatureValue));
            signatureType.getSignedInfo().getReference().setDigestValue(decodeAndroidBase64(digestValue));
            signatureType.getKeyInfo().getX509Data().setX509Certificate(decodeAndroidBase64(x509Certificate));
        }
    }

    private static byte[] decodeAndroidBase64(String value) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<?> clazz = Class.forName("android.util.Base64");

        Method method = clazz.getMethod("decode", String.class, int.class);

        return (byte[]) method.invoke(null, value, 0);
    }

    /**
     * Transforma o Objeto em XML(String)
     *
     * @param obj
     * @return
     * @throws JAXBException
     * @throws NfeException
     */
    public static <T> String objectToXml(Object obj) throws JAXBException, NfeException, InstantiationException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return objectToXml(obj, Charset.forName("UTF-8"));
    }

    public static <T> String objectToXml(Object obj, Charset encode) throws JAXBException, NfeException, InstantiationException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        if (CertificadoService.isAndroid) {

            List<String> forcedAttributes = new ArrayList<>(), forcedContents = new ArrayList<>();

            JSONObject jsonObject = new JSONObject();

            switch (obj.getClass().getSimpleName()) {

                case SITUACAO_NFE:
                    TConsSitNFe modelo = (TConsSitNFe) obj;

                    jsonObject.put("tpAmb", modelo.getTpAmb());
                    jsonObject.put("xServ", modelo.getXServ());
                    jsonObject.put("chNFe", modelo.getChNFe());
                    jsonObject.put("xmlns", "http://www.portalfiscal.inf.br/nfe");

                    jsonObject = new JSONObject().put("consSitNFe", jsonObject);
                    forcedAttributes.add("/consSitNFe/versao");
                    forcedAttributes.add("/consSitNFe/xmlns");
                    break;
            }

            xml.append(objectToXmlAndroid(jsonObject, forcedAttributes, forcedContents));

            if ((obj.getClass().getSimpleName().equals(TPROCEVENTO))) {
                return replacesNfe(xml.toString().replaceAll("procEvento", "procEventoNFe"));
            } else {
                return replacesNfe(xml.toString());
            }
        } else {
            JAXBContext context;
            JAXBElement<?> element;

            switch (obj.getClass().getSimpleName()) {

                case STATUS:
                    context = JAXBContext.newInstance(TConsStatServ.class);
                    element = new br.com.swconsultoria.nfe.schema_4.consStatServ.ObjectFactory().createConsStatServ((TConsStatServ) obj);
                    break;

                case ENVIO_NFE:
                    context = JAXBContext.newInstance(TEnviNFe.class);
                    element = new br.com.swconsultoria.nfe.schema_4.enviNFe.ObjectFactory().createEnviNFe((TEnviNFe) obj);
                    break;

                case RETORNO_ENVIO:
                    context = JAXBContext.newInstance(TRetEnviNFe.class);
                    element = XsdUtil.enviNfe.createTRetEnviNFe((TRetEnviNFe) obj);
                    break;

                case SITUACAO_NFE:
                    context = JAXBContext.newInstance(TConsSitNFe.class);
                    element = new br.com.swconsultoria.nfe.schema_4.consSitNFe.ObjectFactory().createConsSitNFe((TConsSitNFe) obj);
                    break;

                case DIST_DFE:
                    context = JAXBContext.newInstance(DistDFeInt.class);
                    element = XsdUtil.distDFeInt.createDistDFeInt((DistDFeInt) obj);
                    break;

                case TCONSRECINFE:
                    context = JAXBContext.newInstance(TConsReciNFe.class);
                    element = new br.com.swconsultoria.nfe.schema_4.consReciNFe.ObjectFactory().createConsReciNFe((TConsReciNFe) obj);
                    break;

                case TCONS_CAD:
                    context = JAXBContext.newInstance(TConsCad.class);
                    element = new br.com.swconsultoria.nfe.schema.consCad.ObjectFactory().createConsCad((TConsCad) obj);
                    break;

                case INUTILIZACAO:
                    context = JAXBContext.newInstance(TInutNFe.class);
                    element = new br.com.swconsultoria.nfe.schema_4.inutNFe.ObjectFactory().createInutNFe((TInutNFe) obj);
                    break;

                case RET_INUT_NFE:
                    context = JAXBContext.newInstance(TRetInutNFe.class);
                    element = XsdUtil.inutNfe.createTRetInutNfe((TRetInutNFe) obj);
                    break;

                case SITUACAO_NFE_RET:
                    context = JAXBContext.newInstance(TRetConsSitNFe.class);
                    element = new br.com.swconsultoria.nfe.schema_4.retConsSitNFe.ObjectFactory().createRetConsSitNFe((TRetConsSitNFe) obj);
                    break;

                case RET_RECIBO_NFE:
                    context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TRetConsReciNFe.class);
                    element = new br.com.swconsultoria.nfe.schema_4.retConsReciNFe.ObjectFactory().createRetConsReciNFe((br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TRetConsReciNFe) obj);
                    break;

                case RET_STATUS_SERVICO:
                    context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_4.retConsStatServ.TRetConsStatServ.class);
                    element = new br.com.swconsultoria.nfe.schema_4.retConsStatServ.ObjectFactory().createRetConsStatServ((br.com.swconsultoria.nfe.schema_4.retConsStatServ.TRetConsStatServ) obj);
                    break;

                case RET_CONS_CAD:
                    context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.retConsCad.TRetConsCad.class);
                    element = new br.com.swconsultoria.nfe.schema.retConsCad.ObjectFactory().createRetConsCad((br.com.swconsultoria.nfe.schema.retConsCad.TRetConsCad) obj);
                    break;

                case RET_DIST_DFE:
                    context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.retdistdfeint.RetDistDFeInt.class);
                    element = XsdUtil.distDFeInt.createRetDistDFeInt((br.com.swconsultoria.nfe.schema.retdistdfeint.RetDistDFeInt) obj);
                    break;

                case TPROCEVENTO:
                    switch (obj.getClass().getName()) {
                        case TPROCCANCELAR:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancNFe.TProcEvento.class);
                            element = XsdUtil.envEventoCancNFe.createTProcEvento((br.com.swconsultoria.nfe.schema.envEventoCancNFe.TProcEvento) obj);
                            break;
                        case TPROCATORINTERESSADO:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TProcEvento.class);
                            element = XsdUtil.envEventoAtorInteressado.createTProcEvento((br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TProcEvento) obj);
                            break;
                        case TPROCCANCELARSUBST:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancSubst.TProcEvento.class);
                            element = XsdUtil.envEventoCancSubst.createTProcEvento((br.com.swconsultoria.nfe.schema.envEventoCancSubst.TProcEvento) obj);
                            break;
                        case TPROCCCE:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envcce.TProcEvento.class);
                            element =  XsdUtil.envcce.createTProcEvento((br.com.swconsultoria.nfe.schema.envcce.TProcEvento) obj);
                            break;
                        case TPROCEPEC:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEpec.TProcEvento.class);
                            element = XsdUtil.epec.createTProcEvento((br.com.swconsultoria.nfe.schema.envEpec.TProcEvento) obj);
                            break;
                        case TPROCMAN:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envConfRecebto.TProcEvento.class);
                            element = XsdUtil.manifestacao.createTProcEvento((br.com.swconsultoria.nfe.schema.envConfRecebto.TProcEvento) obj);
                            break;
                        default:
                            throw new NfeException("Objeto não mapeado no XmlUtil:" + obj.getClass().getSimpleName());
                    }

                    break;

                case NFEPROC:
                    context = JAXBContext.newInstance(TNfeProc.class);
                    element = XsdUtil.enviNfe.createTNfeProc((TNfeProc) obj);
                    break;

                case NFE:
                    context = JAXBContext.newInstance(TNFe.class);
                    element = new JAXBElement<>(new QName("http://www.portalfiscal.inf.br/nfe", "NFe"), TNFe.class, null, (br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe) obj);
                    break;

                case TPROCINUT:
                    context = JAXBContext.newInstance(TProcInutNFe.class);
                    element = XsdUtil.inutNfe.createTProcInutNFe((TProcInutNFe) obj);
                    break;

                case EVENTO:
                    switch (obj.getClass().getName()) {
                        case CANCELAR:
                            context = JAXBContext.newInstance(TEnvEvento.class);
                            element = new br.com.swconsultoria.nfe.schema.envEventoCancNFe.ObjectFactory().createEnvEvento((TEnvEvento) obj);
                            break;
                        case CANCELAR_SUBSTITUICAO:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancSubst.TEnvEvento.class);
                            element = new br.com.swconsultoria.nfe.schema.envEventoCancSubst.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envEventoCancSubst.TEnvEvento) obj);
                            break;
                        case CCE:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envcce.TEnvEvento.class);
                            element = new br.com.swconsultoria.nfe.schema.envcce.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envcce.TEnvEvento) obj);
                            break;
                        case EPEC:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEpec.TEnvEvento.class);
                            element = new br.com.swconsultoria.nfe.schema.envEpec.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envEpec.TEnvEvento) obj);
                            break;
                        case MANIFESTAR:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envConfRecebto.TEnvEvento.class);
                            element = new br.com.swconsultoria.nfe.schema.envConfRecebto.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envConfRecebto.TEnvEvento) obj);
                            break;
                        case ATOR_INTERESSADO:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TEnvEvento.class);
                            element = new br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TEnvEvento) obj);
                            break;
                        default:
                            throw new NfeException("Objeto não mapeado no XmlUtil:" + obj.getClass().getSimpleName());
                    }
                    break;

                case RET_ENV_EVENTO:
                    switch (obj.getClass().getName()) {
                        case RET_CANCELAR:
                            context = JAXBContext.newInstance(TRetEnvEvento.class);
                            element = XsdUtil.retEnvEvento.createTRetEnvEvento((TRetEnvEvento) obj);
                            break;
                        case RET_CANCELAR_SUBSTITUICAO:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancSubst.TRetEnvEvento.class);
                            element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envEventoCancSubst.TRetEnvEvento) obj);
                            break;
                        case RET_CCE:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envcce.TRetEnvEvento.class);
                            element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envcce.TRetEnvEvento) obj);
                            break;
                        case RET_EPEC:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEpec.TRetEnvEvento.class);
                            element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envEpec.TRetEnvEvento) obj);
                            break;
                        case RET_MANIFESTAR:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envConfRecebto.TRetEnvEvento.class);
                            element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envConfRecebto.TRetEnvEvento) obj);
                            break;
                        case RET_ATOR_INTERESSADO:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TRetEnvEvento.class);
                            element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TRetEnvEvento) obj);
                            break;
                        default:
                            throw new NfeException("Objeto não mapeado no XmlUtil:" + obj.getClass().getSimpleName());
                    }
                    break;

                case TProtNFe:
                    switch (obj.getClass().getName()) {
                        case TProtEnvi:
                            context = JAXBContext.newInstance(TProtNFe.class);
                            element = XsdUtil.enviNfe.createTProtNFe((br.com.swconsultoria.nfe.schema_4.enviNFe.TProtNFe) obj);
                            break;
                        case TProtCons:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TProtNFe.class);
                            element = XsdUtil.retConsSitNfe.createTProtNFe((br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TProtNFe) obj);
                            break;
                        case TProtReci:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TProtNFe.class);
                            element = XsdUtil.retConsReciNfe.createTProtNFe((br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TProtNFe) obj);
                            break;
                        default:
                            throw new NfeException("Objeto não mapeado no XmlUtil:" + obj.getClass().getSimpleName());
                    }
                    break;

                default:
                    throw new NfeException("Objeto não mapeado no XmlUtil:" + obj.getClass().getSimpleName());
            }
            assert context != null;
            Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty("jaxb.encoding", "Unicode");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            StringWriter sw = new StringWriter(4096);

            String encodeXml = encode == null || !Charset.isSupported(encode.displayName()) ? "UTF-8" : encode.displayName();

            sw.append("<?xml version=\"1.0\" encoding=\"" + encodeXml + "\"?>");

            marshaller.marshal(element, sw);

            if ((obj.getClass().getSimpleName().equals(TPROCEVENTO))) {
                return replacesNfe(sw.toString().replace("procEvento", "procEventoNFe"));
            }

            return replacesNfe(sw.toString());
        }

    }

    private static String objectToXmlAndroid(JSONObject obj, List<String> forcedAttributes, List<String> forcedContents) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {

        Class<?> builderClazz = Class.forName("fr.arnaudguyon.xmltojsonlib.JsonToXml$Builder");

        Object newInstance = builderClazz.getConstructor(JSONObject.class).newInstance(obj);

        if (forcedAttributes != null && !forcedAttributes.isEmpty()) {
            Method forcedAttributeMethod = builderClazz.getMethod("forceAttribute", String.class);
            for (String param: forcedAttributes) {
                forcedAttributeMethod.invoke(newInstance, param);
            }
        }

        if (forcedContents != null && !forcedContents.isEmpty()) {
            Method forcedContentMethod = builderClazz.getMethod("forceContent", String.class);
            for (String param: forcedContents) {
                forcedContentMethod.invoke(newInstance, param);
            }
        }

        Method buildMethod = builderClazz.getMethod("build");

        Object jsonToXml = buildMethod.invoke(newInstance);

        Class<?> xmlToJsonClazz = Class.forName("fr.arnaudguyon.xmltojsonlib.JsonToXml");

        Method toJsonMethod = xmlToJsonClazz.getMethod("toString");

        String xml = ((String) toJsonMethod.invoke(jsonToXml)).trim();

        return xml.substring(xml.indexOf('>') + 1);
    }

    public static String gZipToXml(byte[] conteudo) throws IOException {
        if (conteudo == null || conteudo.length == 0) {
            return "";
        }
        GZIPInputStream gis;
        gis = new GZIPInputStream(new ByteArrayInputStream(conteudo));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));
        StringBuilder outStr = new StringBuilder();
        String line;
        while ((line = bf.readLine()) != null) {
            outStr.append(line);
        }

        return outStr.toString();
    }

    public static String criaNfeProc(TEnviNFe enviNfe, Object retorno) throws JAXBException, NfeException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {

        TNfeProc nfeProc = new TNfeProc();
        nfeProc.setVersao("4.00");
        nfeProc.setNFe(enviNfe.getNFe().get(0));
        String xml = XmlNfeUtil.objectToXml(retorno);
        nfeProc.setProtNFe(XmlNfeUtil.xmlToObject(xml, TProtNFe.class));

        return XmlNfeUtil.objectToXml(nfeProc);
    }

    private static String replacesNfe(String xml) {

        return xml.replace("<!\\[CDATA\\[<!\\[CDATA\\[", "<!\\[CDATA\\[")
                .replace("\\]\\]>\\]\\]>", "\\]\\]>")
                .replace("ns2:", "")
                .replace("ns3:", "")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("<Signature>", "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">")
                .replace(" xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\"", "")
                .replace(" xmlns=\"\" xmlns:ns3=\"http://www.portalfiscal.inf.br/nfe\"", "")
                .replace("<NFe>", "<NFe xmlns=\"http://www.portalfiscal.inf.br/nfe\">");

    }

    /**
     * Le o Arquivo XML e retona String
     *
     * @return String
     * @throws NfeException
     */
    public static String leXml(String arquivo) throws IOException {

        ObjetoUtil.verifica(arquivo).orElseThrow(() -> new IllegalArgumentException("Arquivo xml não pode ser nulo/vazio."));
        if (!Files.exists(Paths.get(arquivo))) {
            throw new FileNotFoundException("Arquivo " + arquivo + " não encontrado.");
        }
        List<String> list = Files.readAllLines(Paths.get(arquivo));
        StringJoiner joiner = new StringJoiner("\n");
        list.forEach(joiner::add);

        return joiner.toString();
    }

    public static String dataNfe(LocalDateTime dataASerFormatada) {
        return dataNfe(dataASerFormatada, ZoneId.systemDefault());
    }

    public static String dataNfe(LocalDateTime dataASerFormatada, ZoneId zoneId) {
        try {
            GregorianCalendar calendar = GregorianCalendar.from(dataASerFormatada.atZone(ObjetoUtil.verifica(zoneId).orElse(ZoneId.of("Brazil/East"))));

            XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
            xmlCalendar.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
            return xmlCalendar.toString();

        } catch (DatatypeConfigurationException e) {
            log.warning(e.getMessage());
        }
        return null;
    }

    public static byte[] geraHashCSRT(String chave, String csrt) throws NoSuchAlgorithmException {

        ObjetoUtil.verifica(chave).orElseThrow(() -> new InvalidParameterException("Chave não deve ser nula ou vazia"));
        ObjetoUtil.verifica(csrt).orElseThrow(() -> new InvalidParameterException("CSRT não deve ser nulo ou vazio"));
        if (chave.length() != 44) {
            throw new InvalidParameterException("Chave deve conter 44 caracteres.");
        }
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update((csrt + chave).getBytes());
        return Base64.getEncoder().encode(md.digest());
    }
}
