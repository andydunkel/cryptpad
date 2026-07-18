import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Standalone oracle used to cross-check the FreePascal crypto port against the JDK's
 * reference implementations. Not part of the shipped application.
 *
 * Usage:
 *   java CryptoOracle sha256 <hexInput>
 *   java CryptoOracle hmacsha256 <hexKey> <hexData>
 *   java CryptoOracle pbkdf2 <passwordUtf8> <hexSalt> <iterations> <dkLenBytes>
 *   java CryptoOracle gcmenc <hexKey32> <hexIV12> <hexPlaintext>
 *   java CryptoOracle gcmdec <hexKey32> <hexIV12> <hexCiphertextWithTag>
 */
public class CryptoOracle {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("missing command");
            System.exit(1);
        }
        switch (args[0]) {
            case "sha256": {
                byte[] data = hexToBytes(args[1]);
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                System.out.println(bytesToHex(md.digest(data)));
                break;
            }
            case "hmacsha256": {
                byte[] key = hexToBytes(args[1]);
                byte[] data = hexToBytes(args[2]);
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(key, "HmacSHA256"));
                System.out.println(bytesToHex(mac.doFinal(data)));
                break;
            }
            case "pbkdf2": {
                String password = args[1];
                byte[] salt = hexToBytes(args[2]);
                int iterations = Integer.parseInt(args[3]);
                int dkLenBytes = Integer.parseInt(args[4]);
                PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, dkLenBytes * 8);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                byte[] key = factory.generateSecret(spec).getEncoded();
                System.out.println(bytesToHex(key));
                break;
            }
            case "aesecb": {
                byte[] key = hexToBytes(args[1]);
                byte[] block = hexToBytes(args[2]);
                Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
                System.out.println(bytesToHex(cipher.doFinal(block)));
                break;
            }
            case "gcmenc": {
                byte[] key = hexToBytes(args[1]);
                byte[] iv = hexToBytes(args[2]);
                byte[] plaintext = hexToBytes(args[3]);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                GCMParameterSpec spec = new GCMParameterSpec(128, iv);
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), spec);
                System.out.println(bytesToHex(cipher.doFinal(plaintext)));
                break;
            }
            case "gcmdec": {
                byte[] key = hexToBytes(args[1]);
                byte[] iv = hexToBytes(args[2]);
                byte[] ct = hexToBytes(args[3]);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                GCMParameterSpec spec = new GCMParameterSpec(128, iv);
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), spec);
                System.out.println(bytesToHex(cipher.doFinal(ct)));
                break;
            }
            default:
                System.err.println("unknown command: " + args[0]);
                System.exit(1);
        }
    }

    private static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) return new byte[0];
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return out;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
