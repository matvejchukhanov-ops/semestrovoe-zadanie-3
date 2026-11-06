package ru.mirea.bruteforce;

import ru.mirea.bruteforce.bench.Benchmark;
import ru.mirea.bruteforce.hash.HashType;
import ru.mirea.bruteforce.hash.Hasher;
import ru.mirea.bruteforce.hash.HasherFactory;

import java.util.List;

/**
 * Точка входа и разбор аргументов командной строки.
 * <p>
 * Команды:
 * <pre>
 *   crack  --algo &lt;md5|sha1|bcrypt|argon2&gt; --hash &lt;hash&gt;
 *          [--charset &lt;пресет|символы&gt;] [--max N] [--min N] [--threads N]
 *   hash   --algo &lt;...&gt; --text &lt;строка&gt;
 *   benchmark [--charset ...] [--max N] [--threads N] [--budget СЕК] [--algo ...]
 * </pre>
 */
public final class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        String command = args[0];
        Args a = new Args(args);
        try {
            switch (command) {
                case "crack" -> runCrack(a);
                case "hash" -> runHash(a);
                case "benchmark" -> runBenchmark(a);
                case "help", "-h", "--help" -> printUsage();
                default -> {
                    System.err.println("Неизвестная команда: " + command);
                    printUsage();
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
            System.exit(2);
        }
    }

    private static void runCrack(Args a) {
        HashType type = HasherFactory.parse(a.require("algo"));
        String hash = a.require("hash");
        String charset = Charsets.resolve(a.get("charset", "lower+digits"));
        int min = Integer.parseInt(a.get("min", "1"));
        int max = Integer.parseInt(a.get("max", "6"));
        int threads = Integer.parseInt(a.get("threads",
                String.valueOf(Runtime.getRuntime().availableProcessors())));

        Hasher hasher = HasherFactory.create(type);
        BruteForcer bf = new BruteForcer(hasher, charset, min, max, threads);

        System.out.printf("Подбор %s-пароля | алфавит %d симв. | длины %d..%d | потоков %d%n",
                type, charset.length(), min, max, threads);
        CrackResult r = bf.crack(hash);

        System.out.println("-".repeat(50));
        if (r.found()) {
            System.out.println("ПАРОЛЬ НАЙДЕН: " + r.password());
        } else {
            System.out.println("Пароль не найден в заданном пространстве перебора.");
        }
        System.out.printf("Проверено кандидатов: %,d%n", r.attempts());
        System.out.printf("Время подбора: %s%n", r.formattedElapsed());
        System.out.printf("Скорость: %,.0f хэшей/с%n", r.hashesPerSecond());
    }

    private static void runHash(Args a) {
        HashType type = HasherFactory.parse(a.require("algo"));
        String text = a.require("text");
        Hasher hasher = HasherFactory.create(type);
        System.out.println(hasher.hash(text));
    }

    private static void runBenchmark(Args a) {
        String charset = a.get("charset", "lower+digits");
        int max = Integer.parseInt(a.get("max", "5"));
        int threads = Integer.parseInt(a.get("threads",
                String.valueOf(Runtime.getRuntime().availableProcessors())));
        long budget = Long.parseLong(a.get("budget", "30")) * 1000;

        List<HashType> types = a.has("algo")
                ? List.of(HasherFactory.parse(a.require("algo")))
                : List.of(HashType.MD5, HashType.SHA1, HashType.BCRYPT, HashType.ARGON2);

        new Benchmark(charset, max, threads, budget).run(types);
    }

    private static void printUsage() {
        System.out.println("""
                Переборщик паролей — семестровое задание №3 (МИРЭA, РБПО)

                Команды:
                  crack     --algo <md5|sha1|bcrypt|argon2> --hash <HASH>
                            [--charset <digits|lower|upper|lower+digits|alphanum|ascii|СИМВОЛЫ>]
                            [--min N] [--max N] [--threads N]
                  hash      --algo <...> --text <СТРОКА>
                  benchmark [--charset ...] [--max N] [--threads N] [--budget СЕК] [--algo ...]

                Примеры:
                  crack --algo md5 --hash e10adc3949ba59abbe56e057f20f883e --charset digits --max 8
                  crack --algo bcrypt --hash "$2a$10$..." --charset lower --max 5
                  hash  --algo bcrypt --text 123456
                  benchmark --charset digits --max 6 --threads 8 --budget 20
                """);
    }

    /** Минимальный разбор аргументов вида {@code --ключ значение}. */
    private static final class Args {
        private final String[] argv;

        Args(String[] argv) {
            this.argv = argv;
        }

        boolean has(String key) {
            for (String s : argv) {
                if (s.equals("--" + key)) {
                    return true;
                }
            }
            return false;
        }

        String get(String key, String def) {
            for (int i = 0; i < argv.length - 1; i++) {
                if (argv[i].equals("--" + key)) {
                    return argv[i + 1];
                }
            }
            return def;
        }

        String require(String key) {
            String v = get(key, null);
            if (v == null) {
                throw new IllegalArgumentException("не задан обязательный параметр --" + key);
            }
            return v;
        }
    }
}
