package ua.lpnu.kzp;

/** Тренування в приміщенні (басейн, зал, йога): дистанція не впливає на навантаження. */
public final class IndoorTraining extends Training {
    private static final double CALORIE_WEIGHT = 0.05;

    /**
     * Створює тренування в приміщенні.
     *
     * @param date дата {@code YYYY-MM-DD}
     * @param kind вид тренування; має бути видом у приміщенні
     * @param minutes тривалість у хвилинах
     * @param distanceKm дистанція в кілометрах
     * @param calories витрата калорій
     * @throws IllegalArgumentException якщо вид тренування відбувається на відкритому повітрі
     */
    public IndoorTraining(String date, TrainingKind kind, int minutes, double distanceKm,
            int calories) {
        super(date, kind, minutes, distanceKm, calories);
        // Спільні перевірки виконав базовий клас; тут лише правило підтипу.
        if (kind.isOutdoor()) {
            throw new IllegalArgumentException(
                    "IndoorTraining не підтримує вид на відкритому повітрі: " + kind.label());
        }
    }

    /**
     * Обчислює навантаження: {@code хвилини × інтенсивність + калорії × 0.05}.
     *
     * @return навантаження в умовних одиницях
     */
    @Override
    public double load() {
        return getMinutes() * getTrainingKind().intensity() + getCalories() * CALORIE_WEIGHT;
    }
}
