package ru.mirea.bruteforce;

import ru.mirea.bruteforce.hash.Hasher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Многопоточный переборщик паролей методом полного перебора (bruteforce).
 * <p>
 * Пространство перебора для каждой длины L — это числа {@code 0 .. base^L-1}
 * (см. {@link CandidateGenerator}). Этот диапазон делится на равные части
 * между рабочими потоками, каждый поток независимо проверяет свои кандидаты.
 * <p>
 * Синхронизация сведена к минимуму и реализована в духе теоретической части
 * ТЗ (глава «Завершение потоков»): вместо принудительной остановки каждый
 * поток сам периодически проверяет общий флаг {@link AtomicBoolean} и
 * завершается, как только один из потоков нашёл пароль. Так достигается
 * кооперативная остановка без {@code Thread.stop()}.
 */
public final class BruteForcer {

    /** Как часто поток сверяется с общим флагом остановки (в итерациях). */
    private static final int CANCEL_CHECK_INTERVAL = 2048;

    private final Hasher hasher;
    private final CandidateGenerator generator;
    private final int minLength;
    private final int maxLength;
    private final int threads;

    public BruteForcer(Hasher hasher, String charset, int minLength, int maxLength, int threads) {
        this.hasher = hasher;
        this.generator = new CandidateGenerator(charset);
        this.minLength = Math.max(1, minLength);
        this.maxLength = maxLength;
        this.threads = Math.max(1, threads);
    }

    /**
     * Подбирает пароль к заданному хэшу.
     *
     * @param targetHash целевой хэш
     * @return результат перебора (с флагом успеха, временем и числом попыток)
     */
    public CrackResult crack(String targetHash) {
        AtomicReference<String> foundPassword = new AtomicReference<>(null);
        AtomicBoolean stop = new AtomicBoolean(false);
        AtomicLong totalAttempts = new AtomicLong(0);
        // Момент (нс), когда пароль был найден; 0 — пока не найден.
        AtomicLong foundAtNs = new AtomicLong(0);

        long startNs = System.nanoTime();

        // Перебираем длины по возрастанию: короткие пароли находятся первыми.
        for (int length = minLength; length <= maxLength && !stop.get(); length++) {
            searchLength(targetHash, length, foundPassword, stop, totalAttempts, foundAtNs);
        }

        // Точное время: при успехе — до момента находки, иначе — весь прогон.
        long endNs = (foundAtNs.get() > 0) ? foundAtNs.get() : System.nanoTime();
        long elapsedNanos = endNs - startNs;
        String password = foundPassword.get();
        return new CrackResult(password != null, password, totalAttempts.get(), elapsedNanos, threads);
    }

    /** Перебор всех кандидатов фиксированной длины в несколько потоков. */
    private void searchLength(String targetHash, int length,
                              AtomicReference<String> foundPassword,
                              AtomicBoolean stop,
                              AtomicLong totalAttempts,
                              AtomicLong foundAtNs) {
        long total = generator.countForLength(length);
        long chunk = (total + threads - 1) / threads; // округление вверх

        List<Thread> workers = new ArrayList<>(threads);
        for (int t = 0; t < threads; t++) {
            long from = t * chunk;
            if (from >= total) {
                break; // кандидатов меньше, чем потоков
            }
            long to = Math.min(from + chunk, total);
            Thread worker = new Thread(
                    () -> searchRange(targetHash, length, from, to, foundPassword, stop, totalAttempts, foundAtNs),
                    "bf-" + length + "-" + t);
            workers.add(worker);
            worker.start();
        }

        // Главный поток дожидается завершения всех рабочих (аналог join() из ТЗ).
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stop.set(true);
            }
        }
    }

    /** Работа одного потока: проверка кандидатов с индексами [from, to). */
    private void searchRange(String targetHash, int length, long from, long to,
                             AtomicReference<String> foundPassword,
                             AtomicBoolean stop,
                             AtomicLong totalAttempts,
                             AtomicLong foundAtNs) {
        long localAttempts = 0;
        for (long index = from; index < to; index++) {
            // Периодически проверяем, не нашёл ли кто-то пароль.
            if ((localAttempts & (CANCEL_CHECK_INTERVAL - 1)) == 0 && stop.get()) {
                break;
            }
            String candidate = generator.at(index, length);
            localAttempts++;
            if (hasher.matches(candidate, targetHash)) {
                // Фиксируем точный момент находки (первый нашедший поток).
                foundAtNs.compareAndSet(0, System.nanoTime());
                foundPassword.set(candidate);
                stop.set(true);
                break;
            }
        }
        totalAttempts.addAndGet(localAttempts);
    }
}
