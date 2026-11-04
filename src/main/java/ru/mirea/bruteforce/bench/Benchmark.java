package ru.mirea.bruteforce.bench;

import ru.mirea.bruteforce.BruteForcer;
import ru.mirea.bruteforce.Charsets;
import ru.mirea.bruteforce.CrackResult;
import ru.mirea.bruteforce.hash.HashType;
import ru.mirea.bruteforce.hash.Hasher;
import ru.mirea.bruteforce.hash.HasherFactory;

import java.util.List;

/**
 * Автоматический прогон перебора по всем тестовым хэшам ТЗ с замером времени.
 * Печатает таблицу результатов (пароль × алгоритм → время, попытки), которую
 * можно вставить в отчёт.
 */
public final class Benchmark {

    private final String charset;
    private final int maxLength;
    private final int threads;
    /** Ограничение по времени на один хэш, чтобы не ждать «очень сложные» вечно. */
    private final long timeBudgetMs;

    public Benchmark(String charsetPreset, int maxLength, int threads, long timeBudgetMs) {
        this.charset = Charsets.resolve(charsetPreset);
        this.maxLength = maxLength;
        this.threads = threads;
        this.timeBudgetMs = timeBudgetMs;
    }

    public void run(List<HashType> types) {
        System.out.println("=== Бенчмарк переборщика (семестровое №3) ===");
        System.out.printf("Алфавит: %d симв., макс. длина: %d, потоков: %d, бюджет: %d с/хэш%n",
                charset.length(), maxLength, threads, timeBudgetMs / 1000);
        System.out.println();
        System.out.printf("%-8s | %-13s | %-9s | %12s | %14s%n",
                "алгоритм", "сложность", "найдено", "время, с", "попыток");
        System.out.println("-".repeat(72));

        for (HashType type : types) {
            Hasher hasher = HasherFactory.create(type);
            for (TestHashes.Vector v : TestHashes.forType(type)) {
                CrackResult r = crackWithBudget(hasher, v.hash());
                System.out.printf("%-8s | %-13s | %-9s | %12.3f | %,14d%n",
                        type, v.difficulty(),
                        r.found() ? r.password() : "— (не найден)",
                        r.elapsedMs() / 1000.0,
                        r.attempts());
            }
            System.out.println("-".repeat(72));
        }
    }

    /**
     * Запускает перебор в отдельном потоке и прерывает его по истечении бюджета.
     * «Очень сложные» пароли и bcrypt/argon2 на длинных паролях не вскроются —
     * это ожидаемый результат, демонстрирующий стойкость алгоритмов.
     */
    private CrackResult crackWithBudget(Hasher hasher, String targetHash) {
        BruteForcer bf = new BruteForcer(hasher, charset, 1, maxLength, threads);
        final CrackResult[] box = new CrackResult[1];

        Thread runner = new Thread(() -> box[0] = bf.crack(targetHash), "bench-runner");
        runner.setDaemon(true);
        long start = System.nanoTime();
        runner.start();
        try {
            runner.join(timeBudgetMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (runner.isAlive()) {
            // Бюджет исчерпан: считаем хэш не вскрытым за отведённое время.
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            return new CrackResult(false, null, -1, elapsedMs, threads);
        }
        return box[0];
    }
}
