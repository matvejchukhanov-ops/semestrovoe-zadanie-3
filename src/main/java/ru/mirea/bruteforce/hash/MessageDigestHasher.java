package ru.mirea.bruteforce.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Хэшер поверх {@link MessageDigest} — используется для MD5 и SHA-1.
 * <p>
 * {@link MessageDigest} не потокобезопасен, поэтому экземпляр создаётся
 * внутри каждого вызова {@link #digest}. Для перебора это дешевле, чем
 * синхронизация, и каждый поток работает со своей копией.
 */
public final class MessageDigestHasher implements Hasher {

    private final HashType type;
    private final String algorithm; // имя алгоритма для MessageDigest.getInstance

    public MessageDigestHasher(HashType type, String algorithm) {
        this.type = type;
        this.algorithm = algorithm;
    }

    @Override
    public HashType type() {
        return type;
    }

    @Override
    public boolean matches(String candidate, String targetHash) {
        // Сравнение без учёта регистра: hex может быть записан по-разному.
        return digest(candidate).equalsIgnoreCase(targetHash.trim());
    }

    @Override
    public String hash(String plaintext) {
        return digest(plaintext);
    }

    private String digest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            // MD5 и SHA-1 входят в стандартную поставку JDK, сюда не попадём.
            throw new IllegalStateException("Алгоритм недоступен: " + algorithm, e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
