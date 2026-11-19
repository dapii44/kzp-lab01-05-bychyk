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
        // Лабораторна № 4: цикл замінено на потокові термінальні операції (по одній на показник).
        long totalMinutes = trainings.stream().mapToLong(Training::getMinutes).sum();
        double totalDistanceKm = trainings.stream().mapToDouble(Training::getDistanceKm).sum();
        int maxCalories = trainings.stream().mapToInt(Training::getCalories).max().orElse(0);
        return new TrainingSummary(trainings.size(),
                new DurationDistance(totalMinutes, totalDistanceKm), maxCalories);
    }

    /**
     * Обчислює сумарне навантаження через поліморфний метод {@link Training#load()}.
     *
     * <p>Метод не перевіряє конкретний підтип: для кожного елемента викликається реалізація його
     * фактичного класу, тому додавання нового підтипу не потребує змін тут.
     *
     * @param trainings тренування будь-яких підтипів
     * @return сума навантажень; 0 для порожнього списку
     */
    public static double totalLoad(List<? extends Training> trainings) {
        return trainings.stream().mapToDouble(Training::load).sum();
    }
}
