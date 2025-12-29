package de.dasoftware.cryptpad.model;

import javax.swing.tree.DefaultTreeModel;

/**
 * Data model interface for managing encrypted content
 * 
 * @author DA-Software
 * @version 1.0.0
 */
public interface IDataModel {
    
    /**
     * Saves the data to an encrypted file
     * 
     * @param file File path to save to
     * @throws Exception If save operation fails
     */
    void saveFile(String file) throws Exception;
    
    /**
     * Loads data from an encrypted file
     * 
     * @param file File path to load from
     * @throws Exception If load operation fails
     */
    void loadFile(String file) throws Exception;
    
    /**
     * Subscribes an observer to model changes
     * 
     * @param observer Observer to subscribe
     */
    void subscribe(IObserver observer);
    
    /**
     * Unsubscribes an observer from model changes
     * 
     * @param observer Observer to unsubscribe
     */
    void unsubscribe(IObserver observer);
    
    /**
     * Notifies all observers about model changes
     */
    void refreshObservers();
    
    /**
     * Adds a new node to the tree
     * 
     * @param parent Parent node (null for root level)
     * @param title Node title
     * @return The created node
     */
    EntryTreeNode addNode(EntryTreeNode parent, String title);
    
    /**
     * Deletes a node and all its children from the tree
     * 
     * @param node Node to delete
     */
    void deleteNode(EntryTreeNode node);
    
    /**
     * Gets the root node of the tree
     * 
     * @return Root node
     */
    EntryTreeNode getRootNode();
    
    /**
     * Gets the current encryption password
     * 
     * @return Password string
     */
    String getPassword();
    
    /**
     * Sets the encryption password
     * 
     * @param password Password to set
     */
    void setPassword(String password);
    
    /**
     * Sets the title of a node
     * 
     * @param node Node to modify
     * @param title New title
     */
    void setNodeTitle(EntryTreeNode node, String title);
    
    /**
     * Sets the content of a node
     * 
     * @param node Node to modify
     * @param content New content
     */
    void setNodeContent(EntryTreeNode node, String content);
    
    /**
     * Gets the tree model for UI binding
     * 
     * @return Swing tree model
     */
    DefaultTreeModel getTreeModel();
    
    /**
     * Clears all data from the model
     */
    void clearModel();
}