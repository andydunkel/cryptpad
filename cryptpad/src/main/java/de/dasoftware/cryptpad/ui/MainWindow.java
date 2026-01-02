package de.dasoftware.cryptpad.ui;

import de.dasoftware.cryptpad.Constants;
import de.dasoftware.cryptpad.model.EntryTreeNode;
import de.dasoftware.cryptpad.model.IDataModel;
import de.dasoftware.cryptpad.model.IObserver;
import de.dasoftware.cryptpad.settings.AppSettings;
import de.dasoftware.updater.UpdaterData;
import de.dasoftware.updater.ui.UpdaterDialog;
import de.dasoftware.cryptpad.i18n.Messages;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * Main window of DA-CryptPad application
 */
public class MainWindow extends JFrame implements IObserver {
    
    private static final long serialVersionUID = 1L;
	// Model
    private IDataModel model;
    private boolean saved = false;
    private boolean dirty = false;
    private String savedFileName = "";
    
    // Main components
    private JSplitPane splitPane;
    private JTree navigationTree;
    private JScrollPane treeScrollPane;
    private RSyntaxTextArea contentEditor; 
    private RTextScrollPane editorScrollPane;
    
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
    private JMenuItem menuItemSettings;
    
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
        
        updateTitle();
    }
    
    /**
     * Gets the themed icon path based on current theme setting
     * 
     * @param iconName Icon filename (e.g., "mainnode.png")
     * @return Full icon path with theme folder
     */
    private String getThemedIcon(String iconName) {
        String themeFolder = AppSettings.isDarkTheme() ? "dark" : "light";
        return "/icons/" + themeFolder + "/" + iconName;
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
        
        // Enable drag and drop
        navigationTree.setDragEnabled(true);
        navigationTree.setDropMode(DropMode.ON_OR_INSERT);
        navigationTree.setTransferHandler(new TreeTransferHandler(navigationTree, model));        
        
        // Tree icons
        try {
            ImageIcon leafIcon = new ImageIcon(getClass().getResource(getThemedIcon("leaf.png")));
            ImageIcon folderIcon = new ImageIcon(getClass().getResource(getThemedIcon("folder_closed16.png")));
            ImageIcon openIcon = new ImageIcon(getClass().getResource(getThemedIcon("folder_open16.png")));
            
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
        
        // Initialize editor with Markdown syntax highlighting
        contentEditor = new RSyntaxTextArea();
        contentEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
        contentEditor.setCodeFoldingEnabled(true);
        contentEditor.setAntiAliasingEnabled(true);
        contentEditor.setAutoIndentEnabled(true);
        contentEditor.setTabSize(4);
        contentEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Apply theme based on settings
        try {
            String themePath;
            if (AppSettings.isDarkTheme()) {
                themePath = "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml";
            } else {
                themePath = "/org/fife/ui/rsyntaxtextarea/themes/default.xml";
            }
            
            Theme theme = Theme.load(getClass().getResourceAsStream(themePath));
            theme.apply(contentEditor);
        } catch (Exception e) {
            System.err.println("Could not load RSyntaxTextArea theme: " + e.getMessage());
        }
        
        // Use RTextScrollPane for line numbers and code folding
        editorScrollPane = new RTextScrollPane(contentEditor);
        editorScrollPane.setLineNumbersEnabled(true);
        editorScrollPane.setFoldIndicatorEnabled(true);        
        
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
            ImageIcon icon16 = new ImageIcon(getClass().getResource(getThemedIcon("app-icon-16.png")));
            ImageIcon icon32 = new ImageIcon(getClass().getResource(getThemedIcon("app-icon-32.png")));
            ImageIcon icon48 = new ImageIcon(getClass().getResource(getThemedIcon("app-icon-48.png")));
            ImageIcon icon64 = new ImageIcon(getClass().getResource(getThemedIcon("app-icon-64.png")));
            
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
                ImageIcon icon = new ImageIcon(getClass().getResource(getThemedIcon("app-icon.png")));
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
        btnNew = createToolBarButton(getThemedIcon("tb_new.png"), 
            Messages.getString("tooltip.new"));
        btnOpen = createToolBarButton(getThemedIcon("tb_open.png"), 
            Messages.getString("tooltip.open"));
        btnSave = createToolBarButton(getThemedIcon("tb_save.png"), 
            Messages.getString("tooltip.save"));

        toolBar.add(btnNew);
        toolBar.add(btnOpen);
        toolBar.add(btnSave);
        toolBar.addSeparator();

        // Node operations
        btnNewMainNode = createToolBarButton(getThemedIcon("mainnode.png"), 
            Messages.getString("tooltip.newmainnode"));
        btnNewSibling = createToolBarButton(getThemedIcon("sibling.png"), 
            Messages.getString("tooltip.newsibling"));
        btnNewChild = createToolBarButton(getThemedIcon("childnode.png"), 
            Messages.getString("tooltip.newchild"));
        btnEditNode = createToolBarButton(getThemedIcon("edit.png"), 
            Messages.getString("tooltip.editnode"));
        btnDeleteNode = createToolBarButton(getThemedIcon("delete16.png"), 
            Messages.getString("tooltip.deletenode"));

        toolBar.add(btnNewMainNode);
        toolBar.add(btnNewSibling);
        toolBar.add(btnNewChild);
        toolBar.add(btnEditNode);
        toolBar.add(btnDeleteNode);
        toolBar.addSeparator();

        // Edit operations
        btnCut = createToolBarButton(getThemedIcon("cut16.png"), 
            Messages.getString("tooltip.cut"));
        btnCopy = createToolBarButton(getThemedIcon("copy16.png"), 
            Messages.getString("tooltip.copy"));
        btnPaste = createToolBarButton(getThemedIcon("paste16.png"), 
            Messages.getString("tooltip.paste"));

        toolBar.add(btnCut);
        toolBar.add(btnCopy);
        toolBar.add(btnPaste);
        toolBar.addSeparator();

        // Help
        btnAbout = createToolBarButton(getThemedIcon("Info.png"), 
            Messages.getString("tooltip.about", Constants.APP_NAME));
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
        menuFile = new JMenu(Messages.getString("menu.file"));
        menuFile.setMnemonic(Messages.getMnemonic("menu.file.mnemonic"));

        menuItemNew = createMenuItem(
            Messages.getString("menu.file.new"), 
            getThemedIcon("tb_new.png"), 
            Messages.getMnemonic("menu.file.new.mnemonic"),
            KeyStroke.getKeyStroke("control N")
        );

        menuItemOpen = createMenuItem(
            Messages.getString("menu.file.open"),
            getThemedIcon("tb_open.png"), 
            Messages.getMnemonic("menu.file.open.mnemonic"),
            KeyStroke.getKeyStroke("control O")
        );

        menuItemSave = createMenuItem(
            Messages.getString("menu.file.save"),
            getThemedIcon("tb_save.png"), 
            Messages.getMnemonic("menu.file.save.mnemonic"),
            KeyStroke.getKeyStroke("control S")
        );

        menuItemSaveAs = createMenuItem(
            Messages.getString("menu.file.saveas"),
            null, 
            Messages.getMnemonic("menu.file.saveas.mnemonic"),
            null
        );

        menuItemExit = createMenuItem(
            Messages.getString("menu.file.exit"),
            getThemedIcon("exit.png"), 
            Messages.getMnemonic("menu.file.exit.mnemonic"),
            KeyStroke.getKeyStroke("alt F4")
        );

        menuFile.add(menuItemNew);
        menuFile.add(menuItemOpen);
        menuFile.add(menuItemSave);
        menuFile.add(menuItemSaveAs);
        menuFile.addSeparator();
        menuFile.add(menuItemExit);
        
        // Edit menu
        menuEdit = new JMenu(Messages.getString("menu.edit"));
        menuEdit.setMnemonic(Messages.getMnemonic("menu.edit.mnemonic"));

        menuItemCut = createMenuItem(
            Messages.getString("menu.edit.cut"),
            getThemedIcon("cut16.png"), 
            Messages.getMnemonic("menu.edit.cut.mnemonic"),
            KeyStroke.getKeyStroke("control X")
        );

        menuItemCopy = createMenuItem(
            Messages.getString("menu.edit.copy"),
            getThemedIcon("copy16.png"), 
            Messages.getMnemonic("menu.edit.copy.mnemonic"),
            KeyStroke.getKeyStroke("control C")
        );

        menuItemPaste = createMenuItem(
            Messages.getString("menu.edit.paste"),
            getThemedIcon("paste16.png"), 
            Messages.getMnemonic("menu.edit.paste.mnemonic"),
            KeyStroke.getKeyStroke("control V")
        );

        menuItemDeleteNode = createMenuItem(
            Messages.getString("menu.edit.deletenode"),
            getThemedIcon("delete16.png"), 
            Messages.getMnemonic("menu.edit.deletenode.mnemonic"),
            null
        );
        
        menuItemSettings = createMenuItem(
            Messages.getString("menu.edit.settings"),
            null, 
            Messages.getMnemonic("menu.edit.settings.mnemonic"),
            KeyStroke.getKeyStroke("control COMMA")
        );

        menuEdit.add(menuItemCut);
        menuEdit.add(menuItemCopy);
        menuEdit.add(menuItemPaste);
        menuEdit.addSeparator();
        menuEdit.add(menuItemDeleteNode);
        menuEdit.addSeparator();
        menuEdit.add(menuItemSettings);
        
        // Encryption menu
        menuEncryption = new JMenu(Messages.getString("menu.encryption"));
        menuEncryption.setMnemonic(Messages.getMnemonic("menu.encryption.mnemonic"));

        menuItemSetPassword = createMenuItem(
            Messages.getString("menu.encryption.setpassword"),
            null, 
            Messages.getMnemonic("menu.encryption.setpassword.mnemonic"),
            null
        );

        menuItemPasswordGenerator = createMenuItem(
            Messages.getString("menu.encryption.passwordgen"),
            null, 
            Messages.getMnemonic("menu.encryption.passwordgen.mnemonic"),
            KeyStroke.getKeyStroke("control alt P")
        );

        menuItemTextEncryption = createMenuItem(
            Messages.getString("menu.encryption.textencryption"),
            null, 
            Messages.getMnemonic("menu.encryption.textencryption.mnemonic"),
            KeyStroke.getKeyStroke("control alt T")
        );

        menuEncryption.add(menuItemSetPassword);
        menuEncryption.addSeparator();
        menuEncryption.add(menuItemPasswordGenerator);
        menuEncryption.add(menuItemTextEncryption);
        
        // Help menu
        menuHelp = new JMenu(Messages.getString("menu.help"));
        menuHelp.setMnemonic(Messages.getMnemonic("menu.help.mnemonic"));

        JMenuItem menuItemCheckUpdates = createMenuItem(
            Messages.getString("menu.help.checkupdates"),
            null, 
            Messages.getMnemonic("menu.help.checkupdates.mnemonic"),
            null
        );
        menuItemCheckUpdates.addActionListener(this::onCheckUpdates);

        menuItemAbout = createMenuItem(
            Messages.getString("menu.help.about", Constants.APP_NAME),
            getThemedIcon("Info.png"), 
            Messages.getMnemonic("menu.help.about.mnemonic"),
            null
        );

        menuHelp.add(menuItemCheckUpdates);
        menuHelp.addSeparator();
        menuHelp.add(menuItemAbout);
               
        
        // Add menus to menu bar
        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuEncryption);
        menuBar.add(menuHelp);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Handler for Check Updates
     */
    private void onCheckUpdates(ActionEvent e) {    
        UpdaterData data = new UpdaterData();
        data.setUpdateUrl("https://da-software.net/versions/cryptpad.php");
        data.setVersionString(Constants.APP_VERSION);
        data.setAppTitle(Constants.APP_NAME);
        data.setUpdaterTitle(Constants.APP_NAME + " Updater");
        data.setAutoUpdate(false);
        data.setAutoClose(false);
        
        UpdaterDialog dialog = new UpdaterDialog(this, data);
        dialog.setVisible(true);
    }
    
    /**
     * Handler for Settings
     */
    private void onSettings(ActionEvent e) {
        SettingsDialog.showDialog(this);
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

        popupNewMainNode = createMenuItem(
            Messages.getString("popup.newmainnode"),
            getThemedIcon("mainnode.png"), 
            (char)0, 
            null
        );
        
        popupNewSibling = createMenuItem(
            Messages.getString("popup.newsibling"),
            getThemedIcon("sibling.png"), 
            (char)0, 
            null
        );
        
        popupNewChild = createMenuItem(
            Messages.getString("popup.newchild"),
            getThemedIcon("childnode.png"), 
            (char)0, 
            null
        );
        
        popupEditNode = createMenuItem(
            Messages.getString("popup.editnode"),
            getThemedIcon("edit.png"), 
            (char)0, 
            null
        );
        
        popupDeleteNode = createMenuItem(
            Messages.getString("popup.deletenode"),
            getThemedIcon("delete16.png"), 
            (char)0, 
            null
        );

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

        // Content editor changes - mark as dirty
        contentEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                markDirty();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                markDirty();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                markDirty();
            }
        });        
        
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
        menuItemSettings.addActionListener(this::onSettings);
        
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
     * Marks the file as dirty (unsaved changes)
     */
    public void markDirty() {
        if (!dirty) {
            dirty = true;
            updateTitle();
        }
    }  
    
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
            
            markDirty();
        }
    }
    
    /**
     * Handler for New Sibling Node
     */
    private void onNewSiblingNode(ActionEvent e) {
        TreePath path = navigationTree.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this,
                    Messages.getString("dialog.noselection.message"),
                    Messages.getString("dialog.noselection.title"),
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

            markDirty();
        }
    }
    
    /**
     * Handler for New Child Node
     */
    private void onNewChildNode(ActionEvent e) {
        TreePath path = navigationTree.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this,
                    Messages.getString("dialog.noselection.message"),
                    Messages.getString("dialog.noselection.title"),
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

            markDirty();
        }
    }
    
    /**
     * Handler for Edit Node
     */
    private void onEditNode(ActionEvent e) {
        TreePath path = navigationTree.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this,
                    Messages.getString("dialog.noselection.message"),
                    Messages.getString("dialog.noselection.title"),
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

            markDirty();
        }
    }
    
    /**
     * Handler for Delete Node
     */
    private void onDeleteNode(ActionEvent e) {
        TreePath path = navigationTree.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this,
                    Messages.getString("dialog.noselection.message"),
                    Messages.getString("dialog.noselection.title"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        EntryTreeNode node = (EntryTreeNode) path.getLastPathComponent();

        int result = JOptionPane.showConfirmDialog(this,
                Messages.getString("dialog.deletenode.message"),
                Messages.getString("dialog.deletenode.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            model.deleteNode(node);
            contentEditor.setText("");
            markDirty();
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
                    Messages.getString("dialog.success.password"),
                    Messages.getString("dialog.success.title"),
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
        // Auto-save if file is already saved with password
        if (dirty && saved && !savedFileName.isEmpty() &&
            model.getPassword() != null && !model.getPassword().isEmpty()) {

            // Save current editor content first
            TreePath currentPath = navigationTree.getLeadSelectionPath();
            if (currentPath != null) {
                try {
                    EntryTreeNode node = (EntryTreeNode) currentPath.getLastPathComponent();
                    model.setNodeContent(node, contentEditor.getText());
                } catch (Exception e) {
                    // Ignore
                }
            }

            // Auto-save
            try {
                model.saveFile(savedFileName);
                dispose();
                System.exit(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        Messages.getString("dialog.error.autosave", ex.getMessage()),
                        Messages.getString("dialog.error.title"),
                        JOptionPane.ERROR_MESSAGE);

                int result = JOptionPane.showConfirmDialog(this,
                        Messages.getString("dialog.confirmexit.message"),
                        Messages.getString("dialog.confirmexit.title"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        } else {
            // Show save confirmation
            int result = showSaveConfirmation();
            if (result != JOptionPane.CANCEL_OPTION) {
                dispose();
                System.exit(0);
            }
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
        if (!dirty) {
            return JOptionPane.NO_OPTION;
        }

        int result = JOptionPane.showConfirmDialog(this,
                Messages.getString("dialog.save.message"),
                Messages.getString("dialog.save.title"),
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
        dirty = false;
        savedFileName = "";
        updateTitle();
        
        // Select first node after creating new file
        selectFirstNode();
    }
    
    /**
     * Shows open file dialog
     */
    private void showOpenFileDialog() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                Constants.APP_NAME + " " + Messages.getString("dialog.open.files"),
                Constants.FILE_EXTENSION
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
                dirty = false;
                savedFileName = filename;
                contentEditor.setText("");
                updateTitle();

                // Expand first level of tree
                for (int i = 0; i < navigationTree.getRowCount(); i++) {
                    navigationTree.expandRow(i);
                }

                // Select first node after loading
                selectFirstNode();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        Messages.getString("dialog.error.load", ex.getMessage()),
                        Messages.getString("dialog.error.title"),
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                // Clear password from memory
                Arrays.fill(password, '0');
            }
        }
    }
    
    /**
     * Selects the first visible node in the tree
     * Should be called after creating new file or loading a file
     */
    private void selectFirstNode() {
        SwingUtilities.invokeLater(() -> {
            EntryTreeNode root = model.getRootNode();
            
            if (root != null && root.getChildCount() > 0) {
                // Select first child of root
                EntryTreeNode firstChild = (EntryTreeNode) root.getChildAt(0);
                TreePath path = new TreePath(model.getTreeModel().getPathToRoot(firstChild));
                
                navigationTree.setSelectionPath(path);
                navigationTree.scrollPathToVisible(path);
                
                // Load content into editor
                contentEditor.setText(firstChild.getContent());
                contentEditor.setCaretPosition(0);
            } else {
                // No nodes exist - clear editor
                contentEditor.setText("");
            }
        });
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
                    Constants.APP_NAME + " " + Messages.getString("dialog.open.files"),
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
            dirty = false;
            savedFileName = fileName;
            updateTitle();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    Messages.getString("dialog.error.save", ex.getMessage()),
                    Messages.getString("dialog.error.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Updates window title with filename and dirty indicator
     */
    private void updateTitle() {
        StringBuilder title = new StringBuilder(Constants.APP_NAME);

        if (!savedFileName.isEmpty()) {
            title.append(" - ").append(new File(savedFileName).getName());
        } else {
            title.append(" - ").append(Messages.getString("window.title.untitled"))
                 .append(".").append(Constants.FILE_EXTENSION);
        }

        if (dirty) {
            title.append(" *");
        }

        setTitle(title.toString());
    }
    
    /**
     * Overloaded method for backward compatibility
     * 
     * @param fileName Current file name
     */
    @Deprecated
    private void updateTitle(String fileName) {
        updateTitle();
    }
    
    // ========== IObserver Implementation ==========
    
    /**
     * Called when the model changes
     */
    @Override
    public void refresh() {
        // Null check in case refresh is called before initialization
        if (navigationTree == null || navigationTree.getModel() == null) {
            return;
        }
        
        // Save currently selected path
        TreePath selectedPath = navigationTree.getSelectionPath();
        
        DefaultTreeModel treeModel = (DefaultTreeModel) navigationTree.getModel();
        treeModel.reload();
        navigationTree.repaint();
        
        // Try to restore selection
        if (selectedPath != null) {
            navigationTree.setSelectionPath(selectedPath);
            navigationTree.scrollPathToVisible(selectedPath);
        } else {
            // No selection - select first node
            selectFirstNode();
        }
    }
}