package de.dasoftware.cryptpad.crypto;

/**
 * Interface for encryption implementations
 * 
 * @author DA-Software
 * @version 1.0.0
 */
public interface IEncryption {
    
    /**
     * Encrypts a plain text message with a passphrase
     * 
     * @param passphrase Password for encryption
     * @param message Plain text message to encrypt
     * @return Encrypted message (usually Base64 encoded)
     * @throws Exception If encryption fails
     */
    String encryptString(String passphrase, String message) throws Exception;
    
    /**
     * Decrypts an encrypted message with a passphrase
     * 
     * @param passphrase Password for decryption
     * @param encrypted Encrypted message
     * @return Decrypted plain text message
     * @throws Exception If decryption fails
     */
    String decryptString(String passphrase, String encrypted) throws Exception;
}