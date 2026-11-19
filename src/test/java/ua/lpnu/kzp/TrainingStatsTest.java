package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TrainingStatsTest {
    /* Перевіряє, що коректний рядок потрапляє до всіх показників. */
    @Test
    void validLineUpdatesAllIndicators() {
        TrainingStats stats = new TrainingStats();

        stats.accept(1, "2026-09-01;Біг;45;7.50;520");

        assertEquals(1, stats.validCount());
        assertEquals(45, stats.totalMinutes());
        assertEquals(7.5, stats.totalDistanceKm(), 0.0001);
        assertEquals(520, stats.maxCalories());
        assertTrue(stats.errors().isEmpty());
    }

    /* Перевіряє суму, суму дистанцій і максимум для кількох записів. */
    @Test
    void severalRecordsAreAggregated() {
        TrainingStats stats = new TrainingStats();

        stats.accept(1, "2026-09-01;Біг;45;7.50;520");
        stats.accept(2, "2026-09-03;Велосипед;90;28.40;780");
        stats.accept(3, "2026-09-05;Плавання;60;2.00;450");

        assertEquals(3, stats.validCount());
        assertEquals(195, stats.totalMinutes());
        assertEquals(37.9, stats.totalDistanceKm(), 0.0001);
        assertEquals(780, stats.maxCalories());
    }

    /* Перевіряє відхилення порожнього рядка з номером рядка в повідомленні. */
    @Test
    void emptyLineIsReportedWithLineNumber() {
        TrainingStats stats = new TrainingStats();

        stats.accept(4, "");
        stats.accept(5, "   ");

        assertEquals(0, stats.validCount());
        assertEquals(List.of("Рядок 4: порожній рядок", "Рядок 5: порожній рядок"),
                stats.errors());
    }

    /* Перевіряє відхилення рядка з неправильною кількістю полів. */
    @Test
    void wrongFieldCountIsRejected() {
        TrainingStats stats = new TrainingStats();

        stats.accept(9, "2026-09-12;Біг;40;5.00");
        stats.accept(10, "2026-09-12;Біг;40;5.00;300;зайве");

        assertEquals(0, stats.validCount());
        assertEquals("Рядок 9: очікується 5 полів, отримано 4", stats.errors().get(0));
        assertEquals("Рядок 10: очікується 5 полів, отримано 6", stats.errors().get(1));
    }

    /* Перевіряє, що порожнє останнє поле не губиться під час розбору. */
    @Test
    void trailingEmptyFieldIsKept() {
        TrainingStats stats = new TrainingStats();

        stats.accept(1, "2026-09-01;Біг;45;7.50;");

        assertEquals(0, stats.validCount());
        assertEquals("Рядок 1: поле calories має помилковий формат", stats.errors().get(0));
    }

    /* Перевіряє відхилення нечислових значень у кожному числовому полі. */
    @Test
    void nonNumericFieldsAreRejected() {
        TrainingStats stats = new TrainingStats();

        stats.accept(1, "2026-09-10;Біг;абв;5.00;300");
        stats.accept(2, "2026-09-10;Біг;40;п'ять;300");
        stats.accept(3, "2026-09-10;Біг;40;5.00;багато");

        assertEquals(0, stats.validCount());
        assertEquals(List.of(
                "Рядок 1: поле minutes має помилковий формат",
                "Рядок 2: поле distanceKm має помилковий формат",
                "Рядок 3: поле calories має помилковий формат"), stats.errors());
    }

    /* Перевіряє відхилення NaN і нескінченності, які псують статистику. */
    @Test
    void nonFiniteDistanceIsRejected() {
        TrainingStats stats = new TrainingStats();

        stats.accept(1, "2026-09-10;Біг;40;NaN;300");
        stats.accept(2, "2026-09-10;Біг;40;Infinity;300");

        assertEquals(0, stats.validCount());
        assertEquals(2, stats.errors().size());
    }

    /* Перевіряє відхилення від'ємних значень, але прийняття нуля як межі. */
    @Test
    void negativeIsRejectedAndZeroIsAccepted() {
        TrainingStats stats = new TrainingStats();

        stats.accept(1, "2026-09-11;Силове;-30;0.00;250");
        stats.accept(2, "2026-09-11;Силове;30;-1.00;250");
        stats.accept(3, "2026-09-11;Силове;30;0.00;-5");
        stats.accept(4, "2026-09-11;Йога;0;0.00;0");

        assertEquals(1, stats.validCount());
        assertEquals(3, stats.errors().size());
        assertEquals("Рядок 1: від'ємне числове значення", stats.errors().get(0));
        assertEquals(0, stats.maxCalories());
    }

    /* Перевіряє відхилення порожньої дати або виду тренування. */
    @Test
    void blankDateOrKindIsRejected() {
        TrainingStats stats = new TrainingStats();

        stats.accept(1, ";Біг;45;7.50;520");
        stats.accept(2, "2026-09-01; ;45;7.50;520");

        assertEquals(0, stats.validCount());
        assertEquals("Рядок 1: порожня дата або вид тренування", stats.errors().get(0));
        assertEquals("Рядок 2: порожня дата або вид тренування", stats.errors().get(1));
    }

    /* Перевіряє, що пробіли навколо чисел не заважають розбору. */
    @Test
    void spacesAroundNumbersAreTrimmed() {
        TrainingStats stats = new TrainingStats();

        stats.accept(1, "2026-09-01;Біг; 45 ; 7.50 ; 520 ");

        assertEquals(1, stats.validCount());
        assertEquals(45, stats.totalMinutes());
    }

    /* Перевіряє, що хибний рядок не змінює вже накопичених показників. */
    @Test
    void invalidLineDoesNotChangeIndicators() {
        TrainingStats stats = new TrainingStats();
        stats.accept(1, "2026-09-01;Біг;45;7.50;520");

        stats.accept(2, "2026-09-02;Біг;100;99.00;9999;зайве");
        stats.accept(3, "2026-09-02;Біг;-1;99.00;9999");

        assertEquals(1, stats.validCount());
        assertEquals(45, stats.totalMinutes());
        assertEquals(520, stats.maxCalories());
    }

    /*
     * Перевіряє роботу з українськими символами у виді тренування. Лабораторна № 3: вид тепер
     * належить закритій множині enum, тому використано відомий вид у різному регістрі.
     */
    @Test
    void ukrainianTextIsAccepted() {
        TrainingStats stats = new TrainingStats();

        stats.accept(1, "2026-09-15;Біг;30;2.50;150");
        stats.accept(2, "2026-09-16;БІГ;30;2.50;150");

        assertEquals(2, stats.validCount());
    }

    /* Перевіряє відхилення виду, якого немає в enum (лабораторна № 3). */
    @Test
    void unknownKindIsRejected() {
        TrainingStats stats = new TrainingStats();

        stats.accept(1, "2026-09-15;Дартс;30;0.00;100");

        assertEquals(0, stats.validCount());
        assertEquals("Рядок 1: невідомий вид тренування: Дартс", stats.errors().get(0));
    }

    /* Перевіряє, що повернений список помилок є копією й не змінює стан. */
    @Test
    void errorsListIsDefensiveCopy() {
        TrainingStats stats = new TrainingStats();
        stats.accept(1, "");

        List<String> errors = stats.errors();

        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> errors.add("зайве"));
        assertEquals(1, stats.errors().size());
    }
}
