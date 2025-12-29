package de.dasoftware.cryptpad.ui;

import de.dasoftware.cryptpad.util.IconUtil;
import de.dasoftware.cryptpad.util.PasswordGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

/**
 * Dialog for generating random passwords with configurable options
 */
public class PasswordGeneratorDialog extends JDialog {
    
    // Default values
    private static final int DEFAULT_PASSWORD_LENGTH = 10;
    private static final int DEFAULT_PASSWORD_COUNT = 5;
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MIN_PASSWORD_COUNT = 1;
    private static final int MAX_PASSWORD_COUNT = 100;
    
    // Components
    private JLabel lengthLabel;
    private JLabel countLabel;
    private JTextField lengthField;
    private JTextField countField;
    private JCheckBox specialCharsCheckBox;
    private JCheckBox numbersCheckBox;
    private JCheckBox capitalsCheckBox;
    private JButton generateButton;
    private JPanel passwordsPanel;
    private JScrollPane scrollPane;
    private JList<String> passwordsList;
    private DefaultListModel<String> listModel;
    
    // Popup menu
    private JPopupMenu popupMenu;
    private JMenuItem copyMenuItem;
    
    /**
     * Constructor
     * 
     * @param parent Parent frame
     * @param modal Whether the dialog is modal
     */
    public PasswordGeneratorDialog(Frame parent, boolean modal) {
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
        setTitle("Password Generator");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Labels
        lengthLabel = new JLabel("Password length:");
        countLabel = new JLabel("How many passwords?");
        
        // Text fields
        lengthField = new JTextField(String.valueOf(DEFAULT_PASSWORD_LENGTH), 10);
        countField = new JTextField(String.valueOf(DEFAULT_PASSWORD_COUNT), 10);
        
        // Checkboxes
        specialCharsCheckBox = new JCheckBox("Include special characters");
        numbersCheckBox = new JCheckBox("Include numbers");
        capitalsCheckBox = new JCheckBox("Include capitals");
        
        // Generate button
        generateButton = new JButton("Generate Passwords");
        
        // List with model
        listModel = new DefaultListModel<>();
        passwordsList = new JList<>(listModel);
        passwordsList.setFont(new Font("Courier New", Font.PLAIN, 11));
        passwordsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        scrollPane = new JScrollPane(passwordsList);
        scrollPane.setPreferredSize(new Dimension(363, 140));
        
        // Panel for passwords list
        passwordsPanel = new JPanel(new BorderLayout());
        passwordsPanel.setBorder(BorderFactory.createTitledBorder("Passwords"));
        passwordsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Popup menu
        popupMenu = new JPopupMenu();
        copyMenuItem = new JMenuItem("Copy");
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/copy16.png"));
            copyMenuItem.setIcon(icon);
        } catch (Exception e) {
            // Icon not found, continue without icon
        }
        popupMenu.add(copyMenuItem);
        
        passwordsList.setComponentPopupMenu(popupMenu);
    }
    
    /**
     * Sets up the layout
     */
    private void setupLayout() {
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        // Horizontal layout
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lengthLabel)
                        .addComponent(countLabel))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(lengthField, GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                        .addComponent(countField))
                    .addGap(18)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(specialCharsCheckBox)
                        .addComponent(numbersCheckBox)
                        .addComponent(capitalsCheckBox)))
                .addComponent(generateButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(passwordsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        
        // Vertical layout
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lengthLabel)
                    .addComponent(lengthField)
                    .addComponent(specialCharsCheckBox))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(countLabel)
                    .addComponent(countField)
                    .addComponent(numbersCheckBox))
                .addComponent(capitalsCheckBox)
                .addComponent(generateButton)
                .addComponent(passwordsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
    }
    
    /**
     * Sets up event listeners
     */
    private void setupListeners() {
        generateButton.addActionListener(this::onGenerate);
        copyMenuItem.addActionListener(this::onCopy);
        
        // Double-click on password to copy
        passwordsList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onCopy(null);
                }
            }
        });
        
        // Escape key closes dialog
        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }
    
    /**
     * Handler for Generate button
     */
    private void onGenerate(ActionEvent e) {
        try {
            // Parse and validate length
            int length = parseIntField(lengthField, "Password length");
            if (length < MIN_PASSWORD_LENGTH || length > MAX_PASSWORD_LENGTH) {
                JOptionPane.showMessageDialog(this,
                        String.format("Password length must be between %d and %d characters.",
                                MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH),
                        "Invalid length",
                        JOptionPane.WARNING_MESSAGE);
                lengthField.requestFocus();
                return;
            }
            
            // Parse and validate count
            int count = parseIntField(countField, "Password count");
            if (count < MIN_PASSWORD_COUNT || count > MAX_PASSWORD_COUNT) {
                JOptionPane.showMessageDialog(this,
                        String.format("Password count must be between %d and %d.",
                                MIN_PASSWORD_COUNT, MAX_PASSWORD_COUNT),
                        "Invalid count",
                        JOptionPane.WARNING_MESSAGE);
                countField.requestFocus();
                return;
            }
            
            // Configure password generator
            PasswordGenerator generator = new PasswordGenerator();
            generator.setSpecialCharsAllowed(specialCharsCheckBox.isSelected());
            generator.setNumbersAllowed(numbersCheckBox.isSelected());
            generator.setCapitalsAllowed(capitalsCheckBox.isSelected());
            
            // Clear previous passwords
            listModel.clear();
            
            // Generate passwords
            for (int i = 0; i < count; i++) {
                String password = generator.generatePassword(length);
                listModel.addElement(password);
            }
            
            // Select first password
            if (listModel.getSize() > 0) {
                passwordsList.setSelectedIndex(0);
            }
            
        } catch (NumberFormatException ex) {
            // Error message already shown by parseIntField
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error generating passwords: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handler for Copy menu item
     */
    private void onCopy(ActionEvent e) {
        String selectedPassword = passwordsList.getSelectedValue();
        
        if (selectedPassword != null) {
            // Copy to clipboard
            StringSelection selection = new StringSelection(selectedPassword);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            
            // Optional: Show feedback
            JOptionPane.showMessageDialog(this,
                    "Password copied to clipboard!",
                    "Copied",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a password first.",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Parses an integer from a text field with error handling
     * 
     * @param field Text field to parse
     * @param fieldName Name of field for error message
     * @return Parsed integer value
     * @throws NumberFormatException If parsing fails
     */
    private int parseIntField(JTextField field, String fieldName) throws NumberFormatException {
        try {
            String text = field.getText().trim();
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    fieldName + " must be a valid number.",
                    "Invalid input",
                    JOptionPane.WARNING_MESSAGE);
            field.requestFocus();
            field.selectAll();
            throw ex;
        }
    }
}