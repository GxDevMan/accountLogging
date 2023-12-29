package model;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class encryptdecryptHandler {
    private static final String AES_ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;

    // Generate a new AES key
    public SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        return keyGenerator.generateKey();
    }

    // Convert a key to Base64 text
    public String keyToBase64Text(Key key) {
        byte[] keyBytes = key.getEncoded();
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    // Save the key in text format
    public void saveKeyTextToFile(String keyText, String filename) throws Exception {
        byte[] keyBytes = keyText.getBytes(StandardCharsets.UTF_8);
        Path filePath = Paths.get(filename);
        Files.write(filePath, keyBytes);
    }

    // Save the key to a file
    public void saveKeyToFile(Key key, String filename) throws Exception {
        byte[] keyBytes = key.getEncoded();

        Path filePath = Paths.get(filename);
        Files.write(filePath, keyBytes);
    }

    // Convert a string representation of a key to a SecretKey object
    public SecretKey convertStringToSecretKey(String keyString) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, "AES");
    }

    // Load the key from a file
    public SecretKey loadKeyFromFile(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }

    // Encrypt a message using the specified key
    public String encrypt(String message, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypt a message using the specified key
    public String decrypt(String encryptedMessage, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}

