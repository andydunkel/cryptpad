package de.dasoftware.cryptpad.ui;

import de.dasoftware.cryptpad.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * About dialog showing application information
 * 
 * @author DA-Software
 * @version 1.0.0
 */
public class AboutDialog extends JDialog {
    
    // Components
    private JLabel logoLabel;
    private JLabel versionLabel;
    private JLabel authorLabel;
    private JButton okButton;
    
    /**
     * Constructor
     * 
     * @param parent Parent frame
     * @param modal Whether the dialog is modal
     */
    public AboutDialog(Frame parent, boolean modal) {
        super(parent, modal);
        
        initComponents();
        setupLayout();
        setupListeners();
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    /**
     * Initializes all components
     */
    private void initComponents() {
        setTitle("About " + Constants.APP_NAME);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Logo/Image
        logoLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/splash.png"));
            logoLabel.setIcon(icon);
        } catch (Exception e) {
            // If image not found, display text instead
            logoLabel.setText(Constants.APP_NAME);
            logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        // Version label
        versionLabel = new JLabel("Version " + Constants.APP_VERSION);
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Author label
        authorLabel = new JLabel("© 2024 " + Constants.APP_VENDOR);
        authorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // OK button
        okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(100, 25));
    }
    
    /**
     * Sets up the layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Center panel with logo and labels
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerPanel.setBackground(Color.WHITE);
        
        // Add components with alignment
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        centerPanel.add(logoLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(versionLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(authorLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        buttonPanel.add(okButton);
        
        // Add panels to dialog
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set background
        getContentPane().setBackground(Color.WHITE);
    }
    
    /**
     * Sets up event listeners
     */
    private void setupListeners() {
        okButton.addActionListener(this::onOkClicked);
        
        // Also close on Escape key
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    /**
     * Handler for OK button click
     */
    private void onOkClicked(ActionEvent e) {
        dispose();
    }
}