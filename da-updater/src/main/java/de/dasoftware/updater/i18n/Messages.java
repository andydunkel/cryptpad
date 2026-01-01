package de.dasoftware.updater.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Internationalization helper class
 * Provides access to localized messages
 */
public class Messages {

    private static final String BUNDLE_NAME = "i18n.UpdaterMessages";
    private static ResourceBundle resourceBundle;
    private static Locale currentLocale;

    static {
        // Initialize with system default locale
        System.out.println("=== Messages Debug Info ===");
        System.out.println("System Locale: " + Locale.getDefault());
        System.out.println("Bundle Name: " + BUNDLE_NAME);
        setLocale(Locale.getDefault());
    }

    /**
     * Sets the current locale
     *
     * @param locale Locale to use
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        System.out.println("Setting locale to: " + locale);
        
        try {
            resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            System.out.println("ResourceBundle loaded successfully!");
            System.out.println("Actual locale used: " + resourceBundle.getLocale());
            
            // Test: Print a sample key
            try {
                String test = resourceBundle.getString("updater.version.installed");
                System.out.println("Test key 'updater.version.installed': " + test);
            } catch (Exception e) {
                System.err.println("ERROR: Could not load test key: " + e.getMessage());
            }
            
        } catch (MissingResourceException e) {
            System.err.println("ERROR: ResourceBundle not found!");
            System.err.println("Bundle name: " + BUNDLE_NAME);
            System.err.println("Locale: " + locale);
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            resourceBundle = null;
        }
    }

    /**
     * Gets the current locale
     *
     * @return Current locale
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Gets a localized string
     *
     * @param key Message key
     * @return Localized string
     */
    public static String getString(String key) {
        try {
            if (resourceBundle == null) {
                System.err.println("ERROR: ResourceBundle is null for key: " + key);
                return "!" + key + "!";
            }
            String result = resourceBundle.getString(key);
            return result;
        } catch (MissingResourceException e) {
            System.err.println("ERROR: Key not found: " + key);
            System.err.println("Available keys: " + resourceBundle.keySet());
            return "!" + key + "!";
        } catch (Exception e) {
            System.err.println("ERROR getting key '" + key + "': " + e.getMessage());
            return "!" + key + "!";
        }
    }

    /**
     * Gets a localized string with parameters
     *
     * @param key Message key
     * @param params Parameters to insert into message
     * @return Formatted localized string
     */
    public static String getString(String key, Object... params) {
        try {
            String message = resourceBundle.getString(key);
            return MessageFormat.format(message, params);
        } catch (Exception e) {
            System.err.println("ERROR getting formatted key '" + key + "': " + e.getMessage());
            return "!" + key + "!";
        }
    }

    /**
     * Gets a mnemonic character
     *
     * @param key Message key for mnemonic
     * @return Mnemonic character, or '\0' if not found
     */
    public static char getMnemonic(String key) {
        try {
            String mnemonic = resourceBundle.getString(key);
            return mnemonic.isEmpty() ? '\0' : mnemonic.charAt(0);
        } catch (Exception e) {
            return '\0';
        }
    }

    /**
     * Gets available locales
     *
     * @return Array of available locales
     */
    public static Locale[] getAvailableLocales() {
        return new Locale[] {
            Locale.ENGLISH,
            Locale.GERMAN
        };
    }

    /**
     * Gets display name for a locale
     *
     * @param locale Locale
     * @return Display name
     */
    public static String getLocaleName(Locale locale) {
        return locale.getDisplayName(locale);
    }
}