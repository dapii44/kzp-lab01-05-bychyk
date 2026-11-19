package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CsvParserTest {
    /* Перевіряє простий документ: два записи по три поля. */
    @Test
    void parsesPlainRecords() throws DataStorageException {
        assertEquals(List.of(List.of("a", "b", "c"), List.of("1", "2", "3")),
                CsvParser.parseDocument("a,b,c\n1,2,3\n"));
    }

    /* Перевіряє, що кома, лапки й перенесення рядка всередині лапок лишаються у полі. */
    @Test
    void parsesQuotedCommaQuoteAndNewline() throws DataStorageException {
        List<List<String>> rows = CsvParser.parseDocument(
                "a,\"x, y\",\"he said \"\"hi\"\"\",\"line1\nline2\"\n");

        assertEquals(1, rows.size());
        assertEquals(List.of("a", "x, y", "he said \"hi\"", "line1\nline2"), rows.get(0));
    }

    /* Перевіряє різні роздільники записів і відсутність зайвого запису після останнього. */
    @Test
    void acceptsLfCrLfAndCrAndTrailingNewline() throws DataStorageException {
        List<List<String>> expected = List.of(List.of("a", "b"), List.of("c", "d"));

        assertEquals(expected, CsvParser.parseDocument("a,b\nc,d"));
        assertEquals(expected, CsvParser.parseDocument("a,b\r\nc,d\r\n"));
        assertEquals(expected, CsvParser.parseDocument("a,b\rc,d\r"));
    }

    /* Перевіряє порожні поля: на початку, всередині, наприкінці й порожній документ. */
    @Test
    void parsesEmptyFieldsAndEmptyDocument() throws DataStorageException {
        assertEquals(List.of(List.of("", "x", "")), CsvParser.parseDocument(",x,\n"));
        assertEquals(List.of(List.of("a", "")), CsvParser.parseDocument("a,"));
        assertEquals(List.of(List.of("")), CsvParser.parseDocument("\"\"\n"));
        assertTrue(CsvParser.parseDocument("").isEmpty());
    }

    /* Перевіряє помилки формату: незакрита лапка, лапка в полі, сторонній символ після лапки. */
    @Test
    void rejectsMalformedQuoting() {
        DataStorageException open = assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("a,\"abc\n"));
        assertEquals("Лапка не закрита до кінця документа", open.getMessage());
        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument("ab\"c,d\n"));
        DataStorageException after = assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("\"ab\"c,d\n"));
        assertEquals("Символ після закритої лапки (рядок 1)", after.getMessage());
    }

    /* Перевіряє, що номер рядка в помилці враховує перенесення всередині лапок. */
    @Test
    void errorLineCountsNewlinesInsideQuotes() {
        DataStorageException error = assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("\"a\nb\"x\n"));

        assertEquals("Символ після закритої лапки (рядок 2)", error.getMessage());
    }

    /* Перевіряє перетворення на відображення за заголовком. */
    @Test
    void toRecordsMapsColumnsByHeader() throws DataStorageException {
        List<Map<String, String>> records = CsvParser.toRecords(
                CsvParser.parseDocument("x,y\n1,2\n3,4\n"));

        assertEquals(List.of(Map.of("x", "1", "y", "2"), Map.of("x", "3", "y", "4")), records);
        assertTrue(CsvParser.toRecords(List.of()).isEmpty());
        assertTrue(CsvParser.toRecords(CsvParser.parseDocument("x,y\n")).isEmpty());
    }

    /* Перевіряє хибну кількість полів і повторені заголовки. */
    @Test
    void toRecordsRejectsWrongFieldCountAndDuplicateHeaders() throws DataStorageException {
        List<List<String>> wrongCount = CsvParser.parseDocument("x,y\n1\n");
        DataStorageException count = assertThrows(DataStorageException.class,
                () -> CsvParser.toRecords(wrongCount));
        assertEquals("Запис 1: очікується 2 полів, отримано 1", count.getMessage());

        List<List<String>> duplicate = CsvParser.parseDocument("x,x\n1,2\n");
        assertThrows(DataStorageException.class, () -> CsvParser.toRecords(duplicate));
    }

    /* Перевіряє null. */
    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> CsvParser.parseDocument(null));
        assertThrows(NullPointerException.class, () -> CsvParser.toRecords(null));
    }
}
