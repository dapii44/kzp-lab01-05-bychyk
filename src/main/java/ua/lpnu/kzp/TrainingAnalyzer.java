package ua.lpnu.kzp;

import java.util.List;

/** Обчислює показники за списком об'єктів {@link Training}. */
public final class TrainingAnalyzer {
    /* Забороняє створення екземплярів службового класу. */
    private TrainingAnalyzer() {
    }

    /**
     * Обчислює чотири показники варіанта 2: кількість, сумарний час, сумарну дистанцію й
     * найбільшу витрату калорій. Вхідний список не змінюється.
     *
     * @param trainings коректні тренування
     * @return незмінний підсумок; для порожнього списку — нулі
     */
    public static TrainingSummary summarize(List<Training> trainings) {
        long totalMinutes = 0;
        double totalDistanceKm = 0.0;
        int maxCalories = 0;
        for (Training training : trainings) {
            totalMinutes += training.getMinutes();
            totalDistanceKm += training.getDistanceKm();
            maxCalories = Math.max(maxCalories, training.getCalories());
        }
        return new TrainingSummary(trainings.size(),
                new DurationDistance(totalMinutes, totalDistanceKm), maxCalories);
    }
}
