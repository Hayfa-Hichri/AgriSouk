package tn.isetsb.agrisouk.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Hachage des mots de passe : aucun mot de passe n'est stocké en clair.
 */
public final class SecurityUtils {

    private static final String SALT = "agrisouk::iset-sidi-bouzid";

    private SecurityUtils() {
    }

    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((SALT + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format(Locale.ROOT, "%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithme SHA-256 indisponible", e);
        }
    }
}
