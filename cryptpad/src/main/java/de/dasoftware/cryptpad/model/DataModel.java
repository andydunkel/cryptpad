package de.dasoftware.cryptpad.model;

import de.dasoftware.cryptpad.i18n.Messages;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;

/**
 * Data model for managing the application's tree structure and encrypted content
 */
public class DataModel implements IDataModel {
    
    private EntryTreeNode rootNode;
    private List<IObserver> observers;
    private String password = "";
    private DefaultTreeModel treeModel;
    private IXMLManager xmlManager;
    
    public DataModel() {
        rootNode = new EntryTreeNode(Messages.getString("tree.rootnode"));
        observers = new ArrayList<>();
        
        // Add default node with better name
        EntryTreeNode defaultNode = new EntryTreeNode(Messages.getString("tree.defaultnode"));
        defaultNode.setContent(Messages.getString("tree.defaultcontent"));
        rootNode.add(defaultNode);
        
        treeModel = new DefaultTreeModel(rootNode);
    }
    
    /**
     * Saves the data to an encrypted file
     * Delegates to XMLManager for actual file operations
     * 
     * @param file File path to save to
     * @throws Exception If save operation fails
     */
    @Override
    public void saveFile(String file) throws Exception {
        xmlManager = new XMLManager(file, this);
        xmlManager.saveDocument();
    }
    
    /**
     * Loads data from an encrypted file
     * Delegates to XMLManager for actual file operations
     * 
     * @param file File path to load from
     * @throws Exception If load operation fails
     */
    @Override
    public void loadFile(String file) throws Exception {
        xmlManager = new XMLManager(file, this);
        xmlManager.loadDocument(file);
        refreshObservers();
    }
    
    /**
     * Adds a new node to the tree
     * If no parent is specified, the node is added to the root level
     * 
     * @param parent Parent node (null for root level)
     * @param nodeTitle Title of the new node
     * @return The newly created node
     */
    @Override
    public EntryTreeNode addNode(EntryTreeNode parent, String nodeTitle) {
        EntryTreeNode newNode = new EntryTreeNode(nodeTitle);
        
        if (parent != null) {
            parent.add(newNode);
        } else {
            rootNode.add(newNode);
        }
        
        refreshObservers();
        return newNode;
    }
    
    /**
     * Subscribes an observer to model changes
     * 
     * @param observer Observer to subscribe
     */
    @Override
    public void subscribe(IObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            refreshObservers();
        }
    }
    
    /**
     * Unsubscribes an observer from model changes
     * 
     * @param observer Observer to unsubscribe
     */
    @Override
    public void unsubscribe(IObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Notifies all observers about model changes
     */
    @Override
    public void refreshObservers() {
        for (IObserver observer : observers) {
            observer.refresh();
        }
    }
    
    /**
     * Gets the root node of the tree
     * 
     * @return Root node
     */
    @Override
    public EntryTreeNode getRootNode() {
        return rootNode;
    }
    
    /**
     * Deletes a node and all its children from the tree
     * 
     * @param node Node to delete
     */
    @Override
    public void deleteNode(EntryTreeNode node) {
        treeModel.removeNodeFromParent(node);
        refreshObservers();
    }
    
    /**
     * Gets the current encryption password
     * 
     * @return Password string (empty if not set)
     */
    @Override
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the encryption password for this file
     * 
     * @param password Password to set
     */
    @Override
    public void setPassword(String password) {
        this.password = password != null ? password : "";
    }
    
    /**
     * Gets the tree model for UI binding
     * 
     * @return Swing tree model
     */
    @Override
    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }
    
    /**
     * Sets the title of a given node
     * 
     * @param node Node to modify
     * @param title New title
     */
    @Override
    public void setNodeTitle(EntryTreeNode node, String title) {
        node.setUserObject(title);
        refreshObservers();
    }
    
    /**
     * Sets the content of a given node
     * 
     * @param node Node to modify
     * @param content New content text
     */
    @Override
    public void setNodeContent(EntryTreeNode node, String content) {
        node.setContent(content);
    }
    
    /**
     * Clears all data from the model
     * Creates a new empty tree structure
     */
    @Override
    public void clearModel() {
        rootNode = new EntryTreeNode(Messages.getString("tree.rootnode"));
        treeModel.setRoot(rootNode);
        refreshObservers();
    }
}