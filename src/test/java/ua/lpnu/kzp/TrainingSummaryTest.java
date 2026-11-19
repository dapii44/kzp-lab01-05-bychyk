package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class TrainingSummaryTest {
    /* Перевіряє значеннєву рівність і доступ до компонентів record. */
    @Test
    void recordUsesValueEqualityAndAccessors() {
        TrainingSummary first = new TrainingSummary(3, new DurationDistance(195, 37.9), 780);
        TrainingSummary second = new TrainingSummary(3, new DurationDistance(195, 37.9), 780);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(3, first.count());
        assertEquals(195, first.totals().totalMinutes());
        assertEquals(37.9, first.totals().totalDistanceKm(), 0.0001);
        assertEquals(780, first.maxCalories());
        assertNotEquals(first, new TrainingSummary(4, new DurationDistance(195, 37.9), 780));
    }

    /* Перевіряє, що record відхиляє від'ємні й нескінченні показники. */
    @Test
    void recordsRejectInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new DurationDistance(-1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new DurationDistance(1, -1.0));
        assertThrows(IllegalArgumentException.class,
                () -> new DurationDistance(1, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class,
                () -> new TrainingSummary(-1, new DurationDistance(0, 0), 0));
        assertThrows(IllegalArgumentException.class,
                () -> new TrainingSummary(0, new DurationDistance(0, 0), -1));
        assertThrows(NullPointerException.class, () -> new TrainingSummary(0, null, 0));
    }

    /* Перевіряє, що підсумок порожнього списку складається з нулів. */
    @Test
    void emptyListGivesZeroSummary() {
        TrainingSummary summary = TrainingAnalyzer.summarize(List.of());

        assertEquals(new TrainingSummary(0, new DurationDistance(0, 0.0), 0), summary);
    }

    /* Перевіряє обчислення чотирьох показників і незмінність вхідного списку. */
    @Test
    void analyzerComputesFourIndicatorsWithoutChangingInput() {
        List<Training> trainings = List.of(
                new Training("2026-09-01", "Біг", 45, 7.5, 520),
                new Training("2026-09-03", "Велосипед", 90, 28.4, 780),
                new Training("2026-09-05", "Плавання", 60, 2.0, 450));

        TrainingSummary summary = TrainingAnalyzer.summarize(trainings);

        assertEquals(3, summary.count());
        assertEquals(195, summary.totals().totalMinutes());
        assertEquals(37.9, summary.totals().totalDistanceKm(), 0.0001);
        assertEquals(780, summary.maxCalories());
        assertEquals(3, trainings.size());
    }
}
