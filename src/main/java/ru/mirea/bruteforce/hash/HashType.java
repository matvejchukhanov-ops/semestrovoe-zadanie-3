package ru.mirea.bruteforce.hash;

/**
 * Поддерживаемые алгоритмы хэширования из ТЗ.
 * MD5 и SHA-1 — быстрые «сырые» хэши (ломаются перебором мгновенно).
 * BCRYPT и ARGON2 — медленные адаптивные функции с солью (устойчивы к перебору).
 */
public enum HashType {
    MD5,
    SHA1,
    BCRYPT,
    ARGON2
}
