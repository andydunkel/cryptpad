package de.dasoftware.cryptpad;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import de.dasoftware.cryptpad.ui.MainWindow;
import de.dasoftware.cryptpad.ui.SplashScreen;
import de.dasoftware.cryptpad.i18n.Messages;
import de.dasoftware.cryptpad.model.DataModel;
import de.dasoftware.cryptpad.settings.AppSettings;

import java.util.Locale;

import javax.swing.*;

/**
 * Main class for DA-CryptPad
 * Starts and initializes the application
 */
public class CryptPadMain {
    
    /**
     * Application entry point
     * 
     * @param args Command line arguments (optional: file to open)
     */
    public static void main(String[] args) {
        // Load settings first
        AppSettings.load();
        
        // Apply language setting
        Locale locale = AppSettings.getLocale();
        Messages.setLocale(locale);
        
        // Also set for updater
        de.dasoftware.updater.i18n.Messages.setLocale(locale);
    	    	
        // Show splash screen
        SplashScreen splash = new SplashScreen();
        splash.setVisible(true);    	
    	
        // Set FlatLaf Look and Feel (before creating GUI components!)
        initializeLookAndFeel();
        
        // Give splash screen time to display
        try {
            Thread.sleep(1500); // Show for 1.5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }        
        
        // Start GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            splash.close(); // Close splash before showing main window
            startApplication(args);
        });
    }
    
    /**
     * Initializes the Look and Feel based on settings
     */
    private static void initializeLookAndFeel() {
        String theme = AppSettings.getTheme();
        
        try {
            switch (theme) {
                case "System":
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    break;
                    
                case "FlatLaf Light":
                    FlatLightLaf.setup();
                    applyFlatLafCustomizations();
                    break;
                    
                case "FlatLaf Dark":
                    FlatDarkLaf.setup();
                    applyFlatLafCustomizations();
                    break;
                    
                case "FlatLaf IntelliJ":
                    FlatIntelliJLaf.setup();
                    applyFlatLafCustomizations();
                    break;
                    
                case "FlatLaf Darcula":
                    FlatDarculaLaf.setup();
                    applyFlatLafCustomizations();
                    break;
                    
                case "Nimbus":
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                    break;
                    
                case "Metal":
                    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                    break;
                    
                default:
                    // Fallback to FlatLaf Light
                    FlatLightLaf.setup();
                    applyFlatLafCustomizations();
            }
            
        } catch (Exception ex) {
            System.err.println("Failed to initialize Look and Feel: " + ex.getMessage());
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
     * Applies FlatLaf customizations (rounded corners, etc.)
     */
    private static void applyFlatLafCustomizations() {
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
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