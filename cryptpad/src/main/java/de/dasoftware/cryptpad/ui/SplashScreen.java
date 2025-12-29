package de.dasoftware.cryptpad.ui;

import de.dasoftware.cryptpad.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * Splash screen shown during application startup
 * Displays the application logo for a brief moment
 */
public class SplashScreen extends JWindow {
    
    private JLabel imageLabel;
    
    /**
     * Constructor
     * Creates an undecorated window with the splash image
     */
    public SplashScreen() {
        initComponents();
        setupLayout();
        centerOnScreen();
    }
    
    /**
     * Initializes all components
     */
    private void initComponents() {
        imageLabel = new JLabel();
        
        try {
            ImageIcon splashIcon = new ImageIcon(getClass().getResource("/icons/splash.png"));
            imageLabel.setIcon(splashIcon);
        } catch (Exception e) {
            // If image not found, show text instead
            imageLabel.setText(Constants.APP_NAME);
            imageLabel.setFont(new Font("Arial", Font.BOLD, 48));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setPreferredSize(new Dimension(400, 300));
            imageLabel.setOpaque(true);
            imageLabel.setBackground(Color.WHITE);
        }
        
        // Add border for visual separation
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    }
    
    /**
     * Sets up the layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(imageLabel, BorderLayout.CENTER);
        pack();
    }
    
    /**
     * Centers the window on the screen
     */
    private void centerOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        
        int x = (screenSize.width - windowSize.width) / 2;
        int y = (screenSize.height - windowSize.height) / 2;
        
        setLocation(x, y);
    }
    
    /**
     * Shows the splash screen for the specified duration
     * This method blocks until the duration has elapsed
     * 
     * @param millis Duration in milliseconds to display the splash screen
     */
    public void showFor(int millis) {
        setVisible(true);
        
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        setVisible(false);
        dispose();
    }
    
    /**
     * Shows the splash screen in a separate thread and returns immediately
     * Useful for non-blocking splash screens during startup
     * 
     * @param millis Duration in milliseconds to display the splash screen
     */
    public void showForAsync(int millis) {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            
            Timer timer = new Timer(millis, e -> {
                setVisible(false);
                dispose();
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
    
    /**
     * Closes the splash screen immediately
     */
    public void close() {
        setVisible(false);
        dispose();
    }
}