# Семестровое задание №3 — Переборщик паролей

**Дисциплина:** Разработка безопасного программного обеспечения (МИРЭА)
**Тема:** Сравнительное исследование алгоритмов хэширования (MD5, SHA-1, bcrypt, Argon2)

Многопоточное средство подбора пароля методом полного перебора (bruteforce) на
Java. Программа принимает на вход хэш и восстанавливает исходный пароль, а также
умеет замерять время подбора по всем тестовым хэшам из задания для сравнения
стойкости алгоритмов.

> ⚠️ **Назначение — учебное.** Инструмент предназначен исключительно для
> лабораторной работы по оценке стойкости хэш-функций на заранее заданных
> тестовых хэшах. Применение для подбора чужих паролей недопустимо.

---

## Что реализовано

- **4 алгоритма:** MD5, SHA-1 (через `MessageDigest`), **bcrypt** (jBCrypt, основной
  алгоритм задания), Argon2id (argon2-jvm).
- **Полный перебор** с настраиваемым алфавитом и длиной пароля.
- **Многопоточность:** пространство перебора делится на равные диапазоны между
  рабочими потоками; кооперативная остановка через общий флаг (без `Thread.stop()`).
- **Бенчмарк** по всем 12+ тестовым хэшам ТЗ с таблицей времени и числа попыток.
- **Генерация хэшей** (команда `hash`) для подготовки собственных тестовых данных.

## Структура проекта

```
semestrovoe-3/
├── pom.xml                     # Maven-проект (Java 21)
├── README.md
├── report/
│   └── REPORT.md               # Отчёт: методика, таблицы, выводы, команды hashcat/john
└── src/main/java/ru/mirea/bruteforce/
    ├── Main.java               # CLI: команды crack / hash / benchmark
    ├── BruteForcer.java        # Многопоточный движок перебора
    ├── CandidateGenerator.java # Индекс → кандидат (нумерация пространства перебора)
    ├── Charsets.java           # Пресеты алфавитов
    ├── CrackResult.java        # Результат (пароль, время, попытки, скорость)
    ├── hash/
    │   ├── Hasher.java             # Общий интерфейс проверки кандидата
    │   ├── HashType.java
    │   ├── HasherFactory.java
    │   ├── MessageDigestHasher.java # MD5 и SHA-1
    │   ├── BcryptHasher.java        # bcrypt (основной)
    │   └── Argon2Hasher.java        # Argon2id
    └── bench/
        ├── Benchmark.java      # Прогон по тестовым хэшам с бюджетом времени
        └── TestHashes.java     # Эталонные хэши из ТЗ
```

## Сборка

### Вариант A — Maven (рекомендуется, так открывается в IntelliJ IDEA)

```bash
mvn clean package
java -jar target/bruteforcer.jar benchmark
```

### Вариант B — вручную через javac (без Maven)

```bash
# 1. Скачать зависимости
mkdir -p lib && cd lib
BASE=https://repo1.maven.org/maven2
curl -fsSLO $BASE/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar
curl -fsSLO $BASE/de/mkammerer/argon2-jvm/2.11/argon2-jvm-2.11.jar
curl -fsSLO $BASE/de/mkammerer/argon2-jvm-nolibs/2.11/argon2-jvm-nolibs-2.11.jar
curl -fsSLO $BASE/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar
cd ..

# 2. Скомпилировать
javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name "*.java")

# 3. Запустить (Windows: разделитель classpath — ';', Linux/macOS — ':')
java -Dstdout.encoding=UTF-8 -cp "out;lib/*" ru.mirea.bruteforce.Main benchmark
```

> На Windows для корректного вывода кириллицы добавляйте `-Dstdout.encoding=UTF-8`.

## Использование

```bash
# Подбор пароля
crack  --algo <md5|sha1|bcrypt|argon2> --hash <HASH>
       [--charset <digits|lower|upper|lower+digits|alphanum|ascii|СИМВОЛЫ>]
       [--min N] [--max N] [--threads N]

# Вычислить хэш (для своих тестов)
hash   --algo <...> --text <СТРОКА>

# Прогнать все тестовые хэши ТЗ с замером времени
benchmark [--charset ...] [--max N] [--threads N] [--budget СЕК] [--algo ...]
```

### Примеры

```bash
# Лёгкий MD5 из ТЗ (пароль 123456) — доли секунды
java -jar target/bruteforcer.jar crack --algo md5 \
  --hash e10adc3949ba59abbe56e057f20f883e --charset digits --max 8

# bcrypt — на порядки медленнее (десятки хэшей/с вместо миллионов)
java -jar target/bruteforcer.jar crack --algo bcrypt \
  --hash '$2a$10$...' --charset lower --max 4 --threads 8

# Полный бенчмарк с бюджетом 30 с на хэш
java -jar target/bruteforcer.jar benchmark --charset lower+digits --max 6 --budget 30
```

## Сравнение алгоритмов (кратко)

| Алгоритм | Тип | Соль | Стоимость | Скорость перебора* | Стойкость |
|----------|-----|------|-----------|--------------------|-----------|
| MD5      | быстрый хэш | нет | — | ~3 400 000 хэшей/с | очень низкая |
| SHA-1    | быстрый хэш | нет | — | ~3 300 000 хэшей/с | низкая |
| bcrypt   | адаптивный | да | cost=10 | ~80 хэшей/с | высокая |
| Argon2id | адаптивный, memory-hard | да | m=64 МБ, t=3 | ~20 хэшей/с | очень высокая |

\* Ориентировочно, одна и та же машина; точные цифры — в [отчёте](report/REPORT.md).

**Вывод:** MD5 и SHA-1 перебираются миллионами вариантов в секунду и непригодны
для хранения паролей. bcrypt и Argon2 за счёт вычислительной (и у Argon2 —
дополнительно memory-hard) стоимости замедляют перебор в десятки тысяч раз, что
делает подбор сложных паролей практически невозможным.

Полное описание методики, таблицы с замерами и сравнение с hashcat/John the Ripper —
в файле [report/REPORT.md](report/REPORT.md).
