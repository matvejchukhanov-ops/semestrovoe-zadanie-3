package ru.mirea.bruteforce.hash;

/**
 * Фабрика хэшеров по типу алгоритма и по имени из командной строки.
 */
public final class HasherFactory {

    private HasherFactory() {
    }

    public static Hasher create(HashType type) {
        return switch (type) {
            case MD5 -> new MessageDigestHasher(HashType.MD5, "MD5");
            case SHA1 -> new MessageDigestHasher(HashType.SHA1, "SHA-1");
            case BCRYPT -> new BcryptHasher();
            case ARGON2 -> new Argon2Hasher();
        };
    }

    /** Разбирает имя алгоритма из CLI ("md5", "sha1", "bcrypt", "argon2"). */
    public static HashType parse(String name) {
        return switch (name.toLowerCase()) {
            case "md5" -> HashType.MD5;
            case "sha1", "sha-1" -> HashType.SHA1;
            case "bcrypt" -> HashType.BCRYPT;
            case "argon2", "argon2id" -> HashType.ARGON2;
            default -> throw new IllegalArgumentException("Неизвестный алгоритм: " + name
                    + " (доступны: md5, sha1, bcrypt, argon2)");
        };
    }
}
