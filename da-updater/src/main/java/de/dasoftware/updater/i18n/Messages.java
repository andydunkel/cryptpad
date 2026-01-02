package de.dasoftware.updater.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Internationalization helper class
 * Provides access to localized messages
 */
public class Messages {
    
    private static final String BUNDLE_NAME = "i18n.UpdaterMessages";
    private static ResourceBundle resourceBundle;
    private static Locale currentLocale;
    
    /**
     * Sets the current locale
     *
     * @param locale Locale to use
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        
        // Special handling for English: use default properties without locale suffix
        if (locale.getLanguage().equals("en")) {
            resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ROOT);
        } else {
            resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
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
            return resourceBundle.getString(key);
        } catch (Exception e) {
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