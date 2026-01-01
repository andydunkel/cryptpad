package de.dasoftware.cryptpad.ui;

import javax.swing.*;

import de.dasoftware.cryptpad.i18n.Messages;
import de.dasoftware.cryptpad.util.IconUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Dialog for entering decryption password
 */
public class DecryptPasswordDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;

    private boolean modalResult = false;
    
    // Components
    private JPanel mainPanel;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JButton okButton;
    private JButton cancelButton;
    
    /**
     * Constructor
     * 
     * @param parent Parent frame
     * @param modal Whether the dialog is modal
     */
    public DecryptPasswordDialog(Frame parent, boolean modal) {
        super(parent, modal);
        
        initComponents();
        setupLayout();
        setupListeners();
        IconUtil.setApplicationIcon(this);
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    /**
     * Initializes all components
     */
    private void initComponents() {
        setTitle(Messages.getString("password.decrypt.title"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Main panel with titled border
        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(
            Messages.getString("password.decrypt.border")));
        
        // Password label
        passwordLabel = new JLabel(Messages.getString("password.decrypt.label"));
        
        // Password field
        passwordField = new JPasswordField(20);
        
        // Buttons
        okButton = new JButton(Messages.getString("button.ok"));
        cancelButton = new JButton(Messages.getString("button.cancel"));
        
        // Make OK button default
        getRootPane().setDefaultButton(okButton);
    }
    
    /**
     * Sets up the layout
     */
    private void setupLayout() {
        // Main panel layout
        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        // Horizontal layout
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, 234, GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(okButton)
                    .addComponent(cancelButton))
        );
        
        // Vertical layout
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
        );
        
        // Add main panel to dialog
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Sets up event listeners
     */
    private void setupListeners() {
        okButton.addActionListener(this::onOk);
        cancelButton.addActionListener(this::onCancel);
        
        // Enter key in password field triggers OK
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onOk(null);
                }
            }
        });
        
        // Escape key cancels
        getRootPane().registerKeyboardAction(
            e -> onCancel(null),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    /**
     * Handler for OK button
     */
    private void onOk(ActionEvent e) {
        char[] password = passwordField.getPassword();
        
        if (password.length == 0) {
            JOptionPane.showMessageDialog(this,
                    Messages.getString("password.decrypt.empty"),
                    Messages.getString("password.decrypt.required"),
                    JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
        } else {
            modalResult = true;
            setVisible(false);
        }
    }
    
    /**
     * Handler for Cancel button
     */
    private void onCancel(ActionEvent e) {
        modalResult = false;
        setVisible(false);
    }
    
    /**
     * Gets the modal result (true if OK was clicked, false if Cancel)
     * 
     * @return Modal result
     */
    public boolean getModalResult() {
        return modalResult;
    }
    
    /**
     * Gets the entered password
     * 
     * @return Password as char array
     */
    public char[] getPassword() {
        return passwordField.getPassword();
    }
    
    /**
     * Gets the password field component
     * For backward compatibility with old code
     * 
     * @return Password field
     */
    @Deprecated
    public JPasswordField getEdtPassword() {
        return passwordField;
    }
}