import de.dasoftware.cryptpad.crypto.EncryptionWrapper;
import de.dasoftware.cryptpad.crypto.AESEncryption;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Uses the real, shipped AESEncryption/EncryptionWrapper classes to prove
 * interoperability with the FreePascal crypto port. Not part of the shipped app.
 *
 * Usage:
 *   java -cp .;<target/classes> InteropTest encrypt-message <password> <plaintextFile> <outFile>
 *   java -cp .;<target/classes> InteropTest decrypt-message <password> <inFile>
 *   java -cp .;<target/classes> InteropTest encrypt-raw <password> <plaintextFile> <outFile>   (AESEncryption only, no armor)
 *   java -cp .;<target/classes> InteropTest decrypt-raw <password> <inFile>
 */
public class InteropTest {
    public static void main(String[] args) throws Exception {
        String cmd = args[0];
        String password = args[1];

        switch (cmd) {
            case "encrypt-message": {
                String plaintext = new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8);
                EncryptionWrapper w = new EncryptionWrapper();
                String wrapped = w.encryptFile(plaintext, password);
                Files.write(Paths.get(args[3]), wrapped.getBytes(StandardCharsets.UTF_8));
                System.out.println("wrote " + args[3]);
                break;
            }
            case "decrypt-message": {
                String wrapped = new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8);
                EncryptionWrapper w = new EncryptionWrapper();
                String plaintext = w.decryptMessage(wrapped, password);
                Files.write(Paths.get(args[3]), plaintext.getBytes(StandardCharsets.UTF_8));
                System.out.println("wrote " + args[3]);
                break;
            }
            case "encrypt-raw": {
                String plaintext = new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8);
                AESEncryption enc = new AESEncryption();
                String b64 = enc.encryptString(password, plaintext);
                Files.write(Paths.get(args[3]), b64.getBytes(StandardCharsets.UTF_8));
                System.out.println("wrote " + args[3]);
                break;
            }
            case "decrypt-raw": {
                String b64 = new String(Files.readAllBytes(Paths.get(args[2])), StandardCharsets.UTF_8).trim();
                AESEncryption enc = new AESEncryption();
                String plaintext = enc.decryptString(password, b64);
                Files.write(Paths.get(args[3]), plaintext.getBytes(StandardCharsets.UTF_8));
                System.out.println("wrote " + args[3]);
                break;
            }
            default:
                System.err.println("unknown command: " + cmd);
                System.exit(1);
        }
    }
}
