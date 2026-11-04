package ru.mirea.bruteforce.bench;

import ru.mirea.bruteforce.hash.HashType;

import java.util.List;

/**
 * Эталонные тестовые хэши из ТЗ (семестровое задание №3).
 * <p>
 * Параметры генерации: bcrypt cost=10, Argon2id m=65536, t=3, p=2, соль "saltsalt".
 * По каждому алгоритму дано 4 пароля возрастающей сложности.
 */
public final class TestHashes {

    /** Один тестовый вектор: метка сложности и строка хэша. */
    public record Vector(String difficulty, String hash) {
    }

    private TestHashes() {
    }

    public static final List<Vector> SHA1 = List.of(
            new Vector("лёгкий", "7c4a8d09ca3762af61e59520943dc26494f8941b"),
            new Vector("средний", "d0be2dc421be4fcd0172e5afceea3970e2f3d940"),
            new Vector("сложный", "666846867fc5e0a46a7afc53eb8060967862f333"),
            new Vector("очень сложный", "6e157c5da4410b7e9de85f5c93026b9176e69064")
    );

    public static final List<Vector> MD5 = List.of(
            new Vector("лёгкий", "e10adc3949ba59abbe56e057f20f883e"),
            new Vector("средний", "1f3870be274f6c49b3e31a0c6728957f"),
            new Vector("сложный", "77892341aa9dc66e97f5c248782b5d92"),
            new Vector("очень сложный", "686e697538050e4664636337cc3b834f")
    );

    public static final List<Vector> BCRYPT = List.of(
            new Vector("лёгкий", "$2a$10$z4u9ZkvopUiiytaNX7wfGedy9Lu2ywUxwYpbsAR5YBrAuUs3YGXdi"),
            new Vector("средний", "$2a$10$26GB/T2/6aTsMkTjCgqm/.JP8SUjr32Bhfn9m9smtDiIwM4QIt2ze"),
            new Vector("сложный", "$2a$10$Q9M0vLLrE4/nu/9JEMXFTewB3Yr9uMdIEZ1Sgdk1NQTjHwLN0asfi"),
            new Vector("очень сложный", "$2a$10$yZBadi8Szw0nItV2g96P6eqctI2kbG/.mb0uD/ID9tlof0zpJLLL2")
    );

    public static final List<Vector> ARGON2 = List.of(
            new Vector("лёгкий", "$argon2id$v=19$m=65536,t=3,p=2$c2FsdHNhbHQ$PUF5UxxoUY++mMekkQwFurL0ZsTtB7lelO23zcyZQ0c"),
            new Vector("средний", "$argon2id$v=19$m=65536,t=3,p=2$c2FsdHNhbHQ$HYQwRUw9VcfkvqkUQ5ppyYPom6f/ro3ZCXYznhrYZw4"),
            new Vector("сложный", "$argon2id$v=19$m=65536,t=3,p=2$c2FsdHNhbHQ$9asGA7Xv3vQBz7Yyh4/Ntw0GQgOg8R6OWolOfRETrEg"),
            new Vector("очень сложный", "$argon2id$v=19$m=65536,t=3,p=2$c2FsdHNhbHQ$+smq45/czydGj0lYNdZVXF++FOXJwrkXt6VUIcEauvo")
    );

    public static List<Vector> forType(HashType type) {
        return switch (type) {
            case MD5 -> MD5;
            case SHA1 -> SHA1;
            case BCRYPT -> BCRYPT;
            case ARGON2 -> ARGON2;
        };
    }
}
