package ua.lpnu.kzp;

import java.util.Objects;

/**
 * Незмінний набір чотирьох показників варіанта 2.
 *
 * @param count кількість коректних записів, {@code >= 0}
 * @param totals сумарний час і сумарна дистанція
 * @param maxCalories найбільша витрата калорій, {@code >= 0} (0, якщо записів немає)
 */
public record TrainingSummary(int count, DurationDistance totals, int maxCalories) {
    /** Перевіряє допустимість показників. */
    public TrainingSummary {
        Objects.requireNonNull(totals, "Підсумки часу й дистанції не можуть бути null");
        if (count < 0 || maxCalories < 0) {
            throw new IllegalArgumentException("Показники не можуть бути від'ємними");
        }
    }
}
