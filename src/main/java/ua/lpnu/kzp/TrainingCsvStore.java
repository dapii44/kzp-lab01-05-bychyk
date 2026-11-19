package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Збереження й відновлення записів про тренування у CSV (UTF-8).
 *
 * <p>Запис виконує універсальний {@link CsvExporter}, читання — документний {@link CsvParser} і
 * предметний {@link TrainingRecord#fromColumns}. Порожній файл (0 байтів) і файл лише із
 * заголовком — різні, але допустимі випадки: обидва дають порожнє сховище.
 */
public final class TrainingCsvStore {
    /* Забороняє створення екземплярів службового класу. */
    private TrainingCsvStore() {
    }

    /**
     * Записує всі записи сховища у CSV-файл.
     *
     * @param path шлях до файла
     * @param repository сховище записів
     * @throws DataStorageException якщо файл неможливо записати
     */
    public static void save(Path path, Repository<TrainingRecord> repository)
            throws DataStorageException {
        Objects.requireNonNull(repository, "Сховище не може бути null");
        CsvExporter.write(path, TrainingRecord.class, repository.all());
    }

    /**
     * Читає CSV-файл і відновлює записи.
     *
     * @param path шлях до файла
     * @return нове сховище з відновленими записами в порядку файла
     * @throws DataStorageException якщо файл недоступний або не UTF-8 (причина — {@code
     *     IOException}), заголовок не збігається зі стовпцями {@link TrainingRecord}, кількість
     *     полів запису хибна або значення некоректне
     */
    public static Repository<TrainingRecord> load(Path path) throws DataStorageException {
        Objects.requireNonNull(path, "Шлях не може бути null");
        String text;
        try {
            text = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DataStorageException("Не вдалося прочитати CSV: " + path, exception);
        }
        return parse(text);
    }

    /**
     * Відновлює записи з тексту CSV.
     *
     * @param text повний текст документа
     * @return нове сховище з відновленими записами
     * @throws DataStorageException якщо формат, заголовок або значення некоректні
     */
    public static Repository<TrainingRecord> parse(String text) throws DataStorageException {
        List<List<String>> rows = CsvParser.parseDocument(text);
        Repository<TrainingRecord> repository = new Repository<>();
        if (rows.isEmpty()) {
            return repository;
        }
        List<String> expected = CsvExporter.header(TrainingRecord.class);
        if (rows.get(0).size() != expected.size()
                || !new HashSet<>(rows.get(0)).equals(new HashSet<>(expected))) {
            throw new DataStorageException(
                    "Заголовок %s не відповідає очікуваному %s".formatted(rows.get(0), expected));
        }
        List<Map<String, String>> records = CsvParser.toRecords(rows);
        for (int index = 0; index < records.size(); index++) {
            repository.add(TrainingRecord.fromColumns(index + 1, records.get(index)));
        }
        return repository;
    }
}
