package ru.mirea.bruteforce.hash;

/**
 * Единый интерфейс проверки кандидата пароля против целевого хэша.
 * <p>
 * Переборщику не важно, какой именно алгоритм за ним стоит: он лишь
 * спрашивает "подходит ли этот пароль к этому хэшу?". Это позволяет
 * добавлять новые алгоритмы, не трогая движок перебора.
 */
public interface Hasher {

    /** Тип алгоритма. */
    HashType type();

    /**
     * Проверяет, даёт ли кандидат {@code candidate} целевой хэш {@code targetHash}.
     *
     * @param candidate  проверяемый пароль
     * @param targetHash целевой хэш (hex для MD5/SHA-1, modular crypt format для bcrypt/argon2)
     * @return {@code true}, если пароль подходит
     */
    boolean matches(String candidate, String targetHash);

    /**
     * Вычисляет хэш от строки (нужно для генерации тестовых хэшей, команда {@code hash}).
     *
     * @param plaintext открытый текст
     * @return строковое представление хэша
     */
    String hash(String plaintext);
}
