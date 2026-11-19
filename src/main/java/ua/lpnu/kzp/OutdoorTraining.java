package ua.lpnu.kzp;

/** Тренування на відкритому повітрі (біг, велосипед, ходьба): враховує дистанцію. */
public final class OutdoorTraining extends Training {
    private static final double DISTANCE_WEIGHT = 5.0;
    private static final double CALORIE_WEIGHT = 0.05;

    /**
     * Створює тренування на відкритому повітрі.
     *
     * @param date дата {@code YYYY-MM-DD}
     * @param kind вид тренування; має бути видом на відкритому повітрі
     * @param minutes тривалість у хвилинах
     * @param distanceKm дистанція в кілометрах
     * @param calories витрата калорій
     * @throws IllegalArgumentException якщо вид тренування відбувається в приміщенні
     */
    public OutdoorTraining(String date, TrainingKind kind, int minutes, double distanceKm,
            int calories) {
        super(date, kind, minutes, distanceKm, calories);
        if (!kind.isOutdoor()) {
            throw new IllegalArgumentException(
                    "OutdoorTraining не підтримує вид у приміщенні: " + kind.label());
        }
    }

    /**
     * Обчислює навантаження: {@code хвилини × інтенсивність + км × 5 + калорії × 0.05}.
     *
     * @return навантаження в умовних одиницях
     */
    @Override
    public double load() {
        return getMinutes() * getTrainingKind().intensity()
                + getDistanceKm() * DISTANCE_WEIGHT
                + getCalories() * CALORIE_WEIGHT;
    }
}
