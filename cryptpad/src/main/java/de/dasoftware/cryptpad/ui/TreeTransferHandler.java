package de.dasoftware.cryptpad.ui;

import de.dasoftware.cryptpad.model.EntryTreeNode;
import de.dasoftware.cryptpad.model.IDataModel;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * TransferHandler for drag and drop operations in the tree
 * Allows moving nodes within the tree structure
 * 
 * @author DA-Software
 * @version 1.0.0
 */
public class TreeTransferHandler extends TransferHandler {
    
    private static final long serialVersionUID = 1L;
	private final IDataModel model;
    private final JTree tree;
    
    // Custom DataFlavor for tree nodes
    private final DataFlavor nodesFlavor;
    private final DataFlavor[] flavors = new DataFlavor[1];
    
    // The node being dragged
    private EntryTreeNode draggedNode = null;
    
    /**
     * Constructor
     * 
     * @param tree The JTree to handle drag and drop for
     * @param model The data model
     */
    public TreeTransferHandler(JTree tree, IDataModel model) {
        this.tree = tree;
        this.model = model;
        
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                             ";class=de.dasoftware.cryptpad.model.EntryTreeNode";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not create DataFlavor", e);
        }
    }
    
    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }
        
        support.setShowDropLocation(true);
        
        if (!support.isDataFlavorSupported(nodesFlavor)) {
            return false;
        }
        
        // Get drop location
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath dest = dl.getPath();
        
        if (dest == null) {
            return false;
        }
        
        EntryTreeNode target = (EntryTreeNode) dest.getLastPathComponent();
        
        // Don't allow dropping on itself or its children
        if (draggedNode != null) {
            if (target == draggedNode) {
                return false;
            }
            
            if (isNodeDescendant(draggedNode, target)) {
                return false;
            }                    
        }
        
        return true;
    }
    
    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath path = tree.getSelectionPath();
        
        if (path != null) {
            draggedNode = (EntryTreeNode) path.getLastPathComponent();
            
            // Don't allow dragging root's direct children (top-level nodes can still be moved)
            // Actually, let's allow it - but prevent dropping on root
            
            return new NodeTransferable(draggedNode);
        }
        
        return null;
    }
    
    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }
    
    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        
        // Get drop location
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath dest = dl.getPath();
        EntryTreeNode parent = (EntryTreeNode) dest.getLastPathComponent();
        int index = dl.getChildIndex();
        
        // Get the node being transferred
        try {
            EntryTreeNode transferNode = (EntryTreeNode) support.getTransferable()
                .getTransferData(nodesFlavor);
            
            // Perform the move in the model
            moveNode(transferNode, parent, index);
            
            // Mark as dirty - get MainWindow reference
            Window window = SwingUtilities.getWindowAncestor(tree);
            if (window instanceof MainWindow) {
                ((MainWindow) window).markDirty();  // You need to make this public!
            }
            
            // Select the moved node
            TreePath newPath = new TreePath(
                ((DefaultTreeModel) tree.getModel()).getPathToRoot(transferNode)
            );
            tree.setSelectionPath(newPath);
            tree.scrollPathToVisible(newPath);
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        // Clean up
        draggedNode = null;
    }
    
    /**
     * Moves a node to a new parent at the specified index
     * 
     * @param node Node to move
     * @param newParent New parent node
     * @param index Index in parent's children, or -1 to append
     */
    private void moveNode(EntryTreeNode node, EntryTreeNode newParent, int index) {
        // Remember old parent and position
        EntryTreeNode oldParent = (EntryTreeNode) node.getParent();
        int oldIndex = oldParent.getIndex(node);
        
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        
        // Save expanded state of ALL affected subtrees (old parent, new parent, and moved node)
        java.util.Set<EntryTreeNode> expandedNodes = new java.util.HashSet<>();
        saveExpandedNodes(oldParent, treeModel, expandedNodes);
        if (oldParent != newParent) {
            saveExpandedNodes(newParent, treeModel, expandedNodes);
        }
        
        // Remove from old parent
        oldParent.remove(node);
        
        // Adjust index if moving within same parent and target was after the removed node
        if (oldParent == newParent && index > oldIndex) {
            index--;
        }
        
        // Add to new parent
        if (index == -1 || index > newParent.getChildCount()) {
            // Drop on node (append as last child) or index out of bounds
            newParent.add(node);
        } else {
            // Drop between nodes
            newParent.insert(node, index);
        }
        
        // Notify model
        treeModel.nodeStructureChanged(oldParent);
        if (oldParent != newParent) {
            treeModel.nodeStructureChanged(newParent);
        }
        
        // Restore expanded state of all affected nodes
        restoreExpandedNodes(expandedNodes, treeModel);
    }

    /**
     * Saves all expanded nodes in a subtree
     * 
     * @param node The root node to start from
     * @param treeModel The tree model
     * @param expandedNodes Set to store expanded nodes
     */
    private void saveExpandedNodes(EntryTreeNode node, DefaultTreeModel treeModel, 
                                    java.util.Set<EntryTreeNode> expandedNodes) {
        TreePath path = new TreePath(treeModel.getPathToRoot(node));
        
        if (tree.isExpanded(path)) {
            expandedNodes.add(node);
        }
        
        // Recursively check children
        for (int i = 0; i < node.getChildCount(); i++) {
            EntryTreeNode child = (EntryTreeNode) node.getChildAt(i);
            saveExpandedNodes(child, treeModel, expandedNodes);
        }
    }

    /**
     * Restores expanded state after a move operation
     * 
     * @param expandedNodes Set of nodes that were expanded
     * @param treeModel The tree model
     */
    private void restoreExpandedNodes(java.util.Set<EntryTreeNode> expandedNodes, 
                                       DefaultTreeModel treeModel) {
        for (EntryTreeNode node : expandedNodes) {
            TreePath path = new TreePath(treeModel.getPathToRoot(node));
            tree.expandPath(path);
        }
    }
    
    /**
     * Checks if a node is a descendant of another node
     * 
     * @param ancestor Potential ancestor
     * @param descendant Potential descendant
     * @return true if descendant is a child/grandchild/... of ancestor
     */
    private boolean isNodeDescendant(EntryTreeNode ancestor, EntryTreeNode descendant) {
        if (ancestor == descendant) {
            return true;
        }
        
        for (int i = 0; i < ancestor.getChildCount(); i++) {
            EntryTreeNode child = (EntryTreeNode) ancestor.getChildAt(i);
            if (isNodeDescendant(child, descendant)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Inner class for making nodes transferable
     */
    private class NodeTransferable implements Transferable {
        
        private final EntryTreeNode node;
        
        public NodeTransferable(EntryTreeNode node) {
            this.node = node;
        }
        
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }
        
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) 
                throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return node;
        }
    }
}