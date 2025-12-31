package de.dasoftware.cryptpad.model;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Tree node representing an entry in the navigation tree
 * Extends DefaultMutableTreeNode with content and selection handling
 */
public class EntryTreeNode extends DefaultMutableTreeNode {
    
    private static final long serialVersionUID = -8527091360079777899L;
	private String content = "";
    private boolean selected = false;
    
    /**
     * Constructor with object
     * 
     * @param userObject Object to store in the node
     */
    public EntryTreeNode(Object userObject) {
        super(userObject);
    }
    
    /**
     * Constructor with title string
     * 
     * @param nodeTitle Title of the node
     */
    public EntryTreeNode(String nodeTitle) {
        super(nodeTitle);
    }
    
    /**
     * Gets the content of this node
     * 
     * @return Node content
     */
    public String getContent() {
        return content;
    }
    
    /**
     * Sets the content of this node
     * 
     * @param content Content to set
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * Checks if this node is selected
     * 
     * @return true if selected, false otherwise
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * Sets the selection state of this node
     * 
     * @param selected Selection state
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}