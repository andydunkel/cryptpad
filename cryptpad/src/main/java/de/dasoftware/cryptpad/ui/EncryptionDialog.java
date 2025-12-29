package de.dasoftware.cryptpad.ui;

import de.dasoftware.cryptpad.crypto.EncryptionWrapper;
import de.dasoftware.cryptpad.util.IconUtil;

import javax.crypto.BadPaddingException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Standalone text editor with encryption and decryption capabilities
 * Allows encrypting/decrypting text content and saving to plain text files
 */
public class EncryptionDialog extends JFrame {
    
    private boolean saved = false;
    private String savedFileName = "";
    
    // Components
    private JToolBar toolBar;
    private JButton newButton;
    private JButton openButton;
    private JButton saveButton;
    private JButton cutButton;
    private JButton copyButton;
    private JButton pasteButton;
    private JButton encryptButton;
    private JButton exitButton;
    
    private JScrollPane scrollPane;
    private JTextArea textArea;
    
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu encryptionMenu;
    
    /**
     * Constructor
     */
    public EncryptionDialog() {
        initComponents();
        setupLayout();
        setupListeners();
        setupMenuBar();
        IconUtil.setApplicationIcon(this);
        
        setSize(600, 450);
        setLocationRelativeTo(null);
    }
    
    /**
     * Initializes all components
     */
    private void initComponents() {
        setTitle("Text Encryption");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Toolbar buttons
        newButton = createToolbarButton("/icons/tb_new.png", "New");
        openButton = createToolbarButton("/icons/tb_open.png", "Open");
        saveButton = createToolbarButton("/icons/tb_save.png", "Save");
        cutButton = createToolbarButton("/icons/cut16.png", "Cut");
        copyButton = createToolbarButton("/icons/copy16.png", "Copy");
        pasteButton = createToolbarButton("/icons/paste16.png", "Paste");
        encryptButton = createToolbarButton("/icons/lock.png", "Encrypt");
        exitButton = createToolbarButton("/icons/exit.png", "Exit");
        
        // Text area
        textArea = new JTextArea();
        textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        scrollPane = new JScrollPane(textArea);
        
        // Toolbar
        toolBar = new JToolBar();
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.add(newButton);
        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.add(cutButton);
        toolBar.add(copyButton);
        toolBar.add(pasteButton);
        toolBar.addSeparator();
        toolBar.add(encryptButton);
        toolBar.addSeparator();
        toolBar.add(exitButton);
    }
    
    /**
     * Sets up the layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Sets up event listeners
     */
    private void setupListeners() {
        newButton.addActionListener(this::onNew);
        openButton.addActionListener(this::onOpen);
        saveButton.addActionListener(this::onSave);
        cutButton.addActionListener(e -> textArea.cut());
        copyButton.addActionListener(e -> textArea.copy());
        pasteButton.addActionListener(e -> textArea.paste());
        encryptButton.addActionListener(this::onEncrypt);
        exitButton.addActionListener(e -> dispose());
    }
    
    /**
     * Sets up the menu bar
     */
    private void setupMenuBar() {
        menuBar = new JMenuBar();
        
        // File menu
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        fileMenu.add(createMenuItem("New", 'N', "control N", "/icons/tb_new.png", this::onNew));
        fileMenu.add(createMenuItem("Open", 'O', "control O", "/icons/tb_open.png", this::onOpen));
        fileMenu.add(createMenuItem("Save", 'S', "control S", "/icons/tb_save.png", this::onSave));
        fileMenu.add(createMenuItem("Save As", 'A', null, null, this::onSaveAs));
        fileMenu.addSeparator();
        fileMenu.add(createMenuItem("Exit", 'E', "control F4", "/icons/exit.png", e -> dispose()));
        
        // Edit menu
        editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        
        editMenu.add(createMenuItem("Cut", 'C', "control X", "/icons/cut16.png", e -> textArea.cut()));
        editMenu.add(createMenuItem("Copy", 'O', "control C", "/icons/copy16.png", e -> textArea.copy()));
        editMenu.add(createMenuItem("Paste", 'P', "control V", "/icons/paste16.png", e -> textArea.paste()));
        
        // Encryption menu
        encryptionMenu = new JMenu("Encryption");
        encryptionMenu.setMnemonic('C');
        
        encryptionMenu.add(createMenuItem("Encrypt Text", 'E', null, null, this::onEncrypt));
        encryptionMenu.add(createMenuItem("Decrypt Text", 'D', null, null, this::onDecrypt));
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(encryptionMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Creates a toolbar button with icon
     */
    private JButton createToolbarButton(String iconPath, String tooltip) {
        JButton button = new JButton();
        
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            button.setIcon(icon);
        } catch (Exception e) {
            button.setText(tooltip);
        }
        
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        
        return button;
    }
    
    /**
     * Creates a menu item with optional icon and accelerator
     */
    private JMenuItem createMenuItem(String text, char mnemonic, String accelerator, 
                                     String iconPath, java.awt.event.ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.setMnemonic(mnemonic);
        
        if (accelerator != null) {
            item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
        }
        
        if (iconPath != null) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
                item.setIcon(icon);
            } catch (Exception e) {
                // Icon not found, continue without icon
            }
        }
        
        item.addActionListener(listener);
        return item;
    }
    
    /**
     * Handler for New button
     */
    private void onNew(ActionEvent e) {
        int result = showSaveConfirmation();
        
        if (result != JOptionPane.CANCEL_OPTION) {
            textArea.setText("");
            saved = false;
            savedFileName = "";
            updateTitle();
        }
    }
    
    /**
     * Handler for Open button
     */
    private void onOpen(ActionEvent e) {
        int result = showSaveConfirmation();
        
        if (result != JOptionPane.CANCEL_OPTION) {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Text files (*.txt, *.etf)", "txt", "etf");
            chooser.setFileFilter(filter);
            
            int returnVal = chooser.showOpenDialog(this);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                loadFile(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }
    
    /**
     * Handler for Save button
     */
    private void onSave(ActionEvent e) {
        saveFile(false);
    }
    
    /**
     * Handler for Save As menu item
     */
    private void onSaveAs(ActionEvent e) {
        saveFile(true);
    }
    
    /**
     * Handler for Encrypt button
     */
    private void onEncrypt(ActionEvent e) {
        encryptText();
    }
    
    /**
     * Handler for Decrypt menu item
     */
    private void onDecrypt(ActionEvent e) {
        decryptText();
    }
    
    /**
     * Shows save confirmation dialog
     * 
     * @return User's choice (YES, NO, or CANCEL)
     */
    private int showSaveConfirmation() {
        if (textArea.getText().isEmpty() || saved) {
            return JOptionPane.NO_OPTION;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
                "Do you want to save the current file?",
                "Save Confirmation",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            saveFile(false);
        }
        
        return result;
    }
    
    /**
     * Saves the file
     * 
     * @param showDialog true to always show the file chooser dialog
     */
    private void saveFile(boolean showDialog) {
        if (!saved || showDialog) {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
            chooser.setFileFilter(filter);
            
            if (!savedFileName.isEmpty()) {
                chooser.setSelectedFile(new File(savedFileName));
            }
            
            int returnVal = chooser.showSaveDialog(this);
            
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String filename = chooser.getSelectedFile().getAbsolutePath();
                
                // Add .txt extension if not present
                if (!filename.toLowerCase().endsWith(".txt")) {
                    filename += ".txt";
                }
                
                writeFile(filename);
            }
        } else {
            writeFile(savedFileName);
        }
    }
    
    /**
     * Writes content to file
     * 
     * @param filename File path to write to
     */
    private void writeFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filename, StandardCharsets.UTF_8))) {
            
            writer.write(textArea.getText());
            
            saved = true;
            savedFileName = filename;
            updateTitle();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not save the file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Loads content from file
     * 
     * @param filename File path to load from
     */
    private void loadFile(String filename) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filename)), 
                                       StandardCharsets.UTF_8);
            
            textArea.setText(content);
            saved = true;
            savedFileName = filename;
            updateTitle();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not load the file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Encrypts the current text
     */
    private void encryptText() {
        if (textArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter some text to encrypt.",
                    "No text",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        EncryptPasswordDialog dialog = new EncryptPasswordDialog(this, true);
        dialog.setVisible(true);
        
        if (dialog.getModalResult()) {
            char[] password = dialog.getPassword();
            
            try {
                EncryptionWrapper wrapper = new EncryptionWrapper();
                String encrypted = wrapper.encryptMessage(textArea.getText(), 
                                                         new String(password));
                textArea.setText(encrypted);
                saved = false;
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Encryption failed: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                // Clear password from memory
                dialog.clearPasswords();
                Arrays.fill(password, '0');
            }
        }
    }
    
    /**
     * Decrypts the current text
     */
    private void decryptText() {
        if (textArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter some text to decrypt.",
                    "No text",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        DecryptPasswordDialog dialog = new DecryptPasswordDialog(this, true);
        dialog.setVisible(true);
        
        if (dialog.getModalResult()) {
            char[] password = dialog.getPassword();
            
            try {
                EncryptionWrapper wrapper = new EncryptionWrapper();
                String decrypted = wrapper.decryptMessage(textArea.getText(), 
                                                         new String(password));
                textArea.setText(decrypted);
                saved = false;
                
            } catch (BadPaddingException ex) {
                JOptionPane.showMessageDialog(this,
                        "Decryption failed. Wrong password or corrupted data.",
                        "Decryption Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Decryption failed: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                // Clear password from memory
                Arrays.fill(password, '0');
            }
        }
    }
    
    /**
     * Updates the window title with filename
     */
    private void updateTitle() {
        String title = "Text Encryption";
        
        if (!savedFileName.isEmpty()) {
            File file = new File(savedFileName);
            title += " - " + file.getName();
        }
        
        if (!saved) {
            title += " *";
        }
        
        setTitle(title);
    }
    
    /**
     * Main method for standalone execution
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            EncryptionDialog dialog = new EncryptionDialog();
            dialog.setVisible(true);
        });
    }
}