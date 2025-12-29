package de.dasoftware.cryptpad.model;

/**
 * XML Manager interface for file operations
 * Allows support for different file format versions in future software releases
 * 
 * @author DA-Software
 * @version 1.0.0
 */
public interface IXMLManager {
    
    /**
     * Saves the document to XML format
     * 
     * @throws Exception If save operation fails
     */
    void saveDocument() throws Exception;
    
    /**
     * Loads a document from XML format
     * 
     * @param filename Path to the XML file to load
     * @throws Exception If load operation fails
     */
    void loadDocument(String filename) throws Exception;
}