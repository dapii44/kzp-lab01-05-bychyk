package ua.lpnu.kzp;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Objects;

/**
 * Запис про тренування для збереження у CSV (лабораторна робота № 5).
 *
 * <p>Поля незмінні й позначені {@link CsvColumn}; експортер знаходить їх рефлексією. Клас сам
 * відповідає за власну валідацію (конструктор) і розбір рядка CSV ({@link #fromColumns}). Ключ
 * відновлення в звіті — пара {@code date + kind}.
 *
 * <p>Уточнення варіанта: замість пульсу зберігаються калорії (так у моделі з лабораторної № 1),
 * а необов'язкове текстове поле {@code note} додано, щоб вільний текст міг містити кому, лапки й
 * перенесення рядка.
 */
public final class TrainingRecord {
    @CsvColumn("дата")
    private final LocalDate date;

    @CsvColumn("вид")
    private final TrainingKind kind;

    @CsvColumn("тривалість")
    private final int durationMinutes;

    @CsvColumn("дистанція")
    private final double distanceKm;

    @CsvColumn("калорії")
    private final int calories;

    @CsvColumn("нотатка")
    private final String note;

    /**
     * Створює запис після перевірки інваріантів.
     *
     * @param date дата тренування
     * @param kind вид тренування
     * @param durationMinutes тривалість, {@code >= 0}
     * @param distanceKm дистанція, скінченна і {@code >= 0}
     * @param calories калорії, {@code >= 0}
     * @param note нотатка; порожній рядок допускається, {@code null} — ні
     * @throws NullPointerException якщо {@code date}, {@code kind} або {@code note} дорівнює
     *     {@code null}
     * @throws IllegalArgumentException якщо числове значення від'ємне чи нескінченне
     */
    public TrainingRecord(LocalDate date, TrainingKind kind, int durationMinutes,
            double distanceKm, int calories, String note) {
        this.date = Objects.requireNonNull(date, "Дата не може бути null");
        this.kind = Objects.requireNonNull(kind, "Вид не може бути null");
        if (durationMinutes < 0 || calories < 0 || distanceKm < 0) {
            throw new IllegalArgumentException("від'ємне числове значення");
        }
        if (!Double.isFinite(distanceKm)) {
            throw new IllegalArgumentException("поле distanceKm має бути скінченним числом");
        }
        this.durationMinutes = durationMinutes;
        this.distanceKm = distanceKm;
        this.calories = calories;
        this.note = Objects.requireNonNull(note, "Нотатка не може бути null");
    }

    /**
     * Створює запис зі звичайного тренування (нотатка порожня).
     *
     * @param training тренування з основної моделі
     * @return відповідний запис для збереження
     */
    public static TrainingRecord of(Training training) {
        return new TrainingRecord(training.getLocalDate(), training.getTrainingKind(),
                training.getMinutes(), training.getDistanceKm(), training.getCalories(), "");
    }

    /**
     * Відновлює запис зі значень стовпців одного запису CSV.
     *
     * <p>Предметний розбір на відміну від універсального експортера: знає типи полів. Кожна
     * помилка перетворюється на {@link DataStorageException} з номером запису, назвою стовпця й
     * першопричиною.
     *
     * @param recordNumber номер запису (від 1) для повідомлень
     * @param columns відображення «назва стовпця → текст»
     * @return відновлений запис
     * @throws DataStorageException якщо стовпця немає, число, дата чи вид хибні або порушено
     *     інваріант
     */
    public static TrainingRecord fromColumns(int recordNumber, Map<String, String> columns)
            throws DataStorageException {
        String prefix = "Запис " + recordNumber + ": ";
        try {
            return new TrainingRecord(
                    LocalDate.parse(required(prefix, columns, "дата")),
                    TrainingKind.valueOf(required(prefix, columns, "вид")),
                    Integer.parseInt(required(prefix, columns, "тривалість")),
                    Double.parseDouble(required(prefix, columns, "дистанція")),
                    Integer.parseInt(required(prefix, columns, "калорії")),
                    required(prefix, columns, "нотатка"));
        } catch (NumberFormatException | DateTimeParseException exception) {
            throw new DataStorageException(prefix + "хибне число або дата", exception);
        } catch (IllegalArgumentException exception) {
            // Сюди потрапляють і невідомий вид (valueOf), і порушення інваріантів конструктора.
            throw new DataStorageException(
                    prefix + "хибне значення: " + exception.getMessage(), exception);
        }
    }

    /* Повертає значення стовпця або повідомляє, що стовпця немає. */
    private static String required(String prefix, Map<String, String> columns, String name)
            throws DataStorageException {
        String value = columns.get(name);
        if (value == null) {
            throw new DataStorageException(prefix + "відсутній стовпець «" + name + "»");
        }
        return value;
    }

    /**
     * Повертає дату тренування.
     *
     * @return дата
     */
    public LocalDate date() {
        return date;
    }

    /**
     * Повертає вид тренування.
     *
     * @return константа enum
     */
    public TrainingKind kind() {
        return kind;
    }

    /**
     * Повертає тривалість.
     *
     * @return хвилини
     */
    public int durationMinutes() {
        return durationMinutes;
    }

    /**
     * Повертає дистанцію.
     *
     * @return кілометри
     */
    public double distanceKm() {
        return distanceKm;
    }

    /**
     * Повертає калорії.
     *
     * @return калорії
     */
    public int calories() {
        return calories;
    }

    /**
     * Повертає нотатку.
     *
     * @return текст, можливо порожній
     */
    public String note() {
        return note;
    }

    /**
     * Перетворює запис на тренування основної моделі (нотатка не переноситься).
     *
     * @return {@link OutdoorTraining} або {@link IndoorTraining} за видом
     */
    public Training toTraining() {
        return Training.create(date.toString(), kind.label(), durationMinutes, distanceKm,
                calories);
    }

    /**
     * Порівнює записи за всіма шістьма полями.
     *
     * @param other інший об'єкт
     * @return {@code true}, якщо усі поля рівні ({@code Double.compare} для дистанції)
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        TrainingRecord record = (TrainingRecord) other;
        return durationMinutes == record.durationMinutes
                && calories == record.calories
                && Double.compare(distanceKm, record.distanceKm) == 0
                && date.equals(record.date)
                && kind == record.kind
                && note.equals(record.note);
    }

    /**
     * Повертає хеш-код за тими самими полями.
     *
     * @return хеш-код, узгоджений з {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return Objects.hash(date, kind, durationMinutes, distanceKm, calories, note);
    }

    /**
     * Повертає текстове подання для діагностики.
     *
     * @return рядок із полями запису
     */
    @Override
    public String toString() {
        return "TrainingRecord[date=%s, kind=%s, durationMinutes=%d, distanceKm=%s, calories=%d, note=%s]"
                .formatted(date, kind, durationMinutes, distanceKm, calories, note);
    }
}
