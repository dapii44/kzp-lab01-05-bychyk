package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Спільний абстрактний тип тренувань.
 *
 * <p>Лабораторна робота № 3: спільні поля та їхні інваріанти перенесено до базового класу, а
 * різну поведінку ({@link #load()}) реалізують підтипи {@link IndoorTraining} і
 * {@link OutdoorTraining}. Спадкування обрано, бо кожне тренування <em>є різновидом</em>
 * тренування (is-a) і всі підтипи мають спільний стан та перевірки; композиція не потрібна.
 * Стан незмінний, тому об'єкти безпечно використовувати як ключі {@code HashMap}.
 */
public abstract class Training {
    private static final int FIELD_COUNT = 5;

    private final String date;
    private final TrainingKind kind;
    private final int minutes;
    private final double distanceKm;
    private final int calories;

    /**
     * Створює тренування після перевірки всіх спільних інваріантів.
     *
     * <p>Перевірки виконуються в аргументах делегування {@code this(...)}: якщо значення хибне,
     * виняток виникає до присвоєння полів, а частково створений об'єкт не існує.
     *
     * @param date дата {@code YYYY-MM-DD}; не {@code null} і не порожня
     * @param kind вид тренування; не {@code null}
     * @param minutes тривалість, {@code >= 0}
     * @param distanceKm дистанція, скінченна і {@code >= 0}
     * @param calories калорії, {@code >= 0}
     * @throws NullPointerException якщо {@code date} або {@code kind} дорівнює {@code null}
     * @throws IllegalArgumentException якщо текст порожній або число недопустиме
     */
    protected Training(String date, TrainingKind kind, int minutes, double distanceKm,
            int calories) {
        this(checkDate(date), checkKind(kind), checkNonNegative(minutes),
                checkDistance(distanceKm), checkNonNegative(calories), true);
    }

    /* Присвоює вже перевірений стан під час внутрішнього делегування. */
    private Training(String date, TrainingKind kind, int minutes, double distanceKm,
            int calories, boolean validated) {
        this.date = date;
        this.kind = kind;
        this.minutes = minutes;
        this.distanceKm = distanceKm;
        this.calories = calories;
    }

    /* Перевіряє, що дата існує й не порожня. */
    private static String checkDate(String date) {
        Objects.requireNonNull(date, "Дата не може бути null");
        if (date.isBlank()) {
            throw new IllegalArgumentException("порожня дата або вид тренування");
        }
        return date;
    }

    /* Перевіряє, що вид існує. */
    private static TrainingKind checkKind(TrainingKind kind) {
        return Objects.requireNonNull(kind, "Вид тренування не може бути null");
    }

    /* Перевіряє, що ціле значення не від'ємне. */
    private static int checkNonNegative(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("від'ємне числове значення");
        }
        return value;
    }

    /* Перевіряє, що дистанція не від'ємна й скінченна (NaN не менший за нуль). */
    private static double checkDistance(double distanceKm) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("від'ємне числове значення");
        }
        if (!Double.isFinite(distanceKm)) {
            throw new IllegalArgumentException("поле distanceKm має бути скінченним числом");
        }
        return distanceKm;
    }

    /**
     * Створює тренування потрібного підтипу за підписом виду.
     *
     * <p>Вибір підтипу виконується лише тут, на межі створення; далі код працює зі спільним
     * типом і не перевіряє конкретний клас.
     *
     * @param date дата {@code YYYY-MM-DD}
     * @param kindLabel підпис виду, наприклад {@code Біг}
     * @param minutes тривалість у хвилинах
     * @param distanceKm дистанція в кілометрах
     * @param calories витрата калорій
     * @return {@link OutdoorTraining} для видів на повітрі, інакше {@link IndoorTraining}
     * @throws NullPointerException якщо {@code date} або {@code kindLabel} дорівнює {@code null}
     * @throws IllegalArgumentException якщо вид невідомий, текст порожній або число недопустиме
     */
    public static Training create(String date, String kindLabel, int minutes, double distanceKm,
            int calories) {
        TrainingKind kind = TrainingKind.fromLabel(kindLabel);
        if (kind.isOutdoor()) {
            return new OutdoorTraining(date, kind, minutes, distanceKm, calories);
        }
        return new IndoorTraining(date, kind, minutes, distanceKm, calories);
    }

    /**
     * Створює тренування з CSV-рядка.
     *
     * @param line рядок формату {@code дата;вид;хвилини;дистанція_км;калорії}
     * @return тренування відповідного підтипу
     * @throws NullPointerException якщо {@code line} дорівнює {@code null}
     * @throws IllegalArgumentException якщо структура, число або інваріант некоректні
     */
    public static Training fromCsv(String line) {
        Objects.requireNonNull(line, "Рядок не може бути null");
        if (line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        // -1 зберігає порожнє останнє поле, тому "a;b;" має три поля.
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "очікується %d полів, отримано %d".formatted(FIELD_COUNT, fields.length));
        }
        return create(
                fields[0].trim(),
                fields[1].trim(),
                parseInt(fields[2], "minutes"),
                parseDouble(fields[3], "distanceKm"),
                parseInt(fields[4], "calories"));
    }

    /* Перетворює текст на ціле число й додає назву поля до повідомлення. */
    private static int parseInt(String text, String field) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "поле %s має помилковий формат".formatted(field), exception);
        }
    }

    /* Перетворює текст на число з крапкою й додає назву поля до повідомлення. */
    private static double parseDouble(String text, String field) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "поле %s має помилковий формат".formatted(field), exception);
        }
    }

    /**
     * Обчислює навантаження тренування; формула залежить від підтипу.
     *
     * @return навантаження в умовних одиницях, {@code >= 0}
     */
    public abstract double load();

    /**
     * Повертає дату тренування.
     *
     * @return дата у форматі {@code YYYY-MM-DD}
     */
    public final String getDate() {
        return date;
    }

    /**
     * Повертає підпис виду тренування (як у CSV).
     *
     * @return український підпис, наприклад {@code Біг}
     */
    public final String getKind() {
        return kind.label();
    }

    /**
     * Повертає вид тренування як enum.
     *
     * @return константа {@link TrainingKind}
     */
    public final TrainingKind getTrainingKind() {
        return kind;
    }

    /**
     * Повертає тривалість.
     *
     * @return хвилини
     */
    public final int getMinutes() {
        return minutes;
    }

    /**
     * Повертає дистанцію.
     *
     * @return кілометри
     */
    public final double getDistanceKm() {
        return distanceKm;
    }

    /**
     * Повертає витрату калорій.
     *
     * @return калорії
     */
    public final int getCalories() {
        return calories;
    }

    /**
     * Порівнює тренування за класом і всіма п'ятьма полями.
     *
     * <p>{@code getClass()} не допускає рівності різних підтипів із однаковими полями.
     *
     * @param other інший об'єкт
     * @return {@code true}, якщо об'єкти логічно рівні
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Training training = (Training) other;
        return minutes == training.minutes
                && calories == training.calories
                && Double.compare(distanceKm, training.distanceKm) == 0
                && kind == training.kind
                && date.equals(training.date);
    }

    /**
     * Повертає хеш-код, узгоджений з {@link #equals(Object)}.
     *
     * @return хеш-код за тими самими полями
     */
    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), date, kind, minutes, distanceKm, calories);
    }

    /**
     * Повертає текстове подання для звіту; дробові числа форматуються з {@link Locale#ROOT}.
     *
     * @return рядок на кшталт {@code 2026-09-01, Біг: 45 хв, 7.50 км, 520 ккал}
     */
    @Override
    public final String toString() {
        return String.format(Locale.ROOT, "%s, %s: %d хв, %.2f км, %d ккал",
                date, kind.label(), minutes, distanceKm, calories);
    }
}
