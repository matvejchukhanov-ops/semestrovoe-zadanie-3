package ru.mirea.bruteforce.hash;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Хэшер Argon2id (библиотека argon2-jvm, нативная реальзация через JNA).
 * <p>
 * Argon2 — победитель Password Hashing Competition и самый устойчивый из
 * четырёх алгоритмов задания: помимо вычислительной стоимости он требует
 * большого объёма памяти (в тестовых хэшах ТЗ — m=65536 КБ). Это делает
 * перебор, в том числе на GPU, крайне дорогим.
 * <p>
 * Параметры (t, m, p) и соль берутся из самой строки хэша при проверке,
 * поэтому {@link #matches} работает с любыми корректными argon2-хэшами.
 */
public final class Argon2Hasher implements Hasher {

    // Параметры по умолчанию для команды hash (как в ТЗ): t=3, m=65536, p=2.
    private static final int ITERATIONS = 3;
    private static final int MEMORY_KB = 65536;
    private static final int PARALLELISM = 2;

    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    @Override
    public HashType type() {
        return HashType.ARGON2;
    }

    @Override
    public boolean matches(String candidate, String targetHash) {
        try {
            return argon2.verify(targetHash.trim(), candidate.toCharArray());
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public String hash(String plaintext) {
        // ВНИМАНИЕ: argon2-jvm генерирует случайную соль, поэтому строка не
        // совпадёт побайтово с хэшами ТЗ (там фиксированная соль "saltsalt"),
        // но будет корректно проверяться методом matches().
        return argon2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, plaintext.toCharArray());
    }
}
