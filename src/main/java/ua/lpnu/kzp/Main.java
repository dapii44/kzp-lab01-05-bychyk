package ua.lpnu.kzp;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/** Консольна програма: читає тренування з CSV, перевіряє їх і формує звіт. */
public final class Main {
    /** Код завершення, якщо у вхідних даних немає жодного коректного запису. */
    static final int EXIT_NO_VALID_RECORDS = 1;
    /** Код завершення для хибних аргументів і помилок файлової системи. */
    static final int EXIT_ERROR = 2;

    private static final String USAGE =
            "Використання: java -jar lab01.jar [--help] [--version] [--details] [--input <файл>] [--output <файл>]";
    private static final Path DEFAULT_INPUT = Path.of("data", "input.csv");
    private static final Path DEFAULT_OUTPUT = Path.of("out", "report.txt");

    /* Забороняє створення екземплярів службового класу. */
    private Main() {
    }

    /**
     * Точка входу до програми.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        // Явне UTF-8 для консолі: стандартне кодування Windows може не містити кирилицю.
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(System.err, true, StandardCharsets.UTF_8);
        int exitCode = run(args, out, err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /**
     * Виконує програму й повертає код завершення; відокремлено від main для тестування.
     *
     * @param args аргументи командного рядка
     * @param out потік звичайного виводу
     * @param err потік повідомлень про помилки
     * @return 0 за успіху, 1 без коректних записів, 2 за помилки аргументів або файлів
     */
    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length == 1 && "--version".equals(args[0])) {
            out.println(BuildInfo.version());
            return 0;
        }

        CliOptions options;
        try {
            options = CliOptions.parse(args, DEFAULT_INPUT, DEFAULT_OUTPUT);
        } catch (IllegalArgumentException exception) {
            err.println(exception.getMessage());
            err.println(USAGE);
            return EXIT_ERROR;
        }
        if (options.help()) {
            out.println(USAGE);
            return 0;
        }

        List<String> lines;
        try {
            lines = FileReport.readLines(options.input());
        } catch (IOException exception) {
            err.printf("Не вдалося прочитати %s: %s%n", options.input(), exception.getMessage());
            return EXIT_ERROR;
        }

        // Порядок обробки однаковий для всіх рядків: прочитати, перевірити, обчислити.
        TrainingStats stats = new TrainingStats();
        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            stats.accept(lineNumber, lines.get(lineNumber - 1));
        }

        String report = ReportFormatter.format(stats);
        if (options.details()) {
            report += ReportFormatter.formatDetails(stats.trainings());
        }
        out.print(report);
        try {
            FileReport.writeReport(options.output(), report);
        } catch (IOException exception) {
            err.printf("Не вдалося записати %s: %s%n", options.output(), exception.getMessage());
            return EXIT_ERROR;
        }

        if (stats.validCount() == 0) {
            err.println("У вхідному файлі немає жодного коректного запису");
            return EXIT_NO_VALID_RECORDS;
        }
        return 0;
    }
}
