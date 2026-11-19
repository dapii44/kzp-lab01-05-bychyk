package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileReportTest {
    /* Перевіряє запис і читання українського тексту в UTF-8 разом зі створенням каталогу. */
    @Test
    void writesAndReadsUtf8(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("out").resolve("report.txt");

        FileReport.writeReport(file, "Їжак\nЄнот\n");

        assertEquals(List.of("Їжак", "Єнот"), FileReport.readLines(file));
    }

    /* Перевіряє, що читання відсутнього файла завершується IOException. */
    @Test
    void readingMissingFileFails(@TempDir Path dir) {
        assertThrows(IOException.class, () -> FileReport.readLines(dir.resolve("missing.csv")));
    }
}
