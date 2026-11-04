package ru.mirea.bruteforce;

/**
 * Готовые наборы символов (алфавиты) для перебора.
 * Чем шире алфавит, тем больше пространство перебора: при длине L число
 * кандидатов равно {@code charset.length ^ L}.
 */
public final class Charsets {

    public static final String DIGITS = "0123456789";
    public static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWER_DIGITS = LOWER + DIGITS;
    public static final String ALPHANUM = LOWER + UPPER + DIGITS;
    /** Буквы, цифры и распространённые спецсимволы ASCII. */
    public static final String ASCII = ALPHANUM + "!@#$%^&*()-_=+[]{};:,.?/";

    private Charsets() {
    }

    /** Возвращает алфавит по имени пресета либо сам набор, если передан явно. */
    public static String resolve(String preset) {
        return switch (preset.toLowerCase()) {
            case "digits" -> DIGITS;
            case "lower" -> LOWER;
            case "upper" -> UPPER;
            case "lower+digits", "lowerdigits" -> LOWER_DIGITS;
            case "alphanum" -> ALPHANUM;
            case "ascii" -> ASCII;
            // Иначе считаем, что пользователь передал собственный набор символов.
            default -> preset;
        };
    }
}
