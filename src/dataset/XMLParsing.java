package dataset;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.management.modelmbean.XMLParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for parsing XML files
 */
public class XMLParsing {

    /**
     * Parses an XML (specified by the full {@code path} and
     * returns an {@code ArrayList} of {@code Map<String, String>}.
     * <p>
     * Key - an XML attribute name. Value - {@code String} value of the specified attribute
     *
     * @param path full path to the XML file
     * @return {@code List} of {@code Map<String, String>}
     * which store attribute values in key-value form
     * @throws XMLParseException if a DocumentBuilder cannot be created or XML parsing error occurs
     */
    public static List<Map<String, String>> parseXML(String path) throws XMLParseException {
        List<Map<String, String>> data = new ArrayList<>();
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            // Загрузка XML-файла
            File xmlFile = new File(path);
            Document xmlDocument = docBuilder.parse(xmlFile);

            // Получение корневого элемента
            Element rootElement = xmlDocument.getDocumentElement();

            // Получение списка дочерних элементов
            NodeList nodeList = rootElement.getElementsByTagName("object");

            // Считывание значений атрибутов
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    data.add(getAttributes(element));
                }
            }

        } catch (Exception e) {
            throw new XMLParseException(e.getMessage());
        }

        return data;
    }

    /**
     * Extracts attribute values for given {@code Element}
     * @param element for which attribute values need to be extracted
     * @return {@code Map<String, String>} which stores attribute values.<p>
     * Key - an XML attribute name. Value - {@code String} value of the specified attribute
     */
    private static Map<String, String> getAttributes(Element element) {
        Element parentElement = (Element) element.getParentNode();
        String filename = parentElement.getElementsByTagName("filename").item(0).getTextContent();

        String name = element.getElementsByTagName("name").item(0).getTextContent();
        String xmin = element.getElementsByTagName("xmin").item(0).getTextContent();
        String ymin = element.getElementsByTagName("ymin").item(0).getTextContent();
        String xmax = element.getElementsByTagName("xmax").item(0).getTextContent();
        String ymax = element.getElementsByTagName("ymax").item(0).getTextContent();

        Map<String, String> attributes = new HashMap<>();
        attributes.put("filename", filename);
        attributes.put("name", name);
        attributes.put("xmin", xmin);
        attributes.put("ymin", ymin);
        attributes.put("xmax", xmax);
        attributes.put("ymax", ymax);

        return attributes;
    }
}
