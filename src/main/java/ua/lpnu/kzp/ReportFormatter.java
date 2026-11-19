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
     * <p>Числа форматуються з {@link Locale#ROOT}, щоб десятковим роздільником завжди була
     * крапка незалежно від налаштувань Windows, macOS або Ubuntu.
     *
     * @param stats накопичені показники
     * @return готовий текст звіту
     */
    public static String format(TrainingStats stats) {
        StringBuilder report = new StringBuilder();
        report.append(String.format(Locale.ROOT, "Звіт про тренування%n"));
        report.append(String.format(Locale.ROOT, "Коректних записів: %d%n", stats.validCount()));
        report.append(String.format(Locale.ROOT, "Сумарний час: %d хв%n", stats.totalMinutes()));
        report.append(String.format(Locale.ROOT, "Сумарна дистанція: %.2f км%n",
                stats.totalDistanceKm()));
        report.append(String.format(Locale.ROOT, "Найбільша витрата калорій: %d ккал%n",
                stats.maxCalories()));
        List<String> errors = stats.errors();
        report.append(String.format(Locale.ROOT, "Помилок: %d%n", errors.size()));
        for (String error : errors) {
            report.append(error).append(String.format(Locale.ROOT, "%n"));
        }
        return report.toString();
    }
}
