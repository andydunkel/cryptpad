package de.dasoftware.cryptpad.crypto;

import de.dasoftware.cryptpad.Constants;
import de.dasoftware.cryptpad.i18n.Messages;

/**
 * Wrapper for encryption with message formatting
 * Adds headers and footers to encrypted content similar to PGP-style messages
 */
public class EncryptionWrapper {
    
    // Message format markers
    private static final String BEGIN_MESSAGE = "-----BEGIN ENCRYPTED MESSAGE-----";
    private static final String BEGIN_FILE = "-----BEGIN ENCRYPTED FILE-----";
    private static final String END_MESSAGE = "-----END ENCRYPTED MESSAGE-----";
    private static final String BEGIN_ENC = "-----BEGIN-----";
    private static final String VERSION_INFO = "Version: " + Constants.APP_NAME + " " + Constants.APP_VERSION;
    
    private final IEncryption encryption;
    
    /**
     * Constructor - initializes with AES encryption
     */
    public EncryptionWrapper() {
        this.encryption = new AESEncryption();
    }
    
    /**
     * Constructor with custom encryption implementation
     * 
     * @param encryption Custom encryption implementation
     */
    public EncryptionWrapper(IEncryption encryption) {
        this.encryption = encryption;
    }
    
    /**
     * Encrypts a message and wraps it with message headers
     * 
     * @param message Plain text message to encrypt
     * @param key Encryption key/passphrase
     * @return Formatted encrypted message with headers
     * @throws Exception If encryption fails
     */
    public String encryptMessage(String message, String key) throws Exception {
        return buildEncryptedOutput(BEGIN_MESSAGE, message, key);
    }
    
    /**
     * Encrypts a file content and wraps it with file headers
     * 
     * @param fileContent File content to encrypt
     * @param key Encryption key/passphrase
     * @return Formatted encrypted content with headers
     * @throws Exception If encryption fails
     */
    public String encryptFile(String fileContent, String key) throws Exception {
        return buildEncryptedOutput(BEGIN_FILE, fileContent, key);
    }
    
    /**
     * Decrypts a wrapped encrypted message
     * 
     * @param wrappedMessage Encrypted message with headers
     * @param key Decryption key/passphrase
     * @return Decrypted plain text message
     * @throws Exception If decryption fails or message format is invalid
     */
    public String decryptMessage(String wrappedMessage, String key) throws Exception {
        // Find encrypted content boundaries
        int startPos = wrappedMessage.indexOf(BEGIN_ENC);
        int endPos = wrappedMessage.indexOf(END_MESSAGE);
        
        if (startPos == -1 || endPos == -1) {
            throw new Exception(Messages.getString("encryption.wrapper.error.missingheaders"));
        }
        
        // Extract encrypted content (skip BEGIN marker and newline)
        startPos += BEGIN_ENC.length();
        String encryptedContent = wrappedMessage.substring(startPos, endPos);
        
        // Remove all whitespace (spaces, newlines, tabs)
        encryptedContent = encryptedContent.replaceAll("\\s+", "");
        
        if (encryptedContent.isEmpty()) {
            throw new Exception(Messages.getString("encryption.wrapper.error.nocontent"));
        }
        
        // Decrypt and return
        return encryption.decryptString(key, encryptedContent);
    }
    
    /**
     * Builds the formatted encrypted output with headers and footers
     * 
     * @param beginMarker Begin marker (MESSAGE or FILE)
     * @param content Content to encrypt
     * @param key Encryption key
     * @return Formatted encrypted output
     * @throws Exception If encryption fails
     */
    private String buildEncryptedOutput(String beginMarker, String content, String key) throws Exception {
        StringBuilder output = new StringBuilder();
        
        output.append(beginMarker).append("\n");
        output.append(VERSION_INFO).append("\n\n");
        output.append(BEGIN_ENC).append("\n");
        output.append(encryption.encryptString(key, content)).append("\n");
        output.append(END_MESSAGE);
        
        return output.toString();
    }
    
    /**
     * Gets the encryption implementation
     * 
     * @return Current encryption implementation
     */
    public IEncryption getEncryption() {
        return encryption;
    }
}