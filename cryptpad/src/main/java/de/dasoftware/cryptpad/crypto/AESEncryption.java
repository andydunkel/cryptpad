package de.dasoftware.cryptpad.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Modern AES-GCM encryption implementation with PBKDF2 key derivation
 * 
 * Security features:
 * - AES-256-GCM (authenticated encryption)
 * - PBKDF2 with SHA-256 for key derivation
 * - Random salt per encryption (prevents rainbow table attacks)
 * - Random IV/nonce per encryption (prevents pattern analysis)
 * - Authentication tag (prevents tampering)
 * 
 * Encrypted format: [version(1)][salt(16)][iv(12)][ciphertext][auth_tag(16)]
 * 
 * @author DA-Software
 * @version 2.0.0
 */
public class AESEncryption implements IEncryption {
    
    // Encryption constants
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128; // bits (16 bytes)
    private static final int GCM_IV_LENGTH = 12;   // bytes (96 bits recommended for GCM)
    
    // PBKDF2 constants
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH = 16;     // bytes
    private static final int PBKDF2_ITERATIONS = 100000; // OWASP recommendation
    
    // Format version for future compatibility
    private static final byte FORMAT_VERSION = 1;
    
    private final SecureRandom secureRandom;
    
    /**
     * Constructor
     */
    public AESEncryption() {
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Encrypts a message with a passphrase
     * 
     * @param passphrase Password for encryption
     * @param message Plain text message to encrypt
     * @return Base64 encoded encrypted message with salt, IV, and auth tag
     * @throws Exception If encryption fails
     */
    @Override
    public String encryptString(String passphrase, String message) throws Exception {
        // Generate random salt and IV
        byte[] salt = generateSalt();
        byte[] iv = generateIV();
        
        // Derive key from passphrase
        SecretKey key = deriveKey(passphrase, salt);
        
        // Encrypt
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        
        byte[] plaintextBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertextWithTag = cipher.doFinal(plaintextBytes);
        
        // Combine: version + salt + iv + ciphertext+tag
        ByteBuffer buffer = ByteBuffer.allocate(
            1 + SALT_LENGTH + GCM_IV_LENGTH + ciphertextWithTag.length
        );
        buffer.put(FORMAT_VERSION);
        buffer.put(salt);
        buffer.put(iv);
        buffer.put(ciphertextWithTag);
        
        // Encode to Base64
        return Base64.getEncoder().encodeToString(buffer.array());
    }
    
    /**
     * Decrypts an encrypted string with the given passphrase
     * 
     * @param passphrase Password for decryption
     * @param encrypted Base64 encoded encrypted message
     * @return Decrypted plain text message
     * @throws Exception If decryption fails or authentication fails
     */
    @Override
    public String decryptString(String passphrase, String encrypted) throws Exception {
        // Decode from Base64
        byte[] decoded = Base64.getDecoder().decode(encrypted);
        
        // Parse components
        ByteBuffer buffer = ByteBuffer.wrap(decoded);
        
        byte version = buffer.get();
        if (version != FORMAT_VERSION) {
            throw new IllegalArgumentException(
                "Unsupported format version: " + version
            );
        }
        
        byte[] salt = new byte[SALT_LENGTH];
        buffer.get(salt);
        
        byte[] iv = new byte[GCM_IV_LENGTH];
        buffer.get(iv);
        
        byte[] ciphertextWithTag = new byte[buffer.remaining()];
        buffer.get(ciphertextWithTag);
        
        // Derive key from passphrase
        SecretKey key = deriveKey(passphrase, salt);
        
        // Decrypt and verify authentication tag
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        
        byte[] plaintextBytes = cipher.doFinal(ciphertextWithTag);
        
        return new String(plaintextBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Derives a secret key from a passphrase using PBKDF2
     * 
     * @param passphrase Password to derive key from
     * @param salt Salt for key derivation
     * @return Derived secret key
     * @throws Exception If key derivation fails
     */
    private SecretKey deriveKey(String passphrase, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(
            passphrase.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_SIZE
        );
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
    
    /**
     * Generates a random salt
     * 
     * @return Random salt bytes
     */
    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }
    
    /**
     * Generates a random initialization vector
     * 
     * @return Random IV bytes
     */
    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }
}