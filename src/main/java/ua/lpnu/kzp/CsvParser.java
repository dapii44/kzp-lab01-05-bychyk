package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Документний парсер CSV на основі скінченного автомата.
 *
 * <p>Читає весь текст і розрізняє коми та перенесення рядка всередині лапок і поза ними, тому
 * повертає список <em>записів</em>, а не список фізичних рядків. {@code split(",")} для цього не
 * годиться: він розрізав би поле в лапках.
 */
public final class CsvParser {
    /** Стани автомата розбору. */
    private enum State { FIELD_START, UNQUOTED, QUOTED, AFTER_QUOTE }

    /* Забороняє створення екземплярів службового класу. */
    private CsvParser() {
    }

    /**
     * Розбирає CSV-текст на записи й поля.
     *
     * <p>Роздільники записів — {@code \n}, {@code \r\n} і {@code \r} поза лапками; завершальний
     * роздільник не створює порожнього запису; порожній текст дає порожній список.
     *
     * @param text повний текст документа
     * @return записи, кожен — список полів
     * @throws DataStorageException якщо лапка не закрита, лапка стоїть усередині поля без лапок або
     *     після закритої лапки йде сторонній символ
     */
    public static List<List<String>> parseDocument(String text) throws DataStorageException {
        Objects.requireNonNull(text, "Текст не може бути null");
        List<List<String>> records = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        State state = State.FIELD_START;
        int line = 1;
        int length = text.length();
        for (int index = 0; index < length; index++) {
            char ch = text.charAt(index);
            switch (state) {
                case FIELD_START, UNQUOTED -> {
                    if (ch == ',') {
                        row.add(field.toString());
                        field.setLength(0);
                        state = State.FIELD_START;
                    } else if (ch == '\n' || ch == '\r') {
                        row.add(field.toString());
                        field.setLength(0);
                        records.add(row);
                        row = new ArrayList<>();
                        state = State.FIELD_START;
                        line++;
                        if (ch == '\r' && index + 1 < length && text.charAt(index + 1) == '\n') {
                            index++;
                        }
                    } else if (ch == '"') {
                        if (state == State.UNQUOTED) {
                            throw new DataStorageException(
                                    "Лапка всередині поля без лапок (рядок %d)".formatted(line));
                        }
                        state = State.QUOTED;
                    } else {
                        field.append(ch);
                        state = State.UNQUOTED;
                    }
                }
                case QUOTED -> {
                    if (ch == '"') {
                        if (index + 1 < length && text.charAt(index + 1) == '"') {
                            field.append('"');
                            index++;
                        } else {
                            state = State.AFTER_QUOTE;
                        }
                    } else {
                        if (ch == '\n') {
                            line++;
                        }
                        field.append(ch);
                    }
                }
                case AFTER_QUOTE -> {
                    if (ch == ',') {
                        row.add(field.toString());
                        field.setLength(0);
                        state = State.FIELD_START;
                    } else if (ch == '\n' || ch == '\r') {
                        row.add(field.toString());
                        field.setLength(0);
                        records.add(row);
                        row = new ArrayList<>();
                        state = State.FIELD_START;
                        line++;
                        if (ch == '\r' && index + 1 < length && text.charAt(index + 1) == '\n') {
                            index++;
                        }
                    } else {
                        throw new DataStorageException(
                                "Символ після закритої лапки (рядок %d)".formatted(line));
                    }
                }
                default -> throw new IllegalStateException("Невідомий стан: " + state);
            }
        }
        if (state == State.QUOTED) {
            throw new DataStorageException("Лапка не закрита до кінця документа");
        }
        if (!row.isEmpty() || state != State.FIELD_START) {
            row.add(field.toString());
            records.add(row);
        }
        return List.copyOf(records);
    }

    /**
     * Перетворює записи документа на відображення «назва стовпця → значення» за першим рядком.
     *
     * <p>Порожній документ (без заголовка) дає порожній результат; документ лише із заголовком —
     * також порожній (допустимі різні випадки, див. README).
     *
     * @param rows результат {@link #parseDocument(String)}
     * @return список відображень у порядку записів
     * @throws DataStorageException якщо заголовки повторюються або кількість полів запису не
     *     збігається з кількістю заголовків
     */
    public static List<Map<String, String>> toRecords(List<List<String>> rows)
            throws DataStorageException {
        Objects.requireNonNull(rows, "Записи не можуть бути null");
        if (rows.isEmpty()) {
            return List.of();
        }
        List<String> header = rows.get(0);
        if (header.stream().distinct().count() != header.size()) {
            throw new DataStorageException("Заголовки стовпців повторюються: " + header);
        }
        List<Map<String, String>> result = new ArrayList<>();
        for (int number = 1; number < rows.size(); number++) {
            List<String> row = rows.get(number);
            if (row.size() != header.size()) {
                throw new DataStorageException(
                        "Запис %d: очікується %d полів, отримано %d".formatted(
                                number, header.size(), row.size()));
            }
            Map<String, String> record = new LinkedHashMap<>();
            for (int column = 0; column < header.size(); column++) {
                record.put(header.get(column), row.get(column));
            }
            result.add(record);
        }
        return List.copyOf(result);
    }
}
