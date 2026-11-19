package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Перевіряє експортер на довільних класах, які не мають стосунку до предметної області. */
class CsvExporterTest {
    /** Довільний клас: поля відсортуються як code, ignored(без анотації — пропускається), title. */
    static final class Gadget {
        @CsvColumn("Назва")
        private final String title;
        @CsvColumn("Код")
        private final int code;
        private final String ignored = "не експортується";

        Gadget(String title, int code) {
            this.title = title;
            this.code = code;
        }
    }

    /** Предок з анотованим полем: перевіряє обхід ланцюжка класів. */
    static class Base {
        @CsvColumn("Ід")
        private final int id = 7;
    }

    /** Нащадок: успадковане поле теж експортується. */
    static final class Child extends Base {
        @CsvColumn("Мітка")
        private final String label = "x";
    }

    /** Клас без анотованих полів. */
    static final class Plain {
        private final int value = 1;
    }

    /** Клас із повторюваною назвою стовпця. */
    static final class Twin {
        @CsvColumn("А")
        private final int first = 1;
        @CsvColumn("А")
        private final int second = 2;
    }

    /** Клас зі значенням null у полі. */
    static final class Nullable {
        @CsvColumn("Текст")
        private final String text = null;
    }

    /* Перевіряє заголовок і порядок стовпців: за іменами Java-полів, назви — з анотації. */
    @Test
    void headerFollowsSortedJavaFieldNamesAndAnnotationNames() throws DataStorageException {
        assertEquals(List.of("Код", "Назва"), CsvExporter.header(Gadget.class));
        assertEquals("Код,Назва\n5,Лампа\n",
                CsvExporter.toCsv(Gadget.class, List.of(new Gadget("Лампа", 5))));
    }

    /* Перевіряє екранування коми, лапок і перенесення рядка. */
    @Test
    void escapesCommaQuoteAndNewline() throws DataStorageException {
        String csv = CsvExporter.toCsv(Gadget.class, List.of(
                new Gadget("a,b", 1), new Gadget("він сказав \"так\"", 2),
                new Gadget("рядок1\nрядок2", 3)));

        assertEquals("Код,Назва\n1,\"a,b\"\n2,\"він сказав \"\"так\"\"\"\n"
                + "3,\"рядок1\nрядок2\"\n", csv);
    }

    /* Перевіряє порожній список: лише заголовок. */
    @Test
    void emptyListProducesHeaderOnly() throws DataStorageException {
        assertEquals("Код,Назва\n", CsvExporter.toCsv(Gadget.class, List.of()));
    }

    /* Перевіряє, що експортер читає успадковані анотовані поля й null дає порожній рядок. */
    @Test
    void walksSuperclassChainAndWritesNullAsEmpty() throws DataStorageException {
        assertEquals("Ід,Мітка\n7,x\n", CsvExporter.toCsv(Child.class, List.of(new Child())));
        assertEquals("Текст\n\n", CsvExporter.toCsv(Nullable.class, List.of(new Nullable())));
    }

    /* Перевіряє помилки рівня рефлексії: немає анотацій, повторювана назва, null-запис. */
    @Test
    void rejectsClassWithoutColumnsDuplicateNamesAndNullRecord() {
        DataStorageException none = assertThrows(DataStorageException.class,
                () -> CsvExporter.toCsv(Plain.class, List.of(new Plain())));
        assertTrue(none.getMessage().contains("не має полів з @CsvColumn"));
        assertThrows(DataStorageException.class, () -> CsvExporter.header(Twin.class));
        assertThrows(NullPointerException.class,
                () -> CsvExporter.toCsv(Gadget.class, java.util.Arrays.asList((Gadget) null)));
    }

    /* Перевіряє запис у файл: UTF-8, \n, створення каталогів і перезапис без залишків. */
    @Test
    void writesUtf8FileCreatesDirectoriesAndTruncates(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("nested").resolve("out.csv");

        CsvExporter.write(file, Gadget.class,
                List.of(new Gadget("перший", 1), new Gadget("другий", 2), new Gadget("третій", 3)));
        CsvExporter.write(file, Gadget.class, List.of(new Gadget("є", 9)));

        byte[] bytes = Files.readAllBytes(file);
        assertArrayEquals("Код,Назва\n9,є\n".getBytes(StandardCharsets.UTF_8), bytes);
    }

    /* Перевіряє файлову помилку: батьківський «каталог» є звичайним файлом; cause — IOException. */
    @Test
    void fileProblemIsWrappedWithIoExceptionCause(@TempDir Path dir) throws IOException {
        Path blocker = Files.writeString(dir.resolve("blocker"), "файл, а не каталог");

        DataStorageException error = assertThrows(DataStorageException.class,
                () -> CsvExporter.write(blocker.resolve("out.csv"), Gadget.class, List.of()));

        assertTrue(error.getMessage().startsWith("Не вдалося записати CSV"));
        assertInstanceOf(IOException.class, error.getCause());
    }

    /* Перевіряє екранування безпосередньо. */
    @Test
    void escapeLeavesPlainTextUntouched() {
        assertEquals("звичайний", CsvExporter.escape("звичайний"));
        assertEquals("\"a\r\nb\"", CsvExporter.escape("a\r\nb"));
        assertEquals("\"\"\"\"", CsvExporter.escape("\""));
    }
}
