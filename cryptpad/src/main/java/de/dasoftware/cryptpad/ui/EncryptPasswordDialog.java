package de.dasoftware.cryptpad.ui;

import javax.swing.*;

import de.dasoftware.cryptpad.util.IconUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * Dialog for setting/changing the encryption password
 * Requires password confirmation for security
 */
public class EncryptPasswordDialog extends JDialog {
    
    private boolean modalResult = false;
    
    // Components
    private JPanel mainPanel;
    private JLabel passwordLabel;
    private JLabel retypeLabel;
    private JPasswordField passwordField1;
    private JPasswordField passwordField2;
    private JButton okButton;
    private JButton cancelButton;
    
    /**
     * Constructor
     * 
     * @param parent Parent frame
     * @param modal Whether the dialog is modal
     */
    public EncryptPasswordDialog(Frame parent, boolean modal) {
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
        setTitle("Enter password for encryption");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Main panel with titled border
        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder("Set Password"));
        
        // Labels
        passwordLabel = new JLabel("Password:");
        retypeLabel = new JLabel("Retype Password:");
        
        // Password fields
        passwordField1 = new JPasswordField(20);
        passwordField2 = new JPasswordField(20);
        
        // Buttons
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        
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
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(passwordLabel)
                        .addComponent(retypeLabel))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(passwordField1, GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                        .addComponent(passwordField2)))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(okButton)
                    .addComponent(cancelButton))
        );
        
        // Vertical layout
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField1))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(retypeLabel)
                    .addComponent(passwordField2))
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
        
        // Enter key in second password field triggers OK
        passwordField2.addKeyListener(new KeyAdapter() {
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
     * Validates that passwords match and are not empty
     */
    private void onOk(ActionEvent e) {
        char[] password1 = passwordField1.getPassword();
        char[] password2 = passwordField2.getPassword();
        
        try {
            // Check if password is empty
            if (password1.length == 0) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a password.",
                        "Password required",
                        JOptionPane.WARNING_MESSAGE);
                passwordField1.requestFocusInWindow();
                return;
            }
            
            // Check if passwords match
            if (!Arrays.equals(password1, password2)) {
                JOptionPane.showMessageDialog(this,
                        "Passwords do not match.",
                        "Password mismatch",
                        JOptionPane.WARNING_MESSAGE);
                
                // Clear fields and focus first field
                passwordField1.setText("");
                passwordField2.setText("");
                passwordField1.requestFocusInWindow();
                return;
            }
            
            // All validation passed
            modalResult = true;
            setVisible(false);
            
        } finally {
            // Clear password2 from memory for security
            Arrays.fill(password2, '0');
        }
    }
    
    /**
     * Handler for Cancel button
     */
    private void onCancel(ActionEvent e) {
        modalResult = false;
        
        // Clear passwords from memory
        Arrays.fill(passwordField1.getPassword(), '0');
        Arrays.fill(passwordField2.getPassword(), '0');
        
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
        return passwordField1.getPassword();
    }
    
    /**
     * Gets the first password field component
     * For backward compatibility with old code
     * 
     * @return Password field
     */
    @Deprecated
    public JPasswordField getEdtPw1() {
        return passwordField1;
    }
    
    /**
     * Clears all password fields
     * Should be called after retrieving the password
     */
    public void clearPasswords() {
        Arrays.fill(passwordField1.getPassword(), '0');
        Arrays.fill(passwordField2.getPassword(), '0');
        passwordField1.setText("");
        passwordField2.setText("");
    }
}