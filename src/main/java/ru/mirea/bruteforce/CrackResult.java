package ru.mirea.bruteforce;

/**
 * Результат попытки подбора пароля.
 * <p>
 * Время хранится в наносекундах ({@code elapsedNanos}) для максимальной
 * точности: при успехе оно фиксируется в момент, когда пароль был найден
 * (а не по завершении всех потоков).
 *
 * @param found        найден ли пароль
 * @param password     восстановленный пароль (или null)
 * @param attempts     сколько кандидатов проверено
 * @param elapsedNanos затраченное время, нс (до момента находки при успехе)
 * @param threads      число рабочих потоков
 */
public record CrackResult(boolean found, String password, long attempts, long elapsedNanos, int threads) {

    /** Затраченное время в миллисекундах (целое). */
    public long elapsedMs() {
        return elapsedNanos / 1_000_000;
    }

    /** Затраченное время в миллисекундах (с дробной частью). */
    public double elapsedMillis() {
        return elapsedNanos / 1_000_000.0;
    }

    /** Затраченное время в секундах (с дробной частью). */
    public double elapsedSeconds() {
        return elapsedNanos / 1_000_000_000.0;
    }

    /** Скорость перебора, хэшей в секунду. */
    public double hashesPerSecond() {
        if (elapsedNanos <= 0) {
            return attempts;
        }
        return attempts / elapsedSeconds();
    }

    /**
     * Человекочитаемое точное время подбора.
     * Примеры: {@code "0,330 с (330,142 мс)"}, {@code "1 мин 03,452 с"}.
     */
    public String formattedElapsed() {
        double seconds = elapsedSeconds();
        if (seconds >= 60.0) {
            long minutes = (long) (seconds / 60);
            double rest = seconds - minutes * 60;
            return String.format("%d мин %06.3f с", minutes, rest);
        }
        return String.format("%.3f с (%.3f мс)", seconds, elapsedMillis());
    }
}
