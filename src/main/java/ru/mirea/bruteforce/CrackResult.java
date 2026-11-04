package ru.mirea.bruteforce;

/**
 * Результат попытки подбора пароля.
 *
 * @param found     найден ли пароль
 * @param password  восстановленный пароль (или null)
 * @param attempts  сколько кандидатов проверено
 * @param elapsedMs затраченное время, мс
 * @param threads   число рабочих потоков
 */
public record CrackResult(boolean found, String password, long attempts, long elapsedMs, int threads) {

    /** Скорость перебора, хэшей в секунду. */
    public double hashesPerSecond() {
        if (elapsedMs <= 0) {
            return attempts;
        }
        return attempts / (elapsedMs / 1000.0);
    }
}
