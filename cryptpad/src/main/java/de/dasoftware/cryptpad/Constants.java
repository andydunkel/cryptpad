package de.dasoftware.cryptpad;

/**
 * Application constants
 * 
 * @author DA-Software
 * @version 1.0.0
 */
public final class Constants {
    
    // Application info
    public static final String APP_NAME = "DA-CryptPad";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_VENDOR = "DA-Software";
    
    // File handling
    public static final String FILE_EXTENSION = "cryptpad";
    public static final String FILE_EXTENSION_OLD = "jcryptpad";  // For backward compatibility
    
    // Line separators
    public static final String CRLF = "\r\n";  // Windows style
    public static final String LF = "\n";      // Unix/Mac style
    public static final String SYSTEM_LINE_SEPARATOR = System.lineSeparator();
    
    // UI Constants
    public static final int DEFAULT_WINDOW_WIDTH = 1100;
    public static final int DEFAULT_WINDOW_HEIGHT = 850;
    
    // Encryption
    public static final String ENCRYPTED_MESSAGE_MARKER = "-----BEGIN ENCRYPTED MESSAGE-----";
    public static final String ENCRYPTED_FILE_MARKER = "-----BEGIN ENCRYPTED FILE-----";
    
    /**
     * Private constructor to prevent instantiation
     */
    private Constants() {
        throw new AssertionError("Constants class cannot be instantiated");
    }
}