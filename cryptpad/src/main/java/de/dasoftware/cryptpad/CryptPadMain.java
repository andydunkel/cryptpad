package de.dasoftware.cryptpad;

import com.formdev.flatlaf.FlatLightLaf;
import de.dasoftware.cryptpad.ui.MainWindow;
import de.dasoftware.cryptpad.model.DataModel;

import javax.swing.*;

/**
 * Main class for DA-CryptPad
 * Starts and initializes the application
 * 
 * @author DA-Software
 * @version 1.0.0
 */
public class CryptPadMain {
    
    /**
     * Application entry point
     * 
     * @param args Command line arguments (optional: file to open)
     */
    public static void main(String[] args) {
        // Set FlatLaf Look and Feel (before creating GUI components!)
        initializeLookAndFeel();
        
        // Start GUI in Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            startApplication(args);
        });
    }
    
    /**
     * Initializes the Look and Feel
     */
    private static void initializeLookAndFeel() {
        try {
            // FlatLaf Light Theme
            FlatLightLaf.setup();
            
            // Alternative Themes (commented out):
            // FlatDarkLaf.setup();                    // Dark Theme
            // FlatIntelliJLaf.setup();                // IntelliJ Theme
            // FlatDarculaLaf.setup();                 // Dracula Theme
            
            // FlatLaf settings for rounded corners
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf: " + ex.getMessage());
            ex.printStackTrace();
            
            // Fallback to System Look and Feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Starts the main application
     * 
     * @param args Command line arguments
     */
    private static void startApplication(String[] args) {
        // Create data model
        DataModel dataModel = new DataModel();
        
        // Create main window
        MainWindow mainWindow = new MainWindow(dataModel);
        
        // If command line argument present: open file
        if (args.length > 0) {
            String filePath = args[0];
            mainWindow.openFile(filePath);
        }
        
        // Show window
        mainWindow.setLocationByPlatform(true);
        mainWindow.setVisible(true);
    }
}