package de.dasoftware.cryptpad.util;

import java.security.SecureRandom;

/**
 * PasswordGenerator
 * 
 * Generates random passwords with configurable character sets
 * 
 * @author DA-Software
 * @version 1.0.0
 */
public class PasswordGenerator {
    
    // Character sets for password generation
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER_CHARS = "0123456789";
    private static final String SPECIAL_CHARS = "!\"§$%&/()[]{}+*#'-.,><|";
    
    // Configuration flags
    private boolean specialCharsAllowed = false;
    private boolean numbersAllowed = false;
    private boolean capitalsAllowed = false;
    
    // Use SecureRandom for cryptographically strong random numbers
    private final SecureRandom random = new SecureRandom();
    
    /**
     * Generates a random password with the specified length
     * 
     * @param length Desired password length
     * @return Generated password
     */
    public String generatePassword(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Password length must be greater than 0");
        }
        
        // Build the character pool based on settings
        String characterPool = buildCharacterPool();
        
        if (characterPool.isEmpty()) {
            // Fallback: at least use lowercase if nothing is selected
            characterPool = LOWERCASE_CHARS;
        }
        
        // Generate password using StringBuilder for efficiency
        StringBuilder password = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characterPool.length());
            password.append(characterPool.charAt(randomIndex));
        }
        
        return password.toString();
    }
    
    /**
     * Builds the character pool based on current settings
     * 
     * @return String containing all allowed characters
     */
    private String buildCharacterPool() {
        StringBuilder pool = new StringBuilder();
        
        // Lowercase is always included
        pool.append(LOWERCASE_CHARS);
        
        // Add optional character sets
        if (capitalsAllowed) {
            pool.append(UPPERCASE_CHARS);
        }
        
        if (numbersAllowed) {
            pool.append(NUMBER_CHARS);
        }
        
        if (specialCharsAllowed) {
            pool.append(SPECIAL_CHARS);
        }
        
        return pool.toString();
    }
    
    // Getters and Setters
    
    public boolean isSpecialCharsAllowed() {
        return specialCharsAllowed;
    }
    
    public void setSpecialCharsAllowed(boolean specialCharsAllowed) {
        this.specialCharsAllowed = specialCharsAllowed;
    }
    
    public boolean isNumbersAllowed() {
        return numbersAllowed;
    }
    
    public void setNumbersAllowed(boolean numbersAllowed) {
        this.numbersAllowed = numbersAllowed;
    }
    
    public boolean isCapitalsAllowed() {
        return capitalsAllowed;
    }
    
    public void setCapitalsAllowed(boolean capitalsAllowed) {
        this.capitalsAllowed = capitalsAllowed;
    }
}