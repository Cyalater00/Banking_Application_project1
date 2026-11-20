package com.banking.security;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Preserves the application's PBKDF2 password and recovery-code hashing. */
public final class PasswordUtil {
    private static final int HASH_ITERATIONS = 210000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() { }

    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public static byte[] hashPassword(String password, byte[] salt)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, HASH_ITERATIONS, 256);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
