package ru.mirea.bruteforce.hash;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Хэшер bcrypt (библиотека jBCrypt).
 * <p>
 * bcrypt — <b>основной</b> алгоритм этого задания. Соль и cost-фактор зашиты
 * прямо в строку хэша ({@code $2a$10$...}), поэтому проверка кандидата — это
 * один вызов {@link BCrypt#checkpw}. Каждая такая проверка намеренно
 * дорогая (cost=10 → 2^10 раундов), что на порядки замедляет перебор по
 * сравнению с MD5/SHA-1 — в этом и смысл адаптивной хэш-функции.
 */
public final class BcryptHasher implements Hasher {

    /** cost-фактор по умолчанию (как в тестовых хэшах ТЗ: "$2a$10$"). */
    private final int cost;

    public BcryptHasher() {
        this(10);
    }

    public BcryptHasher(int cost) {
        this.cost = cost;
    }

    @Override
    public HashType type() {
        return HashType.BCRYPT;
    }

    @Override
    public boolean matches(String candidate, String targetHash) {
        try {
            return BCrypt.checkpw(candidate, targetHash.trim());
        } catch (IllegalArgumentException e) {
            // Некорректный формат целевого хэша.
            return false;
        }
    }

    @Override
    public String hash(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(cost));
    }
}
