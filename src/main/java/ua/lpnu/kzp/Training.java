package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Описує одне коректне тренування.
 *
 * <p>Лабораторна робота № 2: рядок CSV і паралельні змінні замінено об'єктом. Стан приватний і
 * незмінний (private final, без сеттерів); усі інваріанти перевіряє конструктор, тому
 * некоректний об'єкт не може потрапити до списку або статистики.
 */
public final class Training {
    private static final int FIELD_COUNT = 5;

    private final String date;
    private final String kind;
    private final int minutes;
    private final double distanceKm;
    private final int calories;

    /**
     * Створює коректне тренування.
     *
     * @param date дата у форматі {@code YYYY-MM-DD}; не {@code null} і не порожня
     * @param kind вид тренування; не {@code null} і не порожній
     * @param minutes тривалість у хвилинах, {@code >= 0}
     * @param distanceKm дистанція в кілометрах, скінченна і {@code >= 0}
     * @param calories витрата калорій, {@code >= 0}
     * @throws NullPointerException якщо {@code date} або {@code kind} дорівнює {@code null}
     * @throws IllegalArgumentException якщо текст порожній або число недопустиме
     */
    public Training(String date, String kind, int minutes, double distanceKm, int calories) {
        // null, порожній текст і недопустимі числа мають різні причини відхилення.
        this.date = Objects.requireNonNull(date, "Дата не може бути null");
        this.kind = Objects.requireNonNull(kind, "Вид тренування не може бути null");
        if (date.isBlank() || kind.isBlank()) {
            throw new IllegalArgumentException("порожня дата або вид тренування");
        }
        if (minutes < 0 || distanceKm < 0 || calories < 0) {
            throw new IllegalArgumentException("від'ємне числове значення");
        }
        // NaN не менший за нуль, тому окрема перевірка скінченності обов'язкова.
        if (!Double.isFinite(distanceKm)) {
            throw new IllegalArgumentException("поле distanceKm має бути скінченним числом");
        }
        this.minutes = minutes;
        this.distanceKm = distanceKm;
        this.calories = calories;
    }

    /**
     * Створює тренування з CSV-рядка.
     *
     * <p>Фабрика відповідає за структуру рядка й типи полів, конструктор — за інваріанти;
     * тому правила не можна обійти іншим способом створення об'єкта.
     *
     * @param line рядок формату {@code дата;вид;хвилини;дистанція_км;калорії}
     * @return створене тренування
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
        return new Training(
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
     * Повертає дату тренування.
     *
     * @return дата у форматі {@code YYYY-MM-DD}
     */
    public String getDate() {
        return date;
    }

    /**
     * Повертає вид тренування.
     *
     * @return вид тренування
     */
    public String getKind() {
        return kind;
    }

    /**
     * Повертає тривалість.
     *
     * @return хвилини
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Повертає дистанцію.
     *
     * @return кілометри
     */
    public double getDistanceKm() {
        return distanceKm;
    }

    /**
     * Повертає витрату калорій.
     *
     * @return калорії
     */
    public int getCalories() {
        return calories;
    }

    /**
     * Повертає текстове подання для звіту; дробові числа форматуються з {@link Locale#ROOT}.
     *
     * @return рядок на кшталт {@code 2026-09-01, Біг: 45 хв, 7.50 км, 520 ккал}
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s, %s: %d хв, %.2f км, %d ккал",
                date, kind, minutes, distanceKm, calories);
    }
}
