package cncs.academy.ess.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public static String hashPassword(String password) throws Exception {
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        SecretKeyFactory skf =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] hash = skf.generateSecret(spec).getEncoded();

        return ITERATIONS + ":" +
                Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(String password, String stored)
            throws Exception {

        String[] parts = stored.split(":");
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] hash = Base64.getDecoder().decode(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                hash.length * 8
        );

        SecretKeyFactory skf =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] testHash = skf.generateSecret(spec).getEncoded();

        return java.security.MessageDigest.isEqual(hash, testHash);
    }
}
