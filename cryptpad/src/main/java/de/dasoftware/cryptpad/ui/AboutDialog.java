package de.dasoftware.cryptpad.ui;

import de.dasoftware.cryptpad.Constants;
import de.dasoftware.cryptpad.util.*;

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
    
    private static final long serialVersionUID = 1L;
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
        IconUtil.setApplicationIcon(this);
        
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
        
        Font baseFont = UIManager.getFont("Label.font");
        
        // Logo/Image
        logoLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/splash.png"));
            logoLabel.setIcon(icon);
        } catch (Exception e) {
            // If image not found, display text instead
            logoLabel.setText(Constants.APP_NAME);
            logoLabel.setFont(baseFont.deriveFont(Font.BOLD, baseFont.getSize2D() + 10f));
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        // Version label
        versionLabel = new JLabel("Version " + Constants.APP_VERSION);
        versionLabel.setFont(baseFont.deriveFont(Font.PLAIN, baseFont.getSize2D()));
        
        // Author / Link label
        authorLabel = new JLabel("<html><a href=''>© DA-Software.net</a></html>");
        authorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        authorLabel.setFont(baseFont.deriveFont(Font.PLAIN, baseFont.getSize2D()));
        authorLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        authorLabel.setToolTipText("http://da-software.net");                
        
        // OK button
        okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(100, 25));
    }
    
    /**
     * Sets up the layout
     */
    private void setupLayout() {
        getContentPane().setLayout(new BorderLayout(10, 10));
        
        // Center panel with logo and labels
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
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
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        buttonPanel.add(okButton);
        
        // Add panels to dialog
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);        
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
        
        authorLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openWebsite("https://da-software.net");
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                authorLabel.setText("<html><a href=''>© <u>DA-Software.net</u></a></html>");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                authorLabel.setText("<html><a href=''>© DA-Software.net</a></html>");
            }
        });
    }
    
    private void openWebsite(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new java.net.URI(url));
            } 
        } catch (Exception ex) {
            //
        }
    }

    
    /**
     * Handler for OK button click
     */
    private void onOkClicked(ActionEvent e) {
        dispose();
    }
}