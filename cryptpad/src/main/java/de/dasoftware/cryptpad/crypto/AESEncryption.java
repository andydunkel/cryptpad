package de.dasoftware.cryptpad.crypto;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

/**
 * AES Encryption implementation
 * 
 * WARNING: This implementation uses deprecated cryptographic practices:
 * - MD5 for password hashing (weak)
 * - ECB mode (not secure for most use cases)
 * - Deterministic key generation (seeded SecureRandom)
 * 
 * This class maintains backward compatibility with existing encrypted data.
 * For new projects, consider using AES-GCM with PBKDF2 key derivation.
 * 
 * @author DA-Software
 * @version 1.0.0
 */
public class AESEncryption implements IEncryption {
    
    private static final int KEY_SIZE = 128;
    private static final String HASH_ALGORITHM = "MD5";
    private static final String ENC_ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String SEC_RANDOM = "SHA1PRNG";
    
    /**
     * Encrypts a message with a passphrase
     * 
     * @param passphrase Password for encryption
     * @param message Plain text message to encrypt
     * @return Base64 encoded encrypted message
     * @throws Exception If encryption fails
     */
    @Override
    public String encryptString(String passphrase, String message) throws Exception {
        Key key = generateKey(passphrase);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        
        byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        
        return base64Encode(encryptedBytes);
    }
    
    /**
     * Decrypts an encrypted string with the given passphrase
     * 
     * @param passphrase Password for decryption
     * @param encrypted Base64 encoded encrypted message
     * @return Decrypted plain text message
     * @throws Exception If decryption fails
     */
    @Override
    public String decryptString(String passphrase, String encrypted) throws Exception {
        Key key = generateKey(passphrase);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);
        
        byte[] encryptedBytes = base64Decode(encrypted);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Generates an AES key from the passphrase
     * Uses MD5 hash and deterministic SecureRandom for backward compatibility
     * 
     * @param passphrase Password to derive key from
     * @return Generated AES key
     * @throws Exception If key generation fails
     */
    private Key generateKey(String passphrase) throws Exception {
        // Hash the passphrase with MD5
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        md.update(passphrase.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        String digestString = base64Encode(digest);
        
        // Generate key using seeded SecureRandom (deterministic for same passphrase)
        SecureRandom secureRandom = SecureRandom.getInstance(SEC_RANDOM);
        secureRandom.setSeed(digestString.getBytes(StandardCharsets.UTF_8));
        
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENC_ALGORITHM);
        keyGenerator.init(KEY_SIZE, secureRandom);
        
        return keyGenerator.generateKey();
    }
    
    /**
     * Encodes bytes to Base64 string
     * 
     * @param data Bytes to encode
     * @return Base64 encoded string
     */
    private String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
    
    /**
     * Decodes Base64 string to bytes
     * 
     * @param encoded Base64 encoded string
     * @return Decoded bytes
     */
    private byte[] base64Decode(String encoded) {
        return Base64.getDecoder().decode(encoded);
    }
}