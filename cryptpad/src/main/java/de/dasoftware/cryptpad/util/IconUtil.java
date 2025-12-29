package de.dasoftware.cryptpad.util;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for icon handling
 */
public class IconUtil {
    
    /**
     * Sets the application icon for any window
     * 
     * @param window Window to set icon for
     */
    public static void setApplicationIcon(Window window) {
        try {
            ImageIcon icon = new ImageIcon(
                IconUtil.class.getResource("/icons/app-icon.png"));
            
            if (window instanceof Frame) {
                ((Frame) window).setIconImage(icon.getImage());
            } else if (window instanceof Dialog) {
                ((Dialog) window).setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            // Icon not found, ignore
        }
    }
}