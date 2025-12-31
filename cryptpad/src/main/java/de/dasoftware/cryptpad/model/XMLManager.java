package de.dasoftware.cryptpad.model;

import de.dasoftware.cryptpad.crypto.EncryptionWrapper;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * XML Manager for saving and loading encrypted XML files
 * Handles serialization of the tree structure to/from encrypted XML format
 */
public class XMLManager implements IXMLManager {
    
    private static final String XML_VERSION = "1";
    private static final String APP_NAME = "DA-CryptPad";
    
    private String filename;
    private IDataModel model;
    private Document dom;
    
    /**
     * Default constructor
     */
    public XMLManager() {
        this.filename = "";
    }
    
    /**
     * Constructor with filename and model
     * 
     * @param filename File path for save/load operations
     * @param model Data model to save/load
     */
    public XMLManager(String filename, IDataModel model) {
        this.filename = filename;
        this.model = model;
    }
    
    /**
     * Saves the document to an encrypted XML file
     * 
     * @throws Exception If save operation fails
     */
    @Override
    public void saveDocument() throws Exception {
        // Create DOM document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        dom = db.newDocument();
        
        // Build XML tree structure
        createDOMTree();
        
        // Write to encrypted file
        writeToEncryptedFile();
    }
    
    /**
     * Creates the DOM tree structure from the data model
     */
    private void createDOMTree() {
        // Create root element
        Element rootElement = dom.createElement("xml");
        dom.appendChild(rootElement);
        
        // Create file info section
        Element fileInfo = dom.createElement("fileinfo");
        Element appName = dom.createElement("appname");
        appName.setAttribute("version", XML_VERSION);
        
        Text appNameText = dom.createTextNode(APP_NAME);
        appName.appendChild(appNameText);
        fileInfo.appendChild(appName);
        rootElement.appendChild(fileInfo);
        
        // Create entries section
        Element entries = dom.createElement("entries");
        Element rootEntry = nodeToXML(model.getRootNode());
        entries.appendChild(rootEntry);
        rootElement.appendChild(entries);
    }
    
    /**
     * Converts a tree node to XML element (recursive)
     * 
     * @param node Tree node to convert
     * @return XML element representing the node
     */
    private Element nodeToXML(EntryTreeNode node) {
        Element element = dom.createElement("entry");
        
        // Create title element
        Element title = dom.createElement("title");
        Text titleText = dom.createTextNode(node.toString());
        title.appendChild(titleText);
        element.appendChild(title);
        
        // Create content element
        Element content = dom.createElement("content");
        content.setAttribute("type", "text");
        Text contentText = dom.createTextNode(node.getContent());
        content.appendChild(contentText);
        element.appendChild(content);
        
        // Recursively process child nodes
        Enumeration<?> children = node.children();
        while (children.hasMoreElements()) {
            EntryTreeNode childNode = (EntryTreeNode) children.nextElement();
            element.appendChild(nodeToXML(childNode));
        }
        
        return element;
    }
    
    /**
     * Writes the DOM document to an encrypted file
     * 
     * @throws Exception If write operation fails
     */
    private void writeToEncryptedFile() throws Exception {
        // Transform DOM to string
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        
        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);
        DOMSource source = new DOMSource(dom);
        
        transformer.transform(source, streamResult);
        String xmlText = stringWriter.toString();
        
        // Encrypt the XML content
        EncryptionWrapper encryption = new EncryptionWrapper();
        String encryptedText = encryption.encryptFile(xmlText, model.getPassword());
        
        // Write encrypted content to file
        try (FileWriter fileWriter = new FileWriter(filename, StandardCharsets.UTF_8)) {
            fileWriter.write(encryptedText);
        }
    }
    
    /**
     * Loads a document from an encrypted XML file
     * 
     * @param filename File path to load from
     * @throws Exception If load operation fails
     */
    @Override
    public void loadDocument(String filename) throws Exception {
        // Read encrypted file content
        String encryptedContent = readEncryptedFile(filename);
        
        // Decrypt the content
        EncryptionWrapper encryption = new EncryptionWrapper();
        String xmlText = encryption.decryptMessage(encryptedContent, model.getPassword());
        
        // Parse XML
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        
        StringReader stringReader = new StringReader(xmlText);
        InputSource inputSource = new InputSource(stringReader);
        dom = db.parse(inputSource);
        
        // Clear existing model and load new data
        model.clearModel();
        
        // Get root element and parse entries
        Element documentElement = dom.getDocumentElement();
        NodeList entryList = documentElement.getElementsByTagName("entry");
        
        if (entryList.getLength() > 0) {
            Element firstEntry = (Element) entryList.item(0);
            parseEntries(firstEntry, model.getRootNode());
        }
    }
    
    /**
     * Reads and returns the content of an encrypted file
     * 
     * @param filename File path to read
     * @return File content as string
     * @throws IOException If read operation fails
     */
    private String readEncryptedFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(filename, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    /**
     * Parses XML entries and builds the tree structure (recursive)
     * 
     * @param element XML element to parse
     * @param parentNode Parent tree node to add children to
     */
    private void parseEntries(Element element, EntryTreeNode parentNode) {
        NodeList children = element.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
            
            // Only process element nodes with name "entry"
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                
                if ("entry".equals(childElement.getNodeName())) {
                    // Create tree node from XML
                    String title = getTextValue(childElement, "title");
                    String content = getTextValue(childElement, "content");
                    
                    EntryTreeNode treeNode = new EntryTreeNode(title);
                    treeNode.setContent(content);
                    parentNode.add(treeNode);
                    
                    // Recursively parse children
                    if (childElement.hasChildNodes()) {
                        parseEntries(childElement, treeNode);
                    }
                }
            }
        }
    }
    
    /**
     * Gets the text value of a child element by tag name
     * 
     * @param element Parent element
     * @param tagName Tag name to search for
     * @return Text value or empty string if not found
     */
    private String getTextValue(Element element, String tagName) {
        try {
            NodeList nodeList = element.getElementsByTagName(tagName);
            
            if (nodeList != null && nodeList.getLength() > 0) {
                Element childElement = (Element) nodeList.item(0);
                Node firstChild = childElement.getFirstChild();
                
                if (firstChild != null) {
                    return firstChild.getNodeValue();
                }
            }
        } catch (Exception e) {
            // Return empty string on any error
        }
        
        return "";
    }
    
    // ========== Getters and Setters ==========
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public IDataModel getModel() {
        return model;
    }
    
    public void setModel(IDataModel model) {
        this.model = model;
    }
}