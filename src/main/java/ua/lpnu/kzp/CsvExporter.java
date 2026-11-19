package ua.lpnu.kzp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Універсальний рефлексивний експортер у CSV.
 *
 * <p>Клас не знає жодного предметного типу: він працює з будь-яким класом, поля якого позначено
 * {@link CsvColumn}. Заголовки беруться з анотацій, порядок стовпців — це сортування Java-імен
 * полів (детермінований, на відміну від порядку, який повертає JVM), значення перетворюються через
 * {@link String#valueOf(Object)}. Рядки завершуються символом {@code \n} на всіх ОС, тож файл
 * однаковий усюди; парсер приймає і {@code \r\n}.
 */
public final class CsvExporter {
    private static final String LINE_END = "\n";

    /* Забороняє створення екземплярів службового класу. */
    private CsvExporter() {
    }

    /**
     * Записує анотовані поля об'єктів у CSV-файл (UTF-8, наявний файл повністю перезаписується).
     *
     * <p>Каталог для файла створюється за потреби. Клас передається окремо, щоб заголовок можна було
     * побудувати й для порожнього списку.
     *
     * @param <T> тип записів
     * @param path шлях до CSV-файла
     * @param type клас записів
     * @param records записи для експорту
     * @throws DataStorageException якщо клас без анотованих полів, доступ до поля заборонено або
     *     файл неможливо записати (причина — {@link IOException})
     */
    public static <T> void write(Path path, Class<T> type, List<? extends T> records)
            throws DataStorageException {
        Objects.requireNonNull(path, "Шлях не може бути null");
        String csv = toCsv(type, records);
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, csv, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new DataStorageException("Не вдалося записати CSV: " + path, exception);
        }
    }

    /**
     * Формує CSV-текст: заголовок і по одному рядку на запис.
     *
     * @param <T> тип записів
     * @param type клас записів
     * @param records записи; жоден не може бути {@code null}
     * @return текст CSV, кожен рядок завершується {@code \n}
     * @throws DataStorageException якщо клас без анотованих полів або доступ до поля заборонено
     */
    public static <T> String toCsv(Class<T> type, List<? extends T> records)
            throws DataStorageException {
        Objects.requireNonNull(records, "Список не може бути null");
        List<Field> fields = annotatedFields(type);
        StringBuilder csv = new StringBuilder(
                header(fields).stream().map(CsvExporter::escape)
                        .collect(Collectors.joining(",")));
        csv.append(LINE_END);
        for (T record : records) {
            Objects.requireNonNull(record, "Запис не може бути null");
            List<String> cells = new ArrayList<>(fields.size());
            for (Field field : fields) {
                cells.add(escape(readValue(field, record)));
            }
            csv.append(String.join(",", cells)).append(LINE_END);
        }
        return csv.toString();
    }

    /**
     * Повертає заголовки стовпців класу в порядку експорту.
     *
     * @param type клас записів
     * @return незмінний список назв із {@link CsvColumn}
     * @throws DataStorageException якщо клас без анотованих полів або назви стовпців повторюються
     */
    public static List<String> header(Class<?> type) throws DataStorageException {
        return header(annotatedFields(type));
    }

    /* Будує список заголовків із вже відсортованих полів. */
    private static List<String> header(List<Field> fields) {
        return fields.stream().map(field -> field.getAnnotation(CsvColumn.class).value()).toList();
    }

    /* Збирає анотовані поля класу й предків, сортує за іменем і перевіряє унікальність назв. */
    private static List<Field> annotatedFields(Class<?> type) throws DataStorageException {
        Objects.requireNonNull(type, "Клас не може бути null");
        List<Field> fields = new ArrayList<>();
        // Ланцюжок класів проходить до Object, щоб успадковані анотовані поля теж потрапили.
        for (Class<?> current = type; current != null && current != Object.class;
                current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(CsvColumn.class)) {
                    fields.add(field);
                }
            }
        }
        if (fields.isEmpty()) {
            throw new DataStorageException(
                    "Клас %s не має полів з @CsvColumn".formatted(type.getName()));
        }
        fields.sort(Comparator.comparing(Field::getName));
        Set<String> names = new HashSet<>();
        for (Field field : fields) {
            String name = field.getAnnotation(CsvColumn.class).value();
            if (!names.add(name)) {
                throw new DataStorageException("Назва стовпця повторюється: " + name);
            }
        }
        return fields;
    }

    /* Читає значення поля через рефлексію; помилку доступу перетворює на власний виняток. */
    private static String readValue(Field field, Object record) throws DataStorageException {
        if (!field.trySetAccessible()) {
            throw new DataStorageException(
                    "Немає доступу до поля %s.%s".formatted(
                            field.getDeclaringClass().getName(), field.getName()));
        }
        try {
            Object value = field.get(record);
            return value == null ? "" : String.valueOf(value);
        } catch (IllegalAccessException exception) {
            throw new DataStorageException(
                    "Не вдалося прочитати поле %s".formatted(field.getName()), exception);
        }
    }

    /**
     * Екранує одне значення за правилами CSV.
     *
     * <p>Поле з комою, лапкою або перенесенням рядка береться в подвійні лапки, а лапки всередині
     * подвоюються.
     *
     * @param value текст поля
     * @return текст, готовий до вставлення в CSV
     */
    static String escape(String value) {
        boolean special = value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
        if (!special) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
