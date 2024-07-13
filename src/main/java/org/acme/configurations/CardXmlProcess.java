package org.acme.configurations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * This class is responsible for processing the Card XML.
 * It implements the Processor interface from Apache Camel, which allows it to be used in a Camel route.
 */
public class CardXmlProcess implements Processor {

    private static String XSLT_FILE_PATH;

    /**
     * Constructor for the CardXmlProcess class.
     * @param xsltFilePath The path to the XSLT file.
     */
    public CardXmlProcess(String xsltFilePath) {
        XSLT_FILE_PATH = xsltFilePath;
    }

    Logger logger= LoggerFactory.getLogger(CardXmlProcess.class);

    /**
     * The process method is called when a message is sent to this processor in a Camel route.
     * @param exchange The current exchange in the route.
     * @throws Exception If an error occurs during processing.
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        // Obtener el JSON del cuerpo del intercambio
        String jsonInput = exchange.getIn().getBody(String.class);

        // Convertir JSON a XML
        String xmlOutput = convertJsonToXml(jsonInput);

        // Loggear el XML resultante
        logger.info("xmlOutput: {}", xmlOutput);

        // Establecer el XML como el cuerpo de la respuesta
        exchange.getIn().setBody(xmlOutput);
    }

    /**
     * Converts a JSON string to an XML string.
     * @param jsonInput The JSON string to convert.
     * @return The converted XML string.
     * @throws Exception If an error occurs during conversion.
     */
    private String convertJsonToXml(String jsonInput) throws Exception {
        // Parsear el JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonInput);

        // Crear un documento XML
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        // Crear el elemento raíz
        Element rootElement = document.createElement("processCardXmlRequest");
        document.appendChild(rootElement);

        // Convertir JSON a XML
        jsonNodeToXml(rootNode, document, rootElement);

        // Transformar el documento XML a una cadena XML
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult streamResult = new StreamResult(writer);
        transformer.transform(domSource, streamResult);

        // Obtener la cadena XML resultante
        String xmlOutput = writer.toString();

        // Eliminar la declaración XML
        return xmlOutput.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
    }

    /**
     * Recursively converts a JsonNode to an XML Element.
     * @param node The JsonNode to convert.
     * @param document The Document to which the Element will be added.
     * @param parentElement The parent Element for the new Element.
     */
    private void jsonNodeToXml(JsonNode node, Document document, Element parentElement) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                Element childElement = document.createElement(entry.getKey());
                parentElement.appendChild(childElement);
                jsonNodeToXml(entry.getValue(), document, childElement);
            });
        } else if (node.isArray()) {
            node.forEach(arrayElement -> jsonNodeToXml(arrayElement, document, parentElement));
        } else if (node.isValueNode()) {
            parentElement.setTextContent(node.asText());
        }
    }

    /**
     * Builds a SOAP envelope around an XML body.
     * @param xmlBody The XML body to wrap in a SOAP envelope.
     * @return The SOAP envelope as a string.
     */
    private String buildSOAPEnvelope(String xmlBody) {
        StringBuilder soapEnvelope = new StringBuilder();
        soapEnvelope.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:gen=\"http://www.bbvacard.com/servicesoapcard/gen\">");
        soapEnvelope.append("<soapenv:Header/>");
        soapEnvelope.append("<soapenv:Body>");
        soapEnvelope.append("<gen:processCardXmlRequest>");
        soapEnvelope.append(xmlBody); // Aquí incluyes el cuerpo XML generado
        soapEnvelope.append("</gen:processCardXmlRequest>");
        soapEnvelope.append("</soapenv:Body>");
        soapEnvelope.append("</soapenv:Envelope>");
        return soapEnvelope.toString();
    }

    /**
     * Transforms an XML string using an XSLT file.
     * @param xmlInput The XML string to transform.
     * @return The transformed XML string.
     * @throws Exception If an error occurs during transformation.
     */
    private String transformXml(String xmlInput) throws Exception {
        logger.info("xmlInput: {}", xmlInput);

        // Ruta del archivo XSLT dentro del directorio de recursos
        String xsltFilePath = XSLT_FILE_PATH; // Ajusta la ruta según la ubicación real de tu archivo

        // Cargar el archivo XSLT utilizando el ClassLoader
        InputStream xsltInputStream = getClass().getResourceAsStream(xsltFilePath);
        if (xsltInputStream == null) {
            throw new FileNotFoundException("El archivo XSLT no se encontró en la ruta: " + xsltFilePath);
        }

        // Crear el Source para el archivo XSLT
        Source xsltSource = new StreamSource(xsltInputStream);

        try {
            // Crear un transformador XSLT
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(xsltSource);

            // Realizar la transformación
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new StreamSource(new StringReader(xmlInput)), new StreamResult(stringWriter));

            return stringWriter.toString();
        } catch (TransformerConfigurationException e) {
            logger.error("Error de configuración durante la transformación XSLT: {}", e.getMessage());
            throw e; // Relanzar la excepción para manejarla en un nivel superior si es necesario
        } catch (TransformerException e) {
            logger.error("Error durante la transformación XSLT: {}", e.getMessage());
            throw e; // Relanzar la excepción para manejarla en un nivel superior si es necesario
        } finally {
            // Cerrar el InputStream del archivo XSLT
            if (xsltInputStream != null) {
                try {
                    xsltInputStream.close();
                } catch (IOException e) {
                    logger.warn("No se pudo cerrar el InputStream del archivo XSLT: {}", e.getMessage());
                }
            }
        }
    }

}