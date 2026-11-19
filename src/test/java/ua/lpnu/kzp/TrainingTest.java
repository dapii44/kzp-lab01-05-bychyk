package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class TrainingTest {
    /* Перевіряє, що коректний конструктор зберігає всі поля. */
    @Test
    void constructorStoresAllFields() {
        Training training = new Training("2026-09-01", "Біг", 45, 7.5, 520);

        assertEquals("2026-09-01", training.getDate());
        assertEquals("Біг", training.getKind());
        assertEquals(45, training.getMinutes());
        assertEquals(7.5, training.getDistanceKm(), 0.0001);
        assertEquals(520, training.getCalories());
    }

    /* Перевіряє, що нуль є допустимою межею для всіх числових полів. */
    @Test
    void zeroValuesAreBoundaryAndAccepted() {
        Training training = new Training("2026-09-07", "Йога", 0, 0.0, 0);

        assertEquals(0, training.getMinutes());
        assertEquals(0.0, training.getDistanceKm(), 0.0001);
        assertEquals(0, training.getCalories());
    }

    /* Перевіряє відмінність null від порожнього тексту. */
    @Test
    void nullAndBlankTextAreRejectedDifferently() {
        assertThrows(NullPointerException.class, () -> new Training(null, "Біг", 1, 1.0, 1));
        assertThrows(NullPointerException.class, () -> new Training("2026-09-01", null, 1, 1.0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Training("", "Біг", 1, 1.0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Training("2026-09-01", "  ", 1, 1.0, 1));
    }

    /* Перевіряє відхилення від'ємних значень у кожному числовому полі. */
    @Test
    void negativeNumbersAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Training("2026-09-01", "Біг", -1, 1.0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Training("2026-09-01", "Біг", 1, -0.01, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Training("2026-09-01", "Біг", 1, 1.0, -1));
    }

    /* Перевіряє відхилення NaN і нескінченності. */
    @Test
    void nonFiniteDistanceIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Training("2026-09-01", "Біг", 1, Double.NaN, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Training("2026-09-01", "Біг", 1, Double.POSITIVE_INFINITY, 1));
    }

    /* Перевіряє створення об'єкта з коректного CSV-рядка. */
    @Test
    void fromCsvCreatesValidEntity() {
        Training training = Training.fromCsv("2026-09-01;Біг;45;7.50;520");

        assertEquals("2026-09-01", training.getDate());
        assertEquals("Біг", training.getKind());
        assertEquals(45, training.getMinutes());
        assertEquals(7.5, training.getDistanceKm(), 0.0001);
        assertEquals(520, training.getCalories());
    }

    /* Перевіряє, що фабрика обрізає пробіли навколо полів. */
    @Test
    void fromCsvTrimsFields() {
        Training training = Training.fromCsv(" 2026-09-01 ; Біг ; 45 ; 7.50 ; 520 ");

        assertEquals("2026-09-01", training.getDate());
        assertEquals("Біг", training.getKind());
    }

    /* Перевіряє відхилення неправильної кількості полів. */
    @Test
    void fromCsvRejectsWrongFieldCount() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> Training.fromCsv("2026-09-12;Біг;40;5.00"));

        assertEquals("очікується 5 полів, отримано 4", error.getMessage());
    }

    /* Перевіряє неправильне число й збереження першопричини. */
    @Test
    void fromCsvRejectsInvalidNumberAndKeepsCause() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> Training.fromCsv("2026-09-10;Біг;абв;5.00;300"));

        assertEquals("поле minutes має помилковий формат", error.getMessage());
        assertEquals(NumberFormatException.class, error.getCause().getClass());
    }

    /* Перевіряє порушення інваріанта через фабрику. */
    @Test
    void fromCsvRejectsInvariantViolation() {
        assertThrows(IllegalArgumentException.class,
                () -> Training.fromCsv("2026-09-11;Силове;-30;0.00;250"));
        assertThrows(IllegalArgumentException.class, () -> Training.fromCsv(";Біг;1;1.0;1"));
        assertThrows(IllegalArgumentException.class, () -> Training.fromCsv(""));
    }

    /* Перевіряє, що null-рядок відрізняється від порожнього. */
    @Test
    void fromCsvRejectsNull() {
        assertThrows(NullPointerException.class, () -> Training.fromCsv(null));
    }

    /* Перевіряє форматування toString із крапкою як десятковим роздільником. */
    @Test
    void toStringUsesRootLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);

            String text = new Training("2026-09-01", "Біг", 45, 7.5, 520).toString();

            assertEquals("2026-09-01, Біг: 45 хв, 7.50 км, 520 ккал", text);
        } finally {
            Locale.setDefault(previous);
        }
    }
}
