package ua.lpnu.kzp;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/** Зберігає розібрані аргументи командного рядка. */
public final class CliOptions {
    /** Дата Optional-пошуку за замовчуванням (одна з дат контрольних даних). */
    public static final LocalDate DEFAULT_FIND_DATE = LocalDate.of(2026, 9, 5);

    private final boolean help;
    private final boolean details;
    private final boolean queries;
    private final LocalDate findDate;
    private final Path export;
    private final Path input;
    private final Path output;

    /* Створює набір параметрів; використовується лише методом parse. */
    private CliOptions(boolean help, boolean details, boolean queries, LocalDate findDate,
            Path export, Path input, Path output) {
        this.help = help;
        this.details = details;
        this.queries = queries;
        this.findDate = findDate;
        this.export = export;
        this.input = input;
        this.output = output;
    }

    /**
     * Розбирає аргументи командного рядка.
     *
     * @param args аргументи, передані в main
     * @param defaultInput шлях до входу за замовчуванням
     * @param defaultOutput шлях до звіту за замовчуванням
     * @return розібрані параметри
     * @throws IllegalArgumentException якщо аргумент невідомий або не має значення
     */
    public static CliOptions parse(String[] args, Path defaultInput, Path defaultOutput) {
        boolean help = false;
        boolean details = false;
        boolean queries = false;
        LocalDate findDate = DEFAULT_FIND_DATE;
        Path export = null;
        Path input = defaultInput;
        Path output = defaultOutput;
        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            switch (argument) {
                case "--help" -> help = true;
                case "--details" -> details = true;
                case "--queries" -> queries = true;
                case "--find-date" -> {
                    index++;
                    findDate = parseDate(valueAt(args, index, argument));
                }
                case "--export" -> {
                    index++;
                    export = Path.of(valueAt(args, index, argument));
                }
                case "--input" -> {
                    index++;
                    input = Path.of(valueAt(args, index, argument));
                }
                case "--output" -> {
                    index++;
                    output = Path.of(valueAt(args, index, argument));
                }
                default -> throw new IllegalArgumentException("Невідомий аргумент: " + argument);
            }
        }
        return new CliOptions(help, details, queries, findDate, export, input, output);
    }

    /* Перетворює текст на дату ISO або повідомляє про помилку формату. */
    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Хибна дата для --find-date: " + text, exception);
        }
    }

    /* Повертає значення параметра або повідомляє, що його немає. */
    private static String valueAt(String[] args, int index, String name) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Для %s потрібне значення".formatted(name));
        }
        return args[index];
    }

    /**
     * Повертає ознаку режиму {@code --help}.
     *
     * @return {@code true}, якщо потрібно показати довідку
     */
    public boolean help() {
        return help;
    }

    /**
     * Повертає ознаку режиму {@code --details} (додатковий блок звіту).
     *
     * @return {@code true}, якщо потрібно додати блок із навантаженням
     */
    public boolean details() {
        return details;
    }

    /**
     * Повертає ознаку режиму {@code --queries} (блок результатів потокових запитів).
     *
     * @return {@code true}, якщо потрібно додати блок запитів
     */
    public boolean queries() {
        return queries;
    }

    /**
     * Повертає дату для Optional-пошуку.
     *
     * @return значення {@code --find-date} або {@link #DEFAULT_FIND_DATE}
     */
    public LocalDate findDate() {
        return findDate;
    }

    /**
     * Повертає шлях для експорту записів у CSV.
     *
     * @return шлях із {@code --export} або порожній {@link Optional}
     */
    public Optional<Path> export() {
        return Optional.ofNullable(export);
    }

    /**
     * Повертає шлях до вхідного файла.
     *
     * @return шлях до CSV
     */
    public Path input() {
        return input;
    }

    /**
     * Повертає шлях до файла звіту.
     *
     * @return шлях до звіту
     */
    public Path output() {
        return output;
    }
}
