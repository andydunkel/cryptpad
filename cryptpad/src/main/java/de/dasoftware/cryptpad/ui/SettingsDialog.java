package de.dasoftware.cryptpad.ui;

import de.dasoftware.cryptpad.i18n.Messages;
import de.dasoftware.cryptpad.settings.AppSettings;

import javax.swing.*;
import java.awt.*;

/**
 * Settings dialog for application preferences
 */
public class SettingsDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    // Components
    private JComboBox<String> languageComboBox;
    private JComboBox<String> themeComboBox;
    private JButton okButton;
    private JButton cancelButton;
    
    // Original values (for cancel)
    private String originalLanguage;
    /**
     * Constructor
     * 
     * @param parent Parent frame
     */
    public SettingsDialog(Frame parent) {
        super(parent, true); // Modal dialog
        
        // Store original values
        originalLanguage = AppSettings.getLanguage();
        AppSettings.getTheme();
        
        initComponents();
        setupLayout();
        setupListeners();
        loadCurrentSettings();
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    /**
     * Initializes all components
     */
    private void initComponents() {
        setTitle(Messages.getString("settings.title"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Language combo box
        String[] languages = {
            Messages.getString("settings.language.system"),
            Messages.getString("settings.language.english"),
            Messages.getString("settings.language.german")
        };
        languageComboBox = new JComboBox<>(languages);
        
        // Theme combo box (for future use)
        String[] themes = {
            "System",
            "FlatLaf Light",
            "FlatLaf Dark"
        };
        themeComboBox = new JComboBox<>(themes);
        themeComboBox.setEnabled(false); // Disabled for now
        
        // Buttons
        okButton = new JButton(Messages.getString("button.ok"));
        cancelButton = new JButton(Messages.getString("button.cancel"));
    }
    
    /**
     * Sets up the layout
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Center panel with settings
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        
        // Language label
        JLabel languageLabel = new JLabel(Messages.getString("settings.language") + ":");
        GridBagConstraints gbc_languageLabel = new GridBagConstraints();
        gbc_languageLabel.insets = new Insets(5, 5, 5, 5);
        gbc_languageLabel.anchor = GridBagConstraints.WEST;
        gbc_languageLabel.gridx = 0;
        gbc_languageLabel.gridy = 0;
        settingsPanel.add(languageLabel, gbc_languageLabel);
        
        // Language combo box
        GridBagConstraints gbc_languageComboBox = new GridBagConstraints();
        gbc_languageComboBox.insets = new Insets(5, 5, 5, 5);
        gbc_languageComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_languageComboBox.weightx = 1.0;
        gbc_languageComboBox.gridx = 1;
        gbc_languageComboBox.gridy = 0;
        settingsPanel.add(languageComboBox, gbc_languageComboBox);
        
        // Theme label
        JLabel themeLabel = new JLabel(Messages.getString("settings.theme") + ":");
        GridBagConstraints gbc_themeLabel = new GridBagConstraints();
        gbc_themeLabel.insets = new Insets(5, 5, 5, 5);
        gbc_themeLabel.anchor = GridBagConstraints.WEST;
        gbc_themeLabel.gridx = 0;
        gbc_themeLabel.gridy = 1;
        settingsPanel.add(themeLabel, gbc_themeLabel);
        
        // Theme combo box
        GridBagConstraints gbc_themeComboBox = new GridBagConstraints();
        gbc_themeComboBox.insets = new Insets(5, 5, 5, 5);
        gbc_themeComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_themeComboBox.weightx = 1.0;
        gbc_themeComboBox.gridx = 1;
        gbc_themeComboBox.gridy = 1;
        settingsPanel.add(themeComboBox, gbc_themeComboBox);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        
        // Add to main panel
        mainPanel.add(settingsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    /**
     * Sets up event listeners
     */
    private void setupListeners() {
        okButton.addActionListener(e -> onOk());
        cancelButton.addActionListener(e -> onCancel());
        
        // ESC key closes dialog
        getRootPane().registerKeyboardAction(
            e -> onCancel(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // Set OK as default button
        getRootPane().setDefaultButton(okButton);
    }
    
    /**
     * Loads current settings into UI
     */
    private void loadCurrentSettings() {
        // Load language
        String currentLang = AppSettings.getLanguage();
        switch (currentLang) {
            case "system":
                languageComboBox.setSelectedIndex(0);
                break;
            case "en":
                languageComboBox.setSelectedIndex(1);
                break;
            case "de":
                languageComboBox.setSelectedIndex(2);
                break;
        }
        
        // Load theme
        String currentTheme = AppSettings.getTheme();
        themeComboBox.setSelectedItem(currentTheme);
    }
    
    /**
     * Handles OK button click
     */
    private void onOk() {
        // Get selected language
        String newLanguage;
        switch (languageComboBox.getSelectedIndex()) {
            case 0:
                newLanguage = "system";
                break;
            case 1:
                newLanguage = "en";
                break;
            case 2:
                newLanguage = "de";
                break;
            default:
                newLanguage = "system";
        }
        
        // Get selected theme
        String newTheme = (String) themeComboBox.getSelectedItem();
        
        // Check if language changed
        boolean languageChanged = !newLanguage.equals(originalLanguage);
        
        // Save settings
        AppSettings.setLanguage(newLanguage);
        AppSettings.setTheme(newTheme);
        AppSettings.save();
        
        // Show restart message if language changed
        if (languageChanged) {
            JOptionPane.showMessageDialog(
                this,
                Messages.getString("settings.restart.message"),
                Messages.getString("settings.restart.title"),
                JOptionPane.INFORMATION_MESSAGE
            );
        }
        
        dispose();
    }
    
    /**
     * Handles Cancel button click
     */
    private void onCancel() {
        // Restore original values (don't save)
        dispose();
    }
    
    /**
     * Shows the settings dialog
     * 
     * @param parent Parent frame
     */
    public static void showDialog(Frame parent) {
        SettingsDialog dialog = new SettingsDialog(parent);
        dialog.setVisible(true);
    }
}