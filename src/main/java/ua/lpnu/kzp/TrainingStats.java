package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;

/**
 * Накопичує коректні тренування та повідомлення про пропущені рядки.
 *
 * <p>Лабораторна робота № 2: розбір і перевірку виконує {@link Training#fromCsv(String)}, а
 * показники обчислює {@link TrainingAnalyzer}. Публічний інтерфейс класу збережено з
 * лабораторної роботи № 1, тому попередні тести проходять без змін.
 */
public final class TrainingStats {
    private final List<Training> trainings = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    /**
     * Перевіряє один рядок і, якщо він коректний, додає тренування до списку.
     *
     * <p>Хибний рядок не потрапляє до списку, не змінює показників і не перериває обробку: причина
     * разом із номером рядка зберігається в списку помилок.
     *
     * @param lineNumber номер рядка у файлі, починаючи з 1
     * @param line текст рядка
     */
    public void accept(int lineNumber, String line) {
        if (line == null) {
            errors.add("Рядок %d: порожній рядок".formatted(lineNumber));
            return;
        }
        try {
            // Перевірка виконується до додавання в список і до зміни статистики.
            trainings.add(Training.fromCsv(line));
        } catch (IllegalArgumentException exception) {
            errors.add("Рядок %d: %s".formatted(lineNumber, exception.getMessage()));
        }
    }

    /**
     * Повертає незмінний підсумок за накопиченими тренуваннями.
     *
     * @return кількість, сумарний час і дистанція, найбільша витрата калорій
     */
    public TrainingSummary summary() {
        return TrainingAnalyzer.summarize(trainings);
    }

    /**
     * Повертає копію списку коректних тренувань у порядку рядків файла.
     *
     * @return тренування, що пройшли перевірку
     */
    public List<Training> trainings() {
        return List.copyOf(trainings);
    }

    /**
     * Повертає кількість коректних записів.
     *
     * @return кількість записів, що пройшли перевірку
     */
    public int validCount() {
        return summary().count();
    }

    /**
     * Повертає сумарний час тренувань.
     *
     * @return сума хвилин
     */
    public long totalMinutes() {
        return summary().totals().totalMinutes();
    }

    /**
     * Повертає сумарну дистанцію.
     *
     * @return сума кілометрів
     */
    public double totalDistanceKm() {
        return summary().totals().totalDistanceKm();
    }

    /**
     * Повертає найбільшу витрату калорій.
     *
     * @return максимум калорій або 0, якщо коректних записів немає
     */
    public int maxCalories() {
        return summary().maxCalories();
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
