package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;

/**
 * Накопичує показники тренувань за рядками CSV.
 *
 * <p>Лабораторна робота № 1: запис існує лише як рядок і набір локальних змінних.
 * Формат рядка: {@code дата;вид;хвилини;дистанція_км;калорії}.
 */
public final class TrainingStats {
    private static final int FIELD_COUNT = 5;

    private int validCount;
    private long totalMinutes;
    private double totalDistanceKm;
    private int maxCalories;
    private final List<String> errors = new ArrayList<>();

    /**
     * Перевіряє один рядок і, якщо він коректний, додає його до показників.
     *
     * <p>Хибний рядок не змінює показників і не перериває обробку: причина разом із номером
     * рядка зберігається в списку помилок.
     *
     * @param lineNumber номер рядка у файлі, починаючи з 1
     * @param line текст рядка
     */
    public void accept(int lineNumber, String line) {
        int minutes;
        double distanceKm;
        int calories;
        try {
            if (line == null || line.isBlank()) {
                throw new IllegalArgumentException("порожній рядок");
            }
            // -1 зберігає порожнє останнє поле, тому "a;b;" має три поля.
            String[] fields = line.split(";", -1);
            if (fields.length != FIELD_COUNT) {
                throw new IllegalArgumentException(
                        "очікується %d полів, отримано %d".formatted(FIELD_COUNT, fields.length));
            }
            if (fields[0].isBlank() || fields[1].isBlank()) {
                throw new IllegalArgumentException("порожня дата або вид тренування");
            }
            minutes = parseInt(fields[2], "minutes");
            distanceKm = parseDouble(fields[3], "distanceKm");
            calories = parseInt(fields[4], "calories");
            if (minutes < 0 || distanceKm < 0 || calories < 0) {
                throw new IllegalArgumentException("від'ємне числове значення");
            }
        } catch (IllegalArgumentException exception) {
            errors.add("Рядок %d: %s".formatted(lineNumber, exception.getMessage()));
            return;
        }
        // До статистики потрапляють лише записи, які пройшли всі перевірки.
        validCount++;
        totalMinutes += minutes;
        totalDistanceKm += distanceKm;
        maxCalories = Math.max(maxCalories, calories);
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

    /* Перетворює текст на скінченне число з крапкою й додає назву поля до повідомлення. */
    private static double parseDouble(String text, String field) {
        try {
            double value = Double.parseDouble(text.trim());
            if (!Double.isFinite(value)) {
                throw new NumberFormatException("не є скінченним числом");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "поле %s має помилковий формат".formatted(field), exception);
        }
    }

    /**
     * Повертає кількість коректних записів.
     *
     * @return кількість записів, що пройшли перевірку
     */
    public int validCount() {
        return validCount;
    }

    /**
     * Повертає сумарний час тренувань.
     *
     * @return сума хвилин
     */
    public long totalMinutes() {
        return totalMinutes;
    }

    /**
     * Повертає сумарну дистанцію.
     *
     * @return сума кілометрів
     */
    public double totalDistanceKm() {
        return totalDistanceKm;
    }

    /**
     * Повертає найбільшу витрату калорій.
     *
     * @return максимум калорій або 0, якщо коректних записів немає
     */
    public int maxCalories() {
        return maxCalories;
    }

    /**
     * Повертає копію списку повідомлень про пропущені рядки.
     *
     * @return повідомлення у порядку рядків файла
     */
    public List<String> errors() {
        return List.copyOf(errors);
    }
}
