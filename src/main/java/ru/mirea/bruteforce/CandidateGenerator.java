package ru.mirea.bruteforce;

/**
 * Преобразование числового индекса в строку-кандидат фиксированной длины.
 * <p>
 * Все кандидаты длины {@code L} из алфавита мощности {@code base} можно
 * занумеровать числами {@code 0 .. base^L - 1}, трактуя индекс как число в
 * системе счисления с основанием {@code base}. Это даёт детерминированный
 * доступ к любому кандидату по номеру и позволяет легко делить пространство
 * перебора между потоками (каждый поток берёт свой непрерывный диапазон
 * индексов).
 */
public final class CandidateGenerator {

    private final char[] alphabet;

    public CandidateGenerator(String charset) {
        this.alphabet = charset.toCharArray();
    }

    public int base() {
        return alphabet.length;
    }

    /** Количество кандидатов длины {@code length}: base^length. */
    public long countForLength(int length) {
        long total = 1;
        for (int i = 0; i < length; i++) {
            total *= alphabet.length;
        }
        return total;
    }

    /**
     * Строит кандидата длины {@code length} по индексу {@code index}.
     * Индекс раскладывается по основанию {@code base} (система счисления).
     */
    public String at(long index, int length) {
        char[] buf = new char[length];
        long value = index;
        for (int pos = length - 1; pos >= 0; pos--) {
            buf[pos] = alphabet[(int) (value % alphabet.length)];
            value /= alphabet.length;
        }
        return new String(buf);
    }
}
