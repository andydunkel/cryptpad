package de.dasoftware.updater;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Remote update data from server
 * Contains information about available updates
 */
public class RemoteUpdateData {
    
    private boolean isValid;
    private int majorVersion;
    private int minorVersion;
    private int bugfixVersion;
    private String downloadUrl;
    private String softwareUrl;
    private String informationUrl;
    private String setupParam;
    private LocalDate releaseDate;
    
    /**
     * Constructor with default values
     */
    public RemoteUpdateData() {
        this.majorVersion = 0;
        this.minorVersion = 0;
        this.bugfixVersion = 0;
        this.downloadUrl = "";
        this.setupParam = "";
        this.softwareUrl = "";
        this.informationUrl = "";
        this.isValid = false;
        this.releaseDate = null;
    }
    
    /**
     * Gets version as formatted string
     * 
     * @return Version string in format x.x.x
     */
    public String getVersionString() {
        return majorVersion + "." + minorVersion + "." + bugfixVersion;
    }
    
    /**
     * Reads and parses update data from XML string
     * Expected XML format:
     * <pre>
     * &lt;xml&gt;
     *   &lt;version major="1" minor="2" bugfix="3"/&gt;
     *   &lt;url&gt;http://download-url&lt;/url&gt;
     *   &lt;infourl&gt;http://info-url&lt;/infourl&gt;
     *   &lt;software_url&gt;http://software-url&lt;/software_url&gt;
     *   &lt;releaseDate&gt;2024-01-01&lt;/releaseDate&gt;
     *   &lt;setup_param&gt;/silent&lt;/setup_param&gt;
     * &lt;/xml&gt;
     * </pre>
     * 
     * @param xml XML string to parse
     * @throws Exception If XML parsing fails
     */
    public void readFromXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        doc.getDocumentElement().normalize();
        
        // Parse version
        Node versionNode = doc.getElementsByTagName("version").item(0);
        if (versionNode != null && versionNode.getNodeType() == Node.ELEMENT_NODE) {
            Element versionElement = (Element) versionNode;
            
            majorVersion = Integer.parseInt(versionElement.getAttribute("major"));
            minorVersion = Integer.parseInt(versionElement.getAttribute("minor"));
            bugfixVersion = Integer.parseInt(versionElement.getAttribute("bugfix"));
        }
        
        // Parse download URL
        downloadUrl = getNodeText(doc, "url");
        
        // Parse information URL
        informationUrl = getNodeText(doc, "infourl");
        
        // Parse software URL (optional)
        try {
            softwareUrl = getNodeText(doc, "software_url");
        } catch (Exception e) {
            // Optional field, ignore if missing
            softwareUrl = "";
        }
        
        // Parse release date (optional)
        try {
            String dateStr = getNodeText(doc, "releaseDate");
            if (dateStr != null && !dateStr.isEmpty()) {
                // Try common date formats
                releaseDate = parseDate(dateStr);
            }
        } catch (Exception e) {
            // Optional field, ignore if missing or invalid
            releaseDate = null;
        }
        
        // Parse setup parameter
        setupParam = getNodeText(doc, "setup_param");
        
        isValid = true;
    }
    
    /**
     * Gets text content of an XML node
     * 
     * @param doc XML document
     * @param tagName Tag name to search for
     * @return Text content or empty string
     */
    private String getNodeText(Document doc, String tagName) {
        Node node = doc.getElementsByTagName(tagName).item(0);
        if (node != null) {
            return node.getTextContent();
        }
        return "";
    }
    
    /**
     * Parses date string in various formats
     * 
     * @param dateStr Date string
     * @return Parsed LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        // Try ISO format first (yyyy-MM-dd)
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            // Try other common formats
            String[] formats = {
                "dd.MM.yyyy",
                "dd/MM/yyyy",
                "MM/dd/yyyy",
                "yyyy-MM-dd"
            };
            
            for (String format : formats) {
                try {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
                } catch (DateTimeParseException ex) {
                    // Try next format
                }
            }
            
            throw new DateTimeParseException("Unable to parse date", dateStr, 0);
        }
    }
    
    // ========== Getters and Setters ==========
    
    public boolean isValid() {
        return isValid;
    }
    
    public void setValid(boolean valid) {
        isValid = valid;
    }
    
    public int getMajorVersion() {
        return majorVersion;
    }
    
    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }
    
    public int getMinorVersion() {
        return minorVersion;
    }
    
    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }
    
    public int getBugfixVersion() {
        return bugfixVersion;
    }
    
    public void setBugfixVersion(int bugfixVersion) {
        this.bugfixVersion = bugfixVersion;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    
    public String getSoftwareUrl() {
        return softwareUrl;
    }
    
    public void setSoftwareUrl(String softwareUrl) {
        this.softwareUrl = softwareUrl;
    }
    
    public String getInformationUrl() {
        return informationUrl;
    }
    
    public void setInformationUrl(String informationUrl) {
        this.informationUrl = informationUrl;
    }
    
    public String getSetupParam() {
        return setupParam;
    }
    
    public void setSetupParam(String setupParam) {
        this.setupParam = setupParam;
    }
    
    public LocalDate getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }
}