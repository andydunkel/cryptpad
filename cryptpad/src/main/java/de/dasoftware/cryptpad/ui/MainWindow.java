package de.dasoftware.cryptpad.ui;

import de.dasoftware.cryptpad.Constants;
import de.dasoftware.cryptpad.model.DataModel;
import de.dasoftware.cryptpad.model.EntryTreeNode;
import de.dasoftware.cryptpad.model.IDataModel;
import de.dasoftware.cryptpad.model.IObserver;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;

/**
 * Main window of DA-CryptPad application
 */
public class MainWindow extends JFrame implements IObserver {
    
    // Model
    private IDataModel model;
    private boolean saved = false;
    private String savedFileName = "";
    
    // Main components
    private JSplitPane splitPane;
    private JTree navigationTree;
    private JScrollPane treeScrollPane;
    private JEditorPane contentEditor;
    private JScrollPane editorScrollPane;
    
    // Toolbar
    private JToolBar toolBar;
    private JButton btnNew;
    private JButton btnOpen;
    private JButton btnSave;
    private JButton btnNewMainNode;
    private JButton btnNewSibling;
    private JButton btnNewChild;
    private JButton btnEditNode;
    private JButton btnDeleteNode;
    private JButton btnCut;
    private JButton btnCopy;
    private JButton btnPaste;
    private JButton btnAbout;
    
    // Menu
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenu menuEdit;
    private JMenu menuEncryption;
    private JMenu menuImport;
    private JMenu menuHelp;
    
    // File menu items
    private JMenuItem menuItemNew;
    private JMenuItem menuItemOpen;
    private JMenuItem menuItemSave;
    private JMenuItem menuItemSaveAs;
    private JMenuItem menuItemExit;
    
    // Edit menu items
    private JMenuItem menuItemCut;
    private JMenuItem menuItemCopy;
    private JMenuItem menuItemPaste;
    private JMenuItem menuItemDeleteNode;
    
    // Encryption menu items
    private JMenuItem menuItemSetPassword;
    private JMenuItem menuItemPasswordGenerator;
    private JMenuItem menuItemTextEncryption;
    
    // Help menu items
    private JMenuItem menuItemAbout;
    
    // Popup menu for tree
    private JPopupMenu treePopupMenu;
    private JMenuItem popupNewMainNode;
    private JMenuItem popupNewSibling;
    private JMenuItem popupNewChild;
    private JMenuItem popupEditNode;
    private JMenuItem popupDeleteNode;
    
    /**
     * Constructor
     * 
     * @param model Data model
     */
    public MainWindow(IDataModel model) {
        this.model = model;
                
        initComponents();
        setupLayout();
        setupListeners();
        
        this.model.subscribe(this);
        
        updateTitle("untitled." + Constants.FILE_EXTENSION);
    }
    
    /**
     * Initializes all components
     */
    private void initComponents() {
        // Frame settings
        setTitle(Constants.APP_NAME);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        
        // Set application icon
        setApplicationIcon();
        
        // Initialize toolbar
        initToolBar();
        
        // Initialize menu
        initMenu();
        
        // Initialize tree
        navigationTree = new JTree(model.getTreeModel());
        navigationTree.setRootVisible(false);
        navigationTree.setShowsRootHandles(true);
        navigationTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        // Tree icons
        try {
            ImageIcon leafIcon = new ImageIcon(getClass().getResource("/icons/leaf.png"));
            ImageIcon folderIcon = new ImageIcon(getClass().getResource("/icons/folder_closed16.png"));
            ImageIcon openIcon = new ImageIcon(getClass().getResource("/icons/folder_open16.png"));
            
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setLeafIcon(leafIcon);
            renderer.setClosedIcon(folderIcon);
            renderer.setOpenIcon(openIcon);
            navigationTree.setCellRenderer(renderer);
        } catch (Exception e) {
            System.err.println("Could not load tree icons: " + e.getMessage());
        }
        
        treeScrollPane = new JScrollPane(navigationTree);
        treeScrollPane.setMinimumSize(new Dimension(200, 300));
        
        // Initialize editor
        contentEditor = new JEditorPane();
        contentEditor.setFont(new Font("Courier New", Font.PLAIN, 12));
        editorScrollPane = new JScrollPane(contentEditor);
        
        // Initialize split pane
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, editorScrollPane);
        splitPane.setDividerLocation(240);
        
        // Initialize tree popup menu
        initTreePopupMenu();
        navigationTree.setComponentPopupMenu(treePopupMenu);
    }

    /**
     * Sets the application icon for this window
     */
    private void setApplicationIcon() {
        try {
            // Load icon image(s)
            ImageIcon icon16 = new ImageIcon(getClass().getResource("/icons/app-icon-16.png"));
            ImageIcon icon32 = new ImageIcon(getClass().getResource("/icons/app-icon-32.png"));
            ImageIcon icon48 = new ImageIcon(getClass().getResource("/icons/app-icon-48.png"));
            ImageIcon icon64 = new ImageIcon(getClass().getResource("/icons/app-icon-64.png"));
            
            // Set multiple icon sizes (for different contexts)
            java.util.List<Image> icons = new java.util.ArrayList<>();
            icons.add(icon16.getImage());
            icons.add(icon32.getImage());
            icons.add(icon48.getImage());
            icons.add(icon64.getImage());
            
            setIconImages(icons);
            
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
            
            // Fallback: Try to load just one icon
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource("/icons/app-icon.png"));
                setIconImage(icon.getImage());
            } catch (Exception ex) {
                System.err.println("Could not load fallback icon: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Initializes the toolbar
     */
    private void initToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        // File operations
        btnNew = createToolBarButton("/icons/tb_new.png", "New File");
        btnOpen = createToolBarButton("/icons/tb_open.png", "Open File");
        btnSave = createToolBarButton("/icons/tb_save.png", "Save File");
        
        toolBar.add(btnNew);
        toolBar.add(btnOpen);
        toolBar.add(btnSave);
        toolBar.addSeparator();
        
        // Node operations
        btnNewMainNode = createToolBarButton("/icons/mainnode.png", "Add Main Node");
        btnNewSibling = createToolBarButton("/icons/sibling.png", "Add Sibling");
        btnNewChild = createToolBarButton("/icons/childnode.png", "Add Child Node");
        btnEditNode = createToolBarButton("/icons/edit.png", "Edit Node");
        btnDeleteNode = createToolBarButton("/icons/delete16.png", "Delete Node");
        
        toolBar.add(btnNewMainNode);
        toolBar.add(btnNewSibling);
        toolBar.add(btnNewChild);
        toolBar.add(btnEditNode);
        toolBar.add(btnDeleteNode);
        toolBar.addSeparator();
        
        // Edit operations
        btnCut = createToolBarButton("/icons/cut16.png", "Cut");
        btnCopy = createToolBarButton("/icons/copy16.png", "Copy");
        btnPaste = createToolBarButton("/icons/paste16.png", "Paste");
        
        toolBar.add(btnCut);
        toolBar.add(btnCopy);
        toolBar.add(btnPaste);
        toolBar.addSeparator();
        
        // Help
        btnAbout = createToolBarButton("/icons/Info.png", "About " + Constants.APP_NAME);
        toolBar.add(btnAbout);
    }
    
    /**
     * Creates a toolbar button with icon and tooltip
     */
    private JButton createToolBarButton(String iconPath, String tooltip) {
        JButton button = new JButton();
        
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            button.setIcon(icon);
        } catch (Exception e) {
            System.err.println("Could not load icon: " + iconPath);
            button.setText(tooltip.substring(0, 3));
        }
        
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        return button;
    }
    
    /**
     * Initializes the menu bar
     */
    private void initMenu() {
        menuBar = new JMenuBar();
        
        // File menu
        menuFile = new JMenu("File");
        menuFile.setMnemonic('F');
        
        menuItemNew = createMenuItem("New", "/icons/tb_new.png", 'N', 
                KeyStroke.getKeyStroke("control N"));
        menuItemOpen = createMenuItem("Open", "/icons/tb_open.png", 'O', 
                KeyStroke.getKeyStroke("control O"));
        menuItemSave = createMenuItem("Save", "/icons/tb_save.png", 'S', 
                KeyStroke.getKeyStroke("control S"));
        menuItemSaveAs = createMenuItem("Save as", null, 'A', null);
        menuItemExit = createMenuItem("Exit", "/icons/exit.png", 'E', 
                KeyStroke.getKeyStroke("alt F4"));
        
        menuFile.add(menuItemNew);
        menuFile.add(menuItemOpen);
        menuFile.add(menuItemSave);
        menuFile.add(menuItemSaveAs);
        menuFile.addSeparator();
        menuFile.add(menuItemExit);
        
        // Edit menu
        menuEdit = new JMenu("Edit");
        menuEdit.setMnemonic('E');
        
        menuItemCut = createMenuItem("Cut", "/icons/cut16.png", 'C', 
                KeyStroke.getKeyStroke("control X"));
        menuItemCopy = createMenuItem("Copy", "/icons/copy16.png", 'O', 
                KeyStroke.getKeyStroke("control C"));
        menuItemPaste = createMenuItem("Paste", "/icons/paste16.png", 'P', 
                KeyStroke.getKeyStroke("control V"));
        menuItemDeleteNode = createMenuItem("Delete Node", "/icons/delete16.png", 'D', null);
        
        menuEdit.add(menuItemCut);
        menuEdit.add(menuItemCopy);
        menuEdit.add(menuItemPaste);
        menuEdit.addSeparator();
        menuEdit.add(menuItemDeleteNode);
        
        // Encryption menu
        menuEncryption = new JMenu("Encryption");
        
        menuItemSetPassword = createMenuItem("Set password for this file", null, 'S', null);
        menuItemPasswordGenerator = createMenuItem("Password Generator", null, 'P', 
                KeyStroke.getKeyStroke("control alt P"));
        menuItemTextEncryption = createMenuItem("Text Encryption", null, 'T', 
                KeyStroke.getKeyStroke("control alt T"));
        
        menuEncryption.add(menuItemSetPassword);
        menuEncryption.addSeparator();
        menuEncryption.add(menuItemPasswordGenerator);
        menuEncryption.add(menuItemTextEncryption);
        
        // Help menu
        menuHelp = new JMenu("Help");
        menuHelp.setMnemonic('H');
        menuItemAbout = createMenuItem("About " + Constants.APP_NAME, "/icons/Info.png", 'A', null);
        menuHelp.add(menuItemAbout);
        
        // Add menus to menu bar
        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuEncryption);
        menuBar.add(menuHelp);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Creates a menu item with optional icon, mnemonic and accelerator
     */
    private JMenuItem createMenuItem(String text, String iconPath, char mnemonic, KeyStroke accelerator) {
        JMenuItem item = new JMenuItem(text);
        
        if (iconPath != null) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
                item.setIcon(icon);
            } catch (Exception e) {
                System.err.println("Could not load icon: " + iconPath);
            }
        }
        
        if (mnemonic != 0) {
            item.setMnemonic(mnemonic);
        }
        
        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }
        
        return item;
    }
    
    /**
     * Initializes the tree popup menu
     */
    private void initTreePopupMenu() {
        treePopupMenu = new JPopupMenu();
        
        popupNewMainNode = createMenuItem("New main node", "/icons/mainnode.png", (char)0, null);
        popupNewSibling = createMenuItem("Add new sibling node", "/icons/sibling.png", (char)0, null);
        popupNewChild = createMenuItem("Add new child node", "/icons/childnode.png", (char)0, null);
        popupEditNode = createMenuItem("Edit Node", "/icons/edit.png", (char)0, null);
        popupDeleteNode = createMenuItem("Delete item", "/icons/delete16.png", (char)0, null);
        
        treePopupMenu.add(popupNewMainNode);
        treePopupMenu.add(popupNewSibling);
        treePopupMenu.add(popupNewChild);
        treePopupMenu.addSeparator();
        treePopupMenu.add(popupEditNode);
        treePopupMenu.add(popupDeleteNode);
    }
    
    /**
     * Sets up the layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        add(toolBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * Sets up event listeners
     */
    private void setupListeners() {
        // Window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onWindowClose();
            }
        });
        
        // Tree selection
        navigationTree.addTreeSelectionListener(this::onTreeSelectionChanged);
        
        // Toolbar buttons
        btnNew.addActionListener(this::onNewFile);
        btnOpen.addActionListener(this::onOpen);
        btnSave.addActionListener(this::onSave);
        btnNewMainNode.addActionListener(this::onNewMainNode);
        btnNewSibling.addActionListener(this::onNewSiblingNode);
        btnNewChild.addActionListener(this::onNewChildNode);
        btnEditNode.addActionListener(this::onEditNode);
        btnDeleteNode.addActionListener(this::onDeleteNode);
        btnCut.addActionListener(this::onCut);
        btnCopy.addActionListener(this::onCopy);
        btnPaste.addActionListener(this::onPaste);
        btnAbout.addActionListener(this::onAbout);
        
        // Menu items - File
        menuItemNew.addActionListener(this::onNewFile);
        menuItemOpen.addActionListener(this::onOpen);
        menuItemSave.addActionListener(this::onSave);
        menuItemSaveAs.addActionListener(this::onSaveAs);
        menuItemExit.addActionListener(this::onExit);
        
        // Menu items - Edit
        menuItemCut.addActionListener(this::onCut);
        menuItemCopy.addActionListener(this::onCopy);
        menuItemPaste.addActionListener(this::onPaste);
        menuItemDeleteNode.addActionListener(this::onDeleteNode);
        
        // Menu items - Encryption
        menuItemSetPassword.addActionListener(this::onSetPassword);
        menuItemPasswordGenerator.addActionListener(this::onPasswordGenerator);
        menuItemTextEncryption.addActionListener(this::onTextEncryption);
       
        
        // Menu items - Help
        menuItemAbout.addActionListener(this::onAbout);
        
        // Popup menu
        popupNewMainNode.addActionListener(this::onNewMainNode);
        popupNewSibling.addActionListener(this::onNewSiblingNode);
        popupNewChild.addActionListener(this::onNewChildNode);
        popupEditNode.addActionListener(this::onEditNode);
        popupDeleteNode.addActionListener(this::onDeleteNode);
    }
    
    // ========== Event Handlers ==========
    
    /**
     * Handles tree selection changes
     */
    private void onTreeSelectionChanged(TreeSelectionEvent evt) {
        TreePath oldPath = evt.getOldLeadSelectionPath();
        TreePath newPath = evt.getNewLeadSelectionPath();
        
        // Save content of previously selected node
        if (oldPath != null) {
            try {
                EntryTreeNode oldNode = (EntryTreeNode) oldPath.getLastPathComponent();
                model.setNodeContent(oldNode, contentEditor.getText());
            } catch (Exception e) {
                // Node might not be EntryTreeNode
            }
        }
        
        // Load content of newly selected node
        if (newPath != null) {
            EntryTreeNode newNode = (EntryTreeNode) newPath.getLastPathComponent();
            contentEditor.setText(newNode.getContent());
            contentEditor.setCaretPosition(0);
        }
    }
    
    /**
     * Handler for New File
     */
    private void onNewFile(ActionEvent e) {
        int result = showSaveConfirmation();
        if (result != JOptionPane.CANCEL_OPTION) {
            createNewFile();
        }
    }
    
    /**
     * Handler for Open File
     */
    private void onOpen(ActionEvent e) {
        int result = showSaveConfirmation();
        if (result != JOptionPane.CANCEL_OPTION) {
            showOpenFileDialog();
        }
    }
    
    /**
     * Handler for Save File
     */
    private void onSave(ActionEvent e) {
        saveFile(false);
    }
    
    /**
     * Handler for Save As
     */
    private void onSaveAs(ActionEvent e) {
        saveFile(true);
    }
    
    /**
     * Handler for Exit
     */
    private void onExit(ActionEvent e) {
        onWindowClose();
    }
    
    /**
     * Handler for New Main Node
     */
    private void onNewMainNode(ActionEvent e) {
        NodeTitleDialog dialog = new NodeTitleDialog(this, true);
        dialog.setVisible(true);
        
        if (dialog.getModalResult()) {
            String title = dialog.getNodeTitle();
            EntryTreeNode newNode = model.addNode(null, title);
            
            // Select the new node
            TreePath path = new TreePath(model.getTreeModel().getPathToRoot(newNode));
            navigationTree.setSelectionPath(path);
            navigationTree.scrollPathToVisible(path);
        }
    }
    
    /**
     * Handler for New Sibling Node
     */
    private void onNewSiblingNode(ActionEvent e) {
        TreePath path = navigationTree.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a node first", 
                    "No selection", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        EntryTreeNode selectedNode = (EntryTreeNode) path.getLastPathComponent();
        EntryTreeNode parentNode = (EntryTreeNode) selectedNode.getParent();
        
        NodeTitleDialog dialog = new NodeTitleDialog(this, true);
        dialog.setVisible(true);
        
        if (dialog.getModalResult()) {
            String title = dialog.getNodeTitle();
            EntryTreeNode newNode = model.addNode(parentNode, title);
            
            // Select the new node
            TreePath newPath = new TreePath(model.getTreeModel().getPathToRoot(newNode));
            navigationTree.setSelectionPath(newPath);
            navigationTree.scrollPathToVisible(newPath);
        }
    }
    
    /**
     * Handler for New Child Node
     */
    private void onNewChildNode(ActionEvent e) {
        TreePath path = navigationTree.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a node first", 
                    "No selection", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        EntryTreeNode selectedNode = (EntryTreeNode) path.getLastPathComponent();
        
        NodeTitleDialog dialog = new NodeTitleDialog(this, true);
        dialog.setVisible(true);
        
        if (dialog.getModalResult()) {
            String title = dialog.getNodeTitle();
            EntryTreeNode newNode = model.addNode(selectedNode, title);
            
            // Expand parent and select new node
            navigationTree.expandPath(path);
            TreePath newPath = new TreePath(model.getTreeModel().getPathToRoot(newNode));
            navigationTree.setSelectionPath(newPath);
            navigationTree.scrollPathToVisible(newPath);
        }
    }
    
    /**
     * Handler for Edit Node
     */
    private void onEditNode(ActionEvent e) {
        TreePath path = navigationTree.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a node first", 
                    "No selection", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        EntryTreeNode node = (EntryTreeNode) path.getLastPathComponent();
        
        NodeTitleDialog dialog = new NodeTitleDialog(this, true);
        dialog.setNodeTitle(node.getUserObject().toString());
        dialog.setVisible(true);
        
        if (dialog.getModalResult()) {
            String newTitle = dialog.getNodeTitle();
            model.setNodeTitle(node, newTitle);
        }
    }
    
    /**
     * Handler for Delete Node
     */
    private void onDeleteNode(ActionEvent e) {
        TreePath path = navigationTree.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a node first", 
                    "No selection", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        EntryTreeNode node = (EntryTreeNode) path.getLastPathComponent();
        
        int result = JOptionPane.showConfirmDialog(this,
                "Delete this node and all children?",
                "Delete Node",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            model.deleteNode(node);
            contentEditor.setText("");
        }
    }
    
    /**
     * Handler for Cut
     */
    private void onCut(ActionEvent e) {
        contentEditor.cut();
    }
    
    /**
     * Handler for Copy
     */
    private void onCopy(ActionEvent e) {
        contentEditor.copy();
    }
    
    /**
     * Handler for Paste
     */
    private void onPaste(ActionEvent e) {
        contentEditor.paste();
    }
    
    /**
     * Handler for Set Password
     */
    private void onSetPassword(ActionEvent e) {
        EncryptPasswordDialog dialog = new EncryptPasswordDialog(this, true);
        dialog.setVisible(true);
        
        if (dialog.getModalResult()) {
            char[] password = dialog.getPassword();
            model.setPassword(new String(password));
            
            // Clear password from memory
            dialog.clearPasswords();
            Arrays.fill(password, '0');
            
            JOptionPane.showMessageDialog(this,
                    "Password set successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Handler for Password Generator
     */
    private void onPasswordGenerator(ActionEvent e) {
        PasswordGeneratorDialog dialog = new PasswordGeneratorDialog(this, true);
        dialog.setVisible(true);
    }
    
    /**
     * Handler for Text Encryption
     */
    private void onTextEncryption(ActionEvent e) {
        EncryptionDialog dialog = new EncryptionDialog();
        dialog.setVisible(true);
    }    
    
    /**
     * Handler for About
     */
    private void onAbout(ActionEvent e) {
        AboutDialog dialog = new AboutDialog(this, true);
        dialog.setVisible(true);
    }
    
    /**
     * Handler for window closing
     */
    private void onWindowClose() {
        int result = showSaveConfirmation();
        if (result != JOptionPane.CANCEL_OPTION) {
            dispose();
            System.exit(0);
        }
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Shows save confirmation dialog
     * 
     * @return JOptionPane result (YES_OPTION, NO_OPTION, CANCEL_OPTION)
     */
    private int showSaveConfirmation() {
        // Don't ask if no changes
        if (savedFileName.isEmpty() && contentEditor.getText().trim().isEmpty()) {
            return JOptionPane.NO_OPTION;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
                "Save the current file?",
                "Save Confirmation",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            saveFile(false);
        }
        
        return result;
    }
    
    /**
     * Creates a new file
     */
    private void createNewFile() {
        model.clearModel();
        contentEditor.setText("");
        saved = false;
        savedFileName = "";
        updateTitle("untitled." + Constants.FILE_EXTENSION);
    }
    
    /**
     * Shows open file dialog
     */
    private void showOpenFileDialog() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                Constants.APP_NAME + " files",
                Constants.FILE_EXTENSION,
                Constants.FILE_EXTENSION_OLD  // Backward compatibility
        );
        
        chooser.setFileFilter(filter);
        int result = chooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    /**
     * Opens a file
     * 
     * @param filename File path to open
     */
    public void openFile(String filename) {
        DecryptPasswordDialog dialog = new DecryptPasswordDialog(this, true);
        dialog.setVisible(true);
        
        if (dialog.getModalResult()) {
            char[] password = dialog.getPassword();
            model.setPassword(new String(password));
            
            try {
                model.loadFile(filename);
                saved = true;
                savedFileName = filename;
                contentEditor.setText("");
                updateTitle(new File(filename).getName());
                
                // Expand first level of tree
                for (int i = 0; i < navigationTree.getRowCount(); i++) {
                    navigationTree.expandRow(i);
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading file:\n" + ex.getMessage(),
                        "Load Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                // Clear password from memory
                Arrays.fill(password, '0');
            }
        }
    }
    
    /**
     * Saves the file
     * 
     * @param showDialog true to always show save dialog, false to save to current file if available
     */
    private void saveFile(boolean showDialog) {
        // Save current editor content
        TreePath currentPath = navigationTree.getLeadSelectionPath();
        if (currentPath != null) {
            try {
                EntryTreeNode node = (EntryTreeNode) currentPath.getLastPathComponent();
                model.setNodeContent(node, contentEditor.getText());
            } catch (Exception e) {
                // Ignore
            }
        }
        
        // Check if password is set
        if (model.getPassword() == null || model.getPassword().isEmpty()) {
            EncryptPasswordDialog pwDialog = new EncryptPasswordDialog(this, true);
            pwDialog.setVisible(true);
            
            if (pwDialog.getModalResult()) {
                char[] password = pwDialog.getPassword();
                model.setPassword(new String(password));
                
                // Clear password from memory
                pwDialog.clearPasswords();
                Arrays.fill(password, '0');
            } else {
                return; // User cancelled password dialog
            }
        }
        
        // Show save dialog if needed
        if (!saved || showDialog || savedFileName.isEmpty()) {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    Constants.APP_NAME + " files",
                    Constants.FILE_EXTENSION
            );
            
            chooser.setFileFilter(filter);
            
            if (!savedFileName.isEmpty()) {
                chooser.setSelectedFile(new File(savedFileName));
            }
            
            int result = chooser.showSaveDialog(this);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                String fileName = chooser.getSelectedFile().getAbsolutePath();
                
                // Add extension if missing
                if (!fileName.endsWith("." + Constants.FILE_EXTENSION)) {
                    fileName += "." + Constants.FILE_EXTENSION;
                }
                
                saveToFile(fileName);
            }
        } else {
            saveToFile(savedFileName);
        }
    }
    
    /**
     * Saves data to file
     * 
     * @param fileName File path
     */
    private void saveToFile(String fileName) {
        try {
            model.saveFile(fileName);
            saved = true;
            savedFileName = fileName;
            updateTitle(new File(fileName).getName());
        
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving file:\n" + ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Updates window title
     * 
     * @param fileName Current file name
     */
    private void updateTitle(String fileName) {
        setTitle(Constants.APP_NAME + " - " + fileName);
    }
    
    // ========== IObserver Implementation ==========
    
    /**
     * Called when the model changes
     */
    @Override
    public void refresh() {
        DefaultTreeModel treeModel = (DefaultTreeModel) navigationTree.getModel();
        treeModel.reload();
        navigationTree.repaint();
    }
}