package de.dasoftware.cryptpad.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Dialog for entering or editing a tree node title
 * 
 * @author DA-Software
 * @version 1.0.0
 */
public class NodeTitleDialog extends JDialog {
    
    private boolean modalResult = false;
    
    // Components
    private JLabel titleLabel;
    private JTextField titleField;
    private JButton okButton;
    private JButton cancelButton;
    
    /**
     * Constructor
     * 
     * @param parent Parent frame
     * @param modal Whether the dialog is modal
     */
    public NodeTitleDialog(Frame parent, boolean modal) {
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
        setTitle("Enter Node Title");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Label
        titleLabel = new JLabel("Title:");
        
        // Text field
        titleField = new JTextField(20);
        
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
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        // Horizontal layout
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(titleLabel)
                    .addComponent(titleField, GroupLayout.PREFERRED_SIZE, 216, GroupLayout.PREFERRED_SIZE))
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addComponent(okButton)
                    .addComponent(cancelButton))
        );
        
        // Vertical layout
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(titleField))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
        );
    }
    
    /**
     * Sets up event listeners
     */
    private void setupListeners() {
        okButton.addActionListener(this::onOk);
        cancelButton.addActionListener(this::onCancel);
        
        // Enter key in text field triggers OK
        titleField.addKeyListener(new KeyAdapter() {
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
        String title = titleField.getText().trim();
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a title.",
                    "Title required",
                    JOptionPane.WARNING_MESSAGE);
            titleField.requestFocusInWindow();
            return;
        }
        
        modalResult = true;
        setVisible(false);
    }
    
    /**
     * Handler for Cancel button
     */
    private void onCancel(ActionEvent e) {
        modalResult = false;
        setVisible(false);
    }
    
    /**
     * Shows the dialog and ensures the text field has focus
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        
        if (visible) {
            // Request focus after dialog is shown
            SwingUtilities.invokeLater(() -> {
                titleField.requestFocusInWindow();
                titleField.selectAll();
            });
        }
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
     * Gets the entered node title
     * 
     * @return Node title
     */
    public String getNodeTitle() {
        return titleField.getText().trim();
    }
    
    /**
     * Sets the node title (for editing existing nodes)
     * 
     * @param title Title to set
     */
    public void setNodeTitle(String title) {
        titleField.setText(title != null ? title : "");
    }
}