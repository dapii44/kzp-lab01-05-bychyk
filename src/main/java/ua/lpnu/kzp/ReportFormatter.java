package ua.lpnu.kzp;

import java.time.LocalDate;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Будує додатковий блок звіту з навантаженням кожного тренування (режим {@code --details}).
     *
     * <p>Кожен рядок обчислюється поліморфним викликом {@code load()}; тип тренування лише
     * відображається, а не використовується для розгалуження.
     *
     * @param trainings коректні тренування
     * @return текст додаткового блоку
     */
    public static String formatDetails(List<Training> trainings) {
        StringBuilder block = new StringBuilder();
        block.append(String.format(Locale.ROOT, "%nНавантаження за тренуваннями:%n"));
        for (Training training : trainings) {
            block.append(String.format(Locale.ROOT, "%s [%s] -> %.2f%n", training,
                    training.getTrainingKind().isOutdoor() ? "на повітрі" : "у приміщенні",
                    training.load()));
        }
        block.append(String.format(Locale.ROOT, "Загальне навантаження: %.2f%n",
                TrainingAnalyzer.totalLoad(trainings)));
        block.append(String.format(Locale.ROOT, "Унікальних тренувань: %d%n",
                new java.util.HashSet<>(trainings).size()));
        return block.toString();
    }

    /**
     * Будує блок результатів потокових запитів (режим {@code --queries}).
     *
     * @param trainings коректні тренування
     * @param searchDate дата для Optional-пошуку
     * @return текст додаткового блоку
     */
    public static String formatQueries(List<Training> trainings, LocalDate searchDate) {
        StringBuilder block = new StringBuilder();
        block.append(String.format(Locale.ROOT, "%nЗапити Stream API:%n"));
        block.append(String.format(Locale.ROOT, "Тривалість не менша %d хв: %d%n",
                TrainingQueries.MIN_MINUTES, TrainingQueries.longTrainings(trainings).size()));
        block.append(String.format(Locale.ROOT, "Дистанції, км: %s%n",
                TrainingQueries.distances(trainings).stream()
                        .map(value -> String.format(Locale.ROOT, "%.2f", value))
                        .collect(Collectors.joining(", ", "[", "]"))));
        Map<TrainingKind, Double> byKind = TrainingQueries.distanceByKind(trainings);
        block.append(String.format(Locale.ROOT, "Дистанція за видом, км: %s%n",
                byKind.entrySet().stream()
                        .map(entry -> String.format(Locale.ROOT, "%s=%.2f",
                                entry.getKey().label(), entry.getValue()))
                        .collect(Collectors.joining(", "))));
        IntSummaryStatistics calories = TrainingQueries.caloriesStatistics(trainings);
        block.append(String.format(Locale.ROOT,
                "Калорії: кількість=%d, мін=%d, макс=%d, середнє=%.2f%n",
                calories.getCount(), calories.getCount() == 0 ? 0 : calories.getMin(),
                calories.getCount() == 0 ? 0 : calories.getMax(), calories.getAverage()));
        block.append(String.format(Locale.ROOT, "Топ-%d за дистанцією:%n", TrainingQueries.TOP_N));
        for (Training training : TrainingQueries.topByDistance(trainings, TrainingQueries.TOP_N)) {
            block.append(String.format(Locale.ROOT, "  %s%n", training));
        }
        block.append(String.format(Locale.ROOT, "Пошук за датою %s: %s%n", searchDate,
                TrainingQueries.findByDate(trainings, searchDate)
                        .map(Training::toString).orElse("не знайдено")));
        return block.toString();
    }
}
