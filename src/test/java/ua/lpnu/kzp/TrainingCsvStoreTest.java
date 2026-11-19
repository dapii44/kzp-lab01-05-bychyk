package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TrainingCsvStoreTest {
    private static final Path CSV_DIR = Path.of("src", "test", "resources", "csv");

    /* Будує 6 записів: з комою, лапками, перенесенням рядка та порожньою нотаткою; ключ date+kind. */
    private static Repository<TrainingRecord> sample() {
        Repository<TrainingRecord> repository = new Repository<>();
        repository.add(rec("2026-09-01", TrainingKind.RUNNING, 45, 7.5, 520, "Ранкова пробіжка"));
        repository.add(rec("2026-09-03", TrainingKind.CYCLING, 90, 28.4, 780, "Маршрут: парк, річка"));
        repository.add(rec("2026-09-05", TrainingKind.SWIMMING, 60, 2.0, 450,
                "Тренер сказав: \"ще круг\""));
        repository.add(rec("2026-09-07", TrainingKind.YOGA, 50, 0.0, 180, "Розтяжка\nдихання"));
        repository.add(rec("2026-09-09", TrainingKind.WALKING, 70, 5.6, 300, ""));
        repository.add(rec("2026-09-09", TrainingKind.RUNNING, 30, 4.2, 280, "інтервали, 6x400 м"));
        return repository;
    }

    private static TrainingRecord rec(String date, TrainingKind kind, int minutes, double km,
            int calories, String note) {
        return new TrainingRecord(LocalDate.parse(date), kind, minutes, km, calories, note);
    }

    private static String golden(String name) throws IOException {
        return Files.readString(CSV_DIR.resolve(name), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    /* Експорт збігається з еталонним CSV: заголовок, екранування, порядок стовпців. */
    @Test
    void exportMatchesGoldenCsv(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("records.csv");

        TrainingCsvStore.save(file, sample());

        assertEquals(golden("records-sample.csv"), Files.readString(file, StandardCharsets.UTF_8));
    }

    /* Round-trip: об'єкт → CSV → об'єкт повертає рівні об'єкти в тому самому порядку. */
    @Test
    void roundTripRestoresEqualObjects(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("records.csv");
        Repository<TrainingRecord> original = sample();

        TrainingCsvStore.save(file, original);
        Repository<TrainingRecord> restored = TrainingCsvStore.load(file);

        assertEquals(original.all(), restored.all());
        assertEquals(6, restored.size());
    }

    /* Кількість фізичних рядків (8) відрізняється від кількості записів (6) через multiline-поле. */
    @Test
    void physicalLinesDifferFromRecords(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("records.csv");
        TrainingCsvStore.save(file, sample());

        List<String> physicalLines = Files.readAllLines(file, StandardCharsets.UTF_8);
        Repository<TrainingRecord> restored = TrainingCsvStore.load(file);

        assertEquals(8, physicalLines.size());
        assertEquals(1 + 6, CsvParser.parseDocument(Files.readString(file)).size());
        assertEquals(6, restored.size());
    }

    /* Відновлення за ключем date+kind: два записи однієї дати розрізняються видом. */
    @Test
    void restoredRecordIsFoundByDateAndKind() throws Exception {
        Repository<TrainingRecord> restored = TrainingCsvStore.parse(golden("records-sample.csv"));

        Optional<TrainingRecord> found = restored.find(r ->
                r.date().equals(LocalDate.of(2026, 9, 9)) && r.kind() == TrainingKind.RUNNING)
                .stream().findFirst();

        assertEquals(Optional.of(rec("2026-09-09", TrainingKind.RUNNING, 30, 4.2, 280,
                "інтервали, 6x400 м")), found);
        assertEquals(2, restored.find(r -> r.date().equals(LocalDate.of(2026, 9, 9))).size());
    }

    /* Спеціальні символи пройшли шлях туди й назад: кома, лапки, перенесення рядка. */
    @Test
    void specialCharactersSurviveRoundTrip() throws Exception {
        List<TrainingRecord> restored = TrainingCsvStore.parse(golden("records-sample.csv")).all();

        assertEquals("Маршрут: парк, річка", restored.get(1).note());
        assertEquals("Тренер сказав: \"ще круг\"", restored.get(2).note());
        assertEquals("Розтяжка\nдихання", restored.get(3).note());
        assertEquals("", restored.get(4).note());
    }

    /* Файл із CRLF (як після збереження у Windows) читається так само. */
    @Test
    void crlfFileIsReadTheSame() throws Exception {
        String crlf = Files.readString(CSV_DIR.resolve("records-sample.csv"), StandardCharsets.UTF_8)
                .replace("\r\n", "\n").replace("\n", "\r\n");
        // Перенесення всередині лапок також стало CRLF, тому нотатка відрізняється лише ним.
        List<TrainingRecord> restored = TrainingCsvStore.parse(crlf).all();

        assertEquals(6, restored.size());
        assertEquals("Розтяжка\r\nдихання", restored.get(3).note());
    }

    /* Порожній набір: файл лише із заголовком і зовсім порожній файл дають порожнє сховище. */
    @Test
    void emptyAndHeaderOnlyFilesGiveEmptyRepository(@TempDir Path dir) throws Exception {
        assertTrue(TrainingCsvStore.load(CSV_DIR.resolve("header-only.csv")).all().isEmpty());
        assertTrue(TrainingCsvStore.load(CSV_DIR.resolve("empty.csv")).all().isEmpty());

        Path file = dir.resolve("none.csv");
        TrainingCsvStore.save(file, new Repository<>());
        assertEquals(golden("header-only.csv"), Files.readString(file, StandardCharsets.UTF_8));
    }

    /* Хибна кількість полів: повідомлення з номером запису. */
    @Test
    void wrongFieldCountIsReported() {
        DataStorageException error = assertThrows(DataStorageException.class,
                () -> TrainingCsvStore.load(CSV_DIR.resolve("broken-field-count.csv")));

        assertEquals("Запис 2: очікується 6 полів, отримано 5", error.getMessage());
    }

    /* Хибне число, enum і дата: DataStorageException із номером запису та причиною. */
    @Test
    void badNumberEnumAndDateAreReportedWithCause() {
        String header = "калорії,дата,дистанція,тривалість,вид,нотатка\n";

        DataStorageException number = assertThrows(DataStorageException.class,
                () -> TrainingCsvStore.parse(header + "abc,2026-09-01,7.5,45,RUNNING,\n"));
        assertInstanceOf(NumberFormatException.class, number.getCause());

        DataStorageException kind = assertThrows(DataStorageException.class,
                () -> TrainingCsvStore.parse(header + "520,2026-09-01,7.5,45,DARTS,\n"));
        assertTrue(kind.getMessage().startsWith("Запис 1: хибне значення"));
        assertInstanceOf(IllegalArgumentException.class, kind.getCause());

        DataStorageException date = assertThrows(DataStorageException.class,
                () -> TrainingCsvStore.parse(header + "520,вчора,7.5,45,RUNNING,\n"));
        assertInstanceOf(java.time.format.DateTimeParseException.class, date.getCause());
    }

    /* Заголовок із чужими стовпцями відхиляється; порядок стовпців у файлі не важливий. */
    @Test
    void headerMismatchIsRejectedButColumnOrderIsFree() throws Exception {
        assertThrows(DataStorageException.class,
                () -> TrainingCsvStore.parse("a,b,c,d,e,f\n1,2,3,4,5,6\n"));
        assertThrows(DataStorageException.class,
                () -> TrainingCsvStore.parse("дата,вид\n2026-09-01,RUNNING\n"));

        Repository<TrainingRecord> reordered = TrainingCsvStore.parse(
                "вид,дата,нотатка,калорії,тривалість,дистанція\nRUNNING,2026-09-01,,520,45,7.5\n");
        assertEquals(List.of(rec("2026-09-01", TrainingKind.RUNNING, 45, 7.5, 520, "")),
                reordered.all());
    }

    /* Файлові помилки: немає файла (NoSuchFileException) і не UTF-8 (MalformedInputException). */
    @Test
    void fileErrorsKeepIoExceptionAsCause(@TempDir Path dir) throws IOException {
        DataStorageException missing = assertThrows(DataStorageException.class,
                () -> TrainingCsvStore.load(dir.resolve("немає.csv")));
        assertInstanceOf(NoSuchFileException.class, missing.getCause());

        Path binary = dir.resolve("binary.csv");
        Files.write(binary, new byte[] {(byte) 0xFF, (byte) 0xFE, (byte) 0xC3, 0x28});
        DataStorageException notUtf8 = assertThrows(DataStorageException.class,
                () -> TrainingCsvStore.load(binary));
        assertInstanceOf(MalformedInputException.class, notUtf8.getCause());
    }

    /* Файл справді UTF-8: кирилиця в заголовку й нотатці кодується двома байтами на символ. */
    @Test
    void fileIsUtf8WithoutBom(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("records.csv");
        TrainingCsvStore.save(file, sample());

        byte[] bytes = Files.readAllBytes(file);
        assertArrayEquals("калорії,".getBytes(StandardCharsets.UTF_8),
                java.util.Arrays.copyOf(bytes, "калорії,".getBytes(StandardCharsets.UTF_8).length));
        assertTrue(bytes[0] != (byte) 0xEF);
    }

    /* Експорт не змінює сховище. */
    @Test
    void saveDoesNotChangeRepository(@TempDir Path dir) throws Exception {
        Repository<TrainingRecord> repository = sample();
        List<TrainingRecord> before = repository.all();

        TrainingCsvStore.save(dir.resolve("r.csv"), repository);

        assertEquals(before, repository.all());
    }
}
