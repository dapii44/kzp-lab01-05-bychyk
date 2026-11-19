package ua.lpnu.kzp;

import java.nio.file.Path;

/** Зберігає розібрані аргументи командного рядка. */
public final class CliOptions {
    private final boolean help;
    private final Path input;
    private final Path output;

    /* Створює набір параметрів; використовується лише методом parse. */
    private CliOptions(boolean help, Path input, Path output) {
        this.help = help;
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
        Path input = defaultInput;
        Path output = defaultOutput;
        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            switch (argument) {
                case "--help" -> help = true;
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
        return new CliOptions(help, input, output);
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
