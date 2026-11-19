package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TrainingRecordTest {
    private static final LocalDate DAY = LocalDate.of(2026, 9, 1);

    /* Повертає коректні значення стовпців для зміни окремих полів у тестах. */
    private static Map<String, String> columns() {
        Map<String, String> map = new HashMap<>();
        map.put("дата", "2026-09-01");
        map.put("вид", "RUNNING");
        map.put("тривалість", "45");
        map.put("дистанція", "7.5");
        map.put("калорії", "520");
        map.put("нотатка", "");
        return map;
    }

    /* Перевіряє конструктор і геттери. */
    @Test
    void constructorStoresFields() {
        TrainingRecord record = new TrainingRecord(DAY, TrainingKind.RUNNING, 45, 7.5, 520, "n");

        assertEquals(DAY, record.date());
        assertEquals(TrainingKind.RUNNING, record.kind());
        assertEquals(45, record.durationMinutes());
        assertEquals(7.5, record.distanceKm());
        assertEquals(520, record.calories());
        assertEquals("n", record.note());
    }

    /* Перевіряє інваріанти моделі: IllegalArgumentException / NullPointerException. */
    @Test
    void constructorRejectsInvalidValues() {
        assertThrows(NullPointerException.class,
                () -> new TrainingRecord(null, TrainingKind.YOGA, 1, 1.0, 1, ""));
        assertThrows(NullPointerException.class,
                () -> new TrainingRecord(DAY, null, 1, 1.0, 1, ""));
        assertThrows(NullPointerException.class,
                () -> new TrainingRecord(DAY, TrainingKind.YOGA, 1, 1.0, 1, null));
        assertThrows(IllegalArgumentException.class,
                () -> new TrainingRecord(DAY, TrainingKind.YOGA, -1, 1.0, 1, ""));
        assertThrows(IllegalArgumentException.class,
                () -> new TrainingRecord(DAY, TrainingKind.YOGA, 1, -0.5, 1, ""));
        assertThrows(IllegalArgumentException.class,
                () -> new TrainingRecord(DAY, TrainingKind.YOGA, 1, 1.0, -1, ""));
        assertThrows(IllegalArgumentException.class,
                () -> new TrainingRecord(DAY, TrainingKind.YOGA, 1, Double.POSITIVE_INFINITY, 1, ""));
        assertThrows(IllegalArgumentException.class,
                () -> new TrainingRecord(DAY, TrainingKind.YOGA, 1, Double.NaN, 1, ""));
    }

    /* Перевіряє equals/hashCode: кожне поле впливає на рівність. */
    @Test
    void equalsAndHashCodeUseAllFields() {
        TrainingRecord base = new TrainingRecord(DAY, TrainingKind.RUNNING, 45, 7.5, 520, "n");

        assertEquals(base, new TrainingRecord(DAY, TrainingKind.RUNNING, 45, 7.5, 520, "n"));
        assertEquals(base.hashCode(),
                new TrainingRecord(DAY, TrainingKind.RUNNING, 45, 7.5, 520, "n").hashCode());
        assertNotEquals(base, new TrainingRecord(DAY.plusDays(1), TrainingKind.RUNNING, 45, 7.5, 520, "n"));
        assertNotEquals(base, new TrainingRecord(DAY, TrainingKind.WALKING, 45, 7.5, 520, "n"));
        assertNotEquals(base, new TrainingRecord(DAY, TrainingKind.RUNNING, 46, 7.5, 520, "n"));
        assertNotEquals(base, new TrainingRecord(DAY, TrainingKind.RUNNING, 45, 7.6, 520, "n"));
        assertNotEquals(base, new TrainingRecord(DAY, TrainingKind.RUNNING, 45, 7.5, 521, "n"));
        assertNotEquals(base, new TrainingRecord(DAY, TrainingKind.RUNNING, 45, 7.5, 520, "m"));
        assertNotEquals(base, null);
    }

    /* Перевіряє зв'язок з основною моделлю: of(Training) і toTraining(). */
    @Test
    void convertsToAndFromTraining() {
        Training training = Training.create("2026-09-01", "Біг", 45, 7.5, 520);

        TrainingRecord record = TrainingRecord.of(training);

        assertEquals(new TrainingRecord(DAY, TrainingKind.RUNNING, 45, 7.5, 520, ""), record);
        assertEquals(training, record.toTraining());
    }

    /* Перевіряє успішний розбір значень стовпців. */
    @Test
    void fromColumnsParsesValidColumns() throws DataStorageException {
        TrainingRecord record = TrainingRecord.fromColumns(1, columns());

        assertEquals(new TrainingRecord(DAY, TrainingKind.RUNNING, 45, 7.5, 520, ""), record);
    }

    /* Перевіряє хибне число: повідомлення з номером запису, cause — NumberFormatException. */
    @Test
    void fromColumnsReportsBadNumberWithCause() {
        Map<String, String> bad = columns();
        bad.put("тривалість", "сорок");

        DataStorageException error = assertThrows(DataStorageException.class,
                () -> TrainingRecord.fromColumns(3, bad));

        assertEquals("Запис 3: хибне число або дата", error.getMessage());
        assertInstanceOf(NumberFormatException.class, error.getCause());
    }

    /* Перевіряє хибну дату: cause — DateTimeParseException. */
    @Test
    void fromColumnsReportsBadDateWithCause() {
        Map<String, String> bad = columns();
        bad.put("дата", "2026-13-45");

        DataStorageException error = assertThrows(DataStorageException.class,
                () -> TrainingRecord.fromColumns(2, bad));

        assertInstanceOf(DateTimeParseException.class, error.getCause());
    }

    /* Перевіряє невідомий enum і порушення інваріанта (від'ємне число). */
    @Test
    void fromColumnsReportsUnknownEnumAndInvariantViolation() {
        Map<String, String> unknownKind = columns();
        unknownKind.put("вид", "DARTS");
        DataStorageException kind = assertThrows(DataStorageException.class,
                () -> TrainingRecord.fromColumns(4, unknownKind));
        assertTrue(kind.getMessage().startsWith("Запис 4: хибне значення"));
        assertInstanceOf(IllegalArgumentException.class, kind.getCause());

        Map<String, String> negative = columns();
        negative.put("калорії", "-5");
        DataStorageException invariant = assertThrows(DataStorageException.class,
                () -> TrainingRecord.fromColumns(5, negative));
        assertEquals("Запис 5: хибне значення: від'ємне числове значення", invariant.getMessage());
    }

    /* Перевіряє відсутній стовпець. */
    @Test
    void fromColumnsReportsMissingColumn() {
        Map<String, String> missing = columns();
        missing.remove("нотатка");

        DataStorageException error = assertThrows(DataStorageException.class,
                () -> TrainingRecord.fromColumns(1, missing));

        assertEquals("Запис 1: відсутній стовпець «нотатка»", error.getMessage());
    }
}
