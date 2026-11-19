package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {
    private final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);
    private final PrintStream err = new PrintStream(errBytes, true, StandardCharsets.UTF_8);

    /* Повертає текст із нормалізованими роздільниками рядків для порівняння на всіх ОС. */
    private static String normalize(String text) {
        return text.replace("\r\n", "\n");
    }

    /* Smoke-test підтверджує підключення JUnit до збірки. */
    @Test
    void sampleCalculationIsCorrect() {
        assertEquals(4, 2 + 2);
    }

    /* Перевіряє режим --help. */
    @Test
    void helpPrintsUsage() {
        int code = Main.run(new String[] {"--help"}, out, err);

        assertEquals(0, code);
        assertTrue(outBytes.toString(StandardCharsets.UTF_8).contains("Використання:"));
    }

    /* Перевіряє, що --version повертає версію й номер збірки. */
    @Test
    void versionPrintsVersionAndBuild() {
        int code = Main.run(new String[] {"--version"}, out, err);

        assertEquals(0, code);
        String version = outBytes.toString(StandardCharsets.UTF_8).strip();
        assertTrue(version.matches("\\d+\\.\\d+\\.\\d+\\+\\w+"),
                "Неочікуваний формат версії: " + version);
    }

    /* Перевіряє повний прогін на контрольних даних і збіг звіту з еталоном. */
    @Test
    void controlDataProducesGoldenReport(@TempDir Path dir) throws IOException {
        Path output = dir.resolve("nested").resolve("report.txt");

        int code = Main.run(new String[] {"--input", "data/input.csv", "--output",
            output.toString()}, out, err);

        String golden = Files.readString(Path.of("src", "test", "resources", "golden",
                "report-lab01.txt"), StandardCharsets.UTF_8);
        assertEquals(0, code);
        assertEquals(normalize(golden), normalize(outBytes.toString(StandardCharsets.UTF_8)));
        assertEquals(normalize(golden),
                normalize(Files.readString(output, StandardCharsets.UTF_8)));
    }

    /* Перевіряє режим --details: базовий звіт не змінюється, додається блок навантаження. */
    @Test
    void detailsModeAppendsLoadBlockAndKeepsBaseReport(@TempDir Path dir) throws IOException {
        Path output = dir.resolve("report.txt");

        int code = Main.run(new String[] {"--details", "--input", "data/input.csv", "--output",
            output.toString()}, out, err);

        String base = Files.readString(Path.of("src", "test", "resources", "golden",
                "report-lab01.txt"), StandardCharsets.UTF_8);
        String details = Files.readString(Path.of("src", "test", "resources", "golden",
                "report-lab03-details.txt"), StandardCharsets.UTF_8);
        String actual = normalize(outBytes.toString(StandardCharsets.UTF_8));
        assertEquals(0, code);
        assertTrue(actual.startsWith(normalize(base)));
        assertEquals(normalize(details), actual.substring(normalize(base).length()));
        assertEquals(actual, normalize(Files.readString(output, StandardCharsets.UTF_8)));
    }

    /* Перевіряє режим --queries на наборі з 8 записів: звіт збігається з еталоном побайтово. */
    @Test
    void queriesModeMatchesGoldenReport(@TempDir Path dir) throws IOException {
        Path output = dir.resolve("report.txt");

        int code = Main.run(new String[] {"--queries", "--input", "data/queries-sample.csv",
            "--output", output.toString()}, out, err);

        String golden = Files.readString(Path.of("src", "test", "resources", "golden",
                "report-lab04-sample.txt"), StandardCharsets.UTF_8);
        assertEquals(0, code);
        assertEquals(normalize(golden), normalize(outBytes.toString(StandardCharsets.UTF_8)));
        assertEquals(normalize(golden), normalize(Files.readString(output,
                StandardCharsets.UTF_8)));
    }

    /* Перевіряє --find-date: відсутня дата дає «не знайдено», а звіт лишається успішним. */
    @Test
    void findDateOptionReportsMissingDate(@TempDir Path dir) {
        int code = Main.run(new String[] {"--queries", "--find-date", "2026-12-31", "--input",
            "data/queries-sample.csv", "--output", dir.resolve("r.txt").toString()}, out, err);

        assertEquals(0, code);
        assertTrue(outBytes.toString(StandardCharsets.UTF_8)
                .contains("Пошук за датою 2026-12-31: не знайдено"));
    }

    /* Перевіряє код 2 для хибної дати та для --find-date без значення. */
    @Test
    void findDateOptionRejectsBadValue() {
        assertEquals(2, Main.run(new String[] {"--find-date", "завтра"}, out, err));
        assertTrue(errBytes.toString(StandardCharsets.UTF_8).contains("Хибна дата"));
        assertEquals(2, Main.run(new String[] {"--find-date"}, out, err));
    }

    /* Перевіряє --export: файл створено, round-trip підтверджено, базовий звіт не змінено. */
    @Test
    void exportOptionWritesCsvAndConfirmsRoundTrip(@TempDir Path dir) throws IOException {
        Path csv = dir.resolve("export").resolve("trainings.csv");

        int code = Main.run(new String[] {"--export", csv.toString(), "--input",
            "data/queries-sample.csv", "--output", dir.resolve("r.txt").toString()}, out, err);

        List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
        assertEquals(0, code);
        assertEquals(9, lines.size());
        assertEquals("калорії,дата,дистанція,тривалість,вид,нотатка", lines.get(0));
        assertTrue(outBytes.toString(StandardCharsets.UTF_8)
                .contains("Експортовано записів: 8"));
        assertTrue(outBytes.toString(StandardCharsets.UTF_8).contains("round-trip: збігається"));
    }

    /* Перевіряє --export у недоступне місце: код 2, повідомлення про помилку запису. */
    @Test
    void exportToBlockedPathReturnsError(@TempDir Path dir) throws IOException {
        Path blocker = Files.writeString(dir.resolve("blocker"), "файл");

        int code = Main.run(new String[] {"--export", blocker.resolve("x.csv").toString(),
            "--input", "data/input.csv", "--output", dir.resolve("r.txt").toString()}, out, err);

        assertEquals(2, code);
        assertTrue(errBytes.toString(StandardCharsets.UTF_8).contains("Не вдалося записати CSV"));
    }

    /* Перевіряє код 2 і повідомлення для відсутнього вхідного файла. */
    @Test
    void missingInputFileGivesErrorCode(@TempDir Path dir) {
        int code = Main.run(new String[] {"--input", dir.resolve("none.csv").toString()},
                out, err);

        assertEquals(Main.EXIT_ERROR, code);
        assertTrue(errBytes.toString(StandardCharsets.UTF_8).contains("Не вдалося прочитати"));
    }

    /* Перевіряє код 1 для файла без жодного коректного запису. */
    @Test
    void fileWithoutValidRecordsGivesCodeOne(@TempDir Path dir) throws IOException {
        Path input = dir.resolve("bad.csv");
        Files.writeString(input, "сміття\n;;;;\n", StandardCharsets.UTF_8);

        int code = Main.run(new String[] {"--input", input.toString(), "--output",
            dir.resolve("r.txt").toString()}, out, err);

        assertEquals(Main.EXIT_NO_VALID_RECORDS, code);
        assertTrue(outBytes.toString(StandardCharsets.UTF_8).contains("Коректних записів: 0"));
    }

    /* Перевіряє порожній файл: звіт створюється, код завершення 1. */
    @Test
    void emptyFileGivesCodeOne(@TempDir Path dir) throws IOException {
        Path input = dir.resolve("empty.csv");
        Files.writeString(input, "", StandardCharsets.UTF_8);

        int code = Main.run(new String[] {"--input", input.toString(), "--output",
            dir.resolve("r.txt").toString()}, out, err);

        assertEquals(Main.EXIT_NO_VALID_RECORDS, code);
        assertTrue(Files.exists(dir.resolve("r.txt")));
    }

    /* Перевіряє невідомий аргумент і параметр без значення. */
    @Test
    void invalidArgumentsGiveErrorCode() {
        assertEquals(Main.EXIT_ERROR, Main.run(new String[] {"--oops"}, out, err));
        assertEquals(Main.EXIT_ERROR, Main.run(new String[] {"--input"}, out, err));
        assertTrue(errBytes.toString(StandardCharsets.UTF_8).contains("Використання:"));
    }

    /* Перевіряє код 2, якщо звіт неможливо записати (шлях збігається з каталогом). */
    @Test
    void unwritableOutputGivesErrorCode(@TempDir Path dir) {
        int code = Main.run(new String[] {"--input", "data/input.csv", "--output",
            dir.toString()}, out, err);

        assertEquals(Main.EXIT_ERROR, code);
        assertTrue(errBytes.toString(StandardCharsets.UTF_8).contains("Не вдалося записати"));
    }
}
