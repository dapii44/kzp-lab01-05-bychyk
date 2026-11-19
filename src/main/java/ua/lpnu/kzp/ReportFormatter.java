package ua.lpnu.kzp;

import java.util.List;
import java.util.Locale;

/** Формує текст звіту. Один текст виводиться і в консоль, і у файл. */
public final class ReportFormatter {
    /* Забороняє створення екземплярів службового класу. */
    private ReportFormatter() {
    }

    /**
     * Будує текст звіту за накопиченими показниками.
     *
     * @param stats накопичені показники
     * @return готовий текст звіту
     */
    public static String format(TrainingStats stats) {
        return format(stats.summary(), stats.errors());
    }

    /**
     * Будує текст звіту за незмінним підсумком і списком помилок.
     *
     * <p>Числа форматуються з {@link Locale#ROOT}, щоб десятковим роздільником завжди була
     * крапка незалежно від налаштувань Windows, macOS або Ubuntu. Формат не змінився з
     * лабораторної роботи № 1.
     *
     * @param summary підсумок із чотирьох показників
     * @param errors повідомлення про пропущені рядки
     * @return готовий текст звіту
     */
    public static String format(TrainingSummary summary, List<String> errors) {
        StringBuilder report = new StringBuilder();
        report.append(String.format(Locale.ROOT, "Звіт про тренування%n"));
        report.append(String.format(Locale.ROOT, "Коректних записів: %d%n", summary.count()));
        report.append(String.format(Locale.ROOT, "Сумарний час: %d хв%n",
                summary.totals().totalMinutes()));
        report.append(String.format(Locale.ROOT, "Сумарна дистанція: %.2f км%n",
                summary.totals().totalDistanceKm()));
        report.append(String.format(Locale.ROOT, "Найбільша витрата калорій: %d ккал%n",
                summary.maxCalories()));
        report.append(String.format(Locale.ROOT, "Помилок: %d%n", errors.size()));
        for (String error : errors) {
            report.append(error).append(String.format(Locale.ROOT, "%n"));
        }
        return report.toString();
    }
}
