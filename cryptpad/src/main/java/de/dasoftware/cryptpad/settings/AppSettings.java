package de.dasoftware.cryptpad.settings;

import java.io.*;
import java.nio.file.*;
import java.util.Locale;
import java.util.Properties;

import de.dasoftware.cryptpad.Constants;

/**
 * Application settings manager
 * Stores settings in a properties file in user's home directory
 */
public class AppSettings {
    
    private static final String APP_NAME = Constants.APP_NAME;
    private static final String SETTINGS_FILENAME = "settings.properties";
    
    // Setting keys
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_THEME = "theme";
    
    // Default values
    private static final String DEFAULT_LANGUAGE = "system";
    private static final String DEFAULT_THEME = "System";
    
    private static Properties properties = new Properties();
    
    static {
        load();
    }
    
    /**
     * Gets the settings file path
     * Creates directory if it doesn't exist
     * 
     * @return Path to settings file
     */
    private static Path getSettingsPath() {
        String userHome = System.getProperty("user.home");
        
        // Use .config directory on Linux/Mac, AppData on Windows
        String configDir;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            configDir = System.getenv("APPDATA");
            if (configDir == null) {
                configDir = userHome;
            }
        } else {
            configDir = userHome + "/.config";
        }
        
        Path appDir = Paths.get(configDir, APP_NAME);
        
        try {
            Files.createDirectories(appDir);
        } catch (IOException e) {
            System.err.println("Error creating settings directory: " + e.getMessage());
        }
        
        return appDir.resolve(SETTINGS_FILENAME);
    }
    
    /**
     * Loads settings from file
     */
    public static void load() {
        Path settingsFile = getSettingsPath();
        
        if (Files.exists(settingsFile)) {
            try (InputStream input = Files.newInputStream(settingsFile)) {
                properties.load(input);
            } catch (IOException e) {
                System.err.println("Error loading settings: " + e.getMessage());
                initDefaults();
            }
        } else {
            initDefaults();
            save();
        }
    }
    
    /**
     * Saves settings to file
     */
    public static void save() {
        Path settingsFile = getSettingsPath();
        
        try (OutputStream output = Files.newOutputStream(settingsFile)) {
            properties.store(output, "DA-CryptPad Settings");
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }
    
    /**
     * Initializes default settings
     */
    private static void initDefaults() {
        properties.setProperty(KEY_LANGUAGE, DEFAULT_LANGUAGE);
        properties.setProperty(KEY_THEME, DEFAULT_THEME);
    }
    
    /**
     * Resets all settings to defaults
     */
    public static void resetToDefaults() {
        properties.clear();
        initDefaults();
        save();
    }
    
    // ========== Language Settings ==========
    
    /**
     * Gets the language setting
     * 
     * @return Language code ("en", "de", or "system")
     */
    public static String getLanguage() {
        return properties.getProperty(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }
    
    /**
     * Sets the language
     * 
     * @param language Language code ("en", "de", or "system")
     */
    public static void setLanguage(String language) {
        properties.setProperty(KEY_LANGUAGE, language);
        save();
    }
    
    /**
     * Gets the locale based on language setting
     * 
     * @return Locale object
     */
    public static Locale getLocale() {
        String lang = getLanguage();
        
        switch (lang) {
            case "en":
                return Locale.ENGLISH;
            case "de":
                return Locale.GERMAN;
            case "system":
            default:
                return Locale.getDefault();
        }
    }
    
    
    // ========== Theme Settings ==========
    
    /**
     * Gets theme setting
     * 
     * @return Theme name
     */
    public static String getTheme() {
        return properties.getProperty(KEY_THEME, DEFAULT_THEME);
    }
    
    /**
     * Sets theme
     * 
     * @param theme Theme name
     */
    public static void setTheme(String theme) {
        properties.setProperty(KEY_THEME, theme);
        save();
    }
    
    /**
     * Gets the settings file location
     * 
     * @return Path to settings file as string
     */
    public static String getSettingsFileLocation() {
        return getSettingsPath().toString();
    }
    
    /**
     * Checks if the current theme is a dark theme
     * 
     * @return true if dark theme is active
     */
    public static boolean isDarkTheme() {
        String theme = getTheme();
        return theme.contains("Dark") || theme.equals("FlatLaf Darcula");
    }
}