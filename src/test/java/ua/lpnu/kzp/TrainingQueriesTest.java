package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrainingQueriesTest {
    private List<Training> sample;

    /* Завантажує 8 коректних записів двох підтипів, серед яких межа 45/44 хв і дубль дати. */
    @BeforeEach
    void loadSample() throws IOException {
        sample = Files.readAllLines(Path.of("data", "queries-sample.csv"), StandardCharsets.UTF_8)
                .stream()
                .map(Training::fromCsv)
                .toList();
    }

    /* Еталонна реалізація лабораторної № 3 з циклом — для порівняння із Stream API. */
    private static TrainingSummary loopSummarize(List<Training> trainings) {
        long minutes = 0;
        double distance = 0.0;
        int maxCalories = 0;
        for (Training training : trainings) {
            minutes += training.getMinutes();
            distance += training.getDistanceKm();
            maxCalories = Math.max(maxCalories, training.getCalories());
        }
        return new TrainingSummary(trainings.size(), new DurationDistance(minutes, distance),
                maxCalories);
    }

    /* Перевіряє, що вибірка містить обидва підтипи, тобто запити працюють поліморфно. */
    @Test
    void sampleContainsBothSubtypes() {
        assertEquals(8, sample.size());
        assertTrue(sample.stream().anyMatch(t -> t instanceof OutdoorTraining));
        assertTrue(sample.stream().anyMatch(t -> t instanceof IndoorTraining));
    }

    /* Запит 1: поріг 45 хв включно, 44 хв відкидається, початковий порядок збережено. */
    @Test
    void filterKeepsThresholdAndDropsShorter() {
        List<Training> result = TrainingQueries.longTrainings(sample);

        assertEquals(6, result.size());
        assertEquals(List.of("2026-09-01", "2026-09-03", "2026-09-04", "2026-09-05",
                "2026-09-05", "2026-09-06"), result.stream().map(Training::getDate).toList());
        assertTrue(result.stream().allMatch(t -> t.getMinutes() >= TrainingQueries.MIN_MINUTES));
    }

    /* Запит 2: map повертає дистанції в початковому порядку. */
    @Test
    void mapReturnsDistancesInOrder() {
        assertEquals(List.of(7.5, 30.0, 28.4, 2.0, 0.0, 5.6, 10.0, 0.0),
                TrainingQueries.distances(sample));
    }

    /* Запит 3: сума дистанції за видом; ключ — enum, порядок ключів — порядок констант. */
    @Test
    void groupingSumsDistanceByKindInEnumOrder() {
        Map<TrainingKind, Double> result = TrainingQueries.distanceByKind(sample);

        assertInstanceOf(EnumMap.class, result);
        assertEquals(List.of(TrainingKind.RUNNING, TrainingKind.CYCLING, TrainingKind.WALKING,
                TrainingKind.SWIMMING, TrainingKind.YOGA, TrainingKind.STRENGTH),
                new ArrayList<>(result.keySet()));
        assertEquals(17.5, result.get(TrainingKind.RUNNING), 0.0001);
        assertEquals(58.4, result.get(TrainingKind.CYCLING), 0.0001);
        assertEquals(5.6, result.get(TrainingKind.WALKING), 0.0001);
        assertEquals(2.0, result.get(TrainingKind.SWIMMING), 0.0001);
        assertEquals(0.0, result.get(TrainingKind.YOGA), 0.0001);
    }

    /* Запит 3: види без тренувань у результат не потрапляють. */
    @Test
    void groupingOmitsKindsWithoutTrainings() {
        Map<TrainingKind, Double> result = TrainingQueries.distanceByKind(
                List.of(Training.fromCsv("2026-09-01;Біг;45;7.50;520")));

        assertEquals(1, result.size());
        assertFalse(result.containsKey(TrainingKind.YOGA));
    }

    /* Запит 4: кількість, мінімум, максимум, сума, середнє калорій. */
    @Test
    void statisticsCoverCountMinMaxSumAverage() {
        IntSummaryStatistics stats = TrainingQueries.caloriesStatistics(sample);

        assertEquals(8, stats.getCount());
        assertEquals(180, stats.getMin());
        assertEquals(780, stats.getMax());
        assertEquals(3740, stats.getSum());
        assertEquals(467.5, stats.getAverage(), 0.0001);
    }

    /* Запит 5: top-5 за дистанцією спаданням; з 8 записів повертається рівно 5. */
    @Test
    void topNTruncatesToFiveByDistanceDescending() {
        List<Training> top = TrainingQueries.topByDistance(sample, TrainingQueries.TOP_N);

        assertEquals(5, top.size());
        assertEquals(List.of(30.0, 28.4, 10.0, 7.5, 5.6),
                top.stream().map(Training::getDistanceKm).toList());
    }

    /* Запит 5: за рівної дистанції другорядний критерій — дата за зростанням. */
    @Test
    void topNBreaksTiesByDateAscending() {
        List<Training> input = List.of(
                Training.fromCsv("2026-09-09;Біг;50;10.00;400"),
                Training.fromCsv("2026-09-02;Біг;50;10.00;400"),
                Training.fromCsv("2026-09-05;Біг;50;10.00;400"),
                Training.fromCsv("2026-09-01;Ходьба;50;12.00;400"));

        List<Training> top = TrainingQueries.topByDistance(input, 4);

        assertEquals(List.of("2026-09-01", "2026-09-02", "2026-09-05", "2026-09-09"),
                top.stream().map(Training::getDate).toList());
    }

    /* Запит 5: N = 0 дає порожній результат, завеликий N — усі елементи, від'ємний — помилка. */
    @Test
    void topNHandlesZeroLargeAndNegativeN() {
        assertTrue(TrainingQueries.topByDistance(sample, 0).isEmpty());
        assertEquals(8, TrainingQueries.topByDistance(sample, 100).size());
        assertEquals(1, TrainingQueries.topByDistance(sample, 1).size());
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> TrainingQueries.topByDistance(sample, -1));
        assertEquals("N не може бути від'ємним: -1", error.getMessage());
    }

    /* Пошук: знайдено — Optional із першим записом; дубль дати повертає перший у порядку входу. */
    @Test
    void findReturnsFirstMatchForDuplicateDate() {
        Optional<Training> found = TrainingQueries.findByDate(sample, LocalDate.of(2026, 9, 5));

        assertTrue(found.isPresent());
        assertEquals(TrainingKind.YOGA, found.get().getTrainingKind());
        assertSame(sample.get(4), found.get());
    }

    /* Пошук: відсутня дата дає порожній Optional, а не null чи виняток. */
    @Test
    void findReturnsEmptyOptionalWhenMissing() {
        assertEquals(Optional.empty(),
                TrainingQueries.findByDate(sample, LocalDate.of(2026, 12, 31)));
    }

    /* Порожній вхід: кожен запит повертає «порожнє» значення. */
    @Test
    void emptyInputGivesEmptyResults() {
        List<Training> empty = List.of();

        assertTrue(TrainingQueries.longTrainings(empty).isEmpty());
        assertTrue(TrainingQueries.distances(empty).isEmpty());
        assertTrue(TrainingQueries.distanceByKind(empty).isEmpty());
        assertEquals(0, TrainingQueries.caloriesStatistics(empty).getCount());
        assertTrue(TrainingQueries.topByDistance(empty, 5).isEmpty());
        assertTrue(TrainingQueries.findByDate(empty, LocalDate.of(2026, 9, 1)).isEmpty());
        assertEquals(0.0, TrainingAnalyzer.totalLoad(empty), 0.0);
    }

    /* null замість списку або дати відхиляється з NullPointerException. */
    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> TrainingQueries.longTrainings(null));
        assertThrows(NullPointerException.class, () -> TrainingQueries.distances(null));
        assertThrows(NullPointerException.class, () -> TrainingQueries.distanceByKind(null));
        assertThrows(NullPointerException.class, () -> TrainingQueries.caloriesStatistics(null));
        assertThrows(NullPointerException.class, () -> TrainingQueries.topByDistance(null, 5));
        assertThrows(NullPointerException.class, () -> TrainingQueries.findByDate(null,
                LocalDate.of(2026, 9, 1)));
        assertThrows(NullPointerException.class, () -> TrainingQueries.findByDate(sample, null));
    }

    /* Запити не змінюють вхідну колекцію, а результати є незмінними списками. */
    @Test
    void queriesDoNotMutateInputAndReturnImmutableLists() {
        List<Training> before = new ArrayList<>(sample);
        List<Training> mutableInput = new ArrayList<>(sample);

        TrainingQueries.longTrainings(mutableInput);
        TrainingQueries.distances(mutableInput);
        TrainingQueries.distanceByKind(mutableInput);
        TrainingQueries.caloriesStatistics(mutableInput);
        TrainingQueries.topByDistance(mutableInput, 5);
        TrainingQueries.findByDate(mutableInput, LocalDate.of(2026, 9, 5));

        assertEquals(before, mutableInput);
        assertThrows(UnsupportedOperationException.class,
                () -> TrainingQueries.longTrainings(sample).add(sample.get(0)));
        assertThrows(UnsupportedOperationException.class,
                () -> TrainingQueries.topByDistance(sample, 3).clear());
    }

    /* Stream API дає той самий підсумок, що й цикл лабораторної № 3, на двох наборах. */
    @Test
    void streamSummaryEqualsLoopSummary() throws IOException {
        List<Training> control = Files.readAllLines(Path.of("data", "input.csv"),
                StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .map(line -> {
                    try {
                        return Training.fromCsv(line);
                    } catch (IllegalArgumentException exception) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        for (List<Training> data : List.of(sample, control, List.<Training>of())) {
            TrainingSummary expected = loopSummarize(data);
            TrainingSummary actual = TrainingAnalyzer.summarize(data);
            assertEquals(expected.count(), actual.count());
            assertEquals(expected.maxCalories(), actual.maxCalories());
            assertEquals(expected.totals().totalMinutes(), actual.totals().totalMinutes());
            assertEquals(expected.totals().totalDistanceKm(),
                    actual.totals().totalDistanceKm(), 1e-9);
        }
        assertEquals(5, control.size());
    }

    /* Дата тепер LocalDate: хибна календарна дата відхиляється з причиною. */
    @Test
    void invalidCalendarDateIsRejectedWithCause() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> Training.fromCsv("2026-13-45;Біг;45;7.50;520"));

        assertEquals("поле date має помилковий формат", error.getMessage());
        assertInstanceOf(java.time.format.DateTimeParseException.class, error.getCause());
        assertEquals(LocalDate.of(2026, 9, 1),
                Training.fromCsv("2026-09-01;Біг;45;7.50;520").getLocalDate());
    }
}
