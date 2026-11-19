package ua.lpnu.kzp;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * П'ять потокових запитів і Optional-пошук для варіанта 2 (лабораторна робота № 4).
 *
 * <p>Кожен метод будує <em>новий</em> конвеєр {@code stream()} над вхідним списком і не змінює його:
 * джерело залишається незмінним, а результати повертаються як нові незмінні колекції або
 * значення. Жоден метод не має ручного циклу й не перевіряє конкретний підтип {@link Training}.
 *
 * <p>Відхилення від таблиці варіантів: у моделі цього проєкту (з лабораторної № 1) немає пульсу,
 * тому статистика обчислюється за калоріями.
 */
public final class TrainingQueries {
    /** Поріг тривалості фільтра: тренування «не менше 45 хв» залишаються. */
    public static final int MIN_MINUTES = 45;

    /** Кількість елементів у запиті top-N за домовленістю методички. */
    public static final int TOP_N = 5;

    /* Забороняє створення екземплярів службового класу. */
    private TrainingQueries() {
    }

    /**
     * Запит 1 (filter): залишає тренування тривалістю не менше {@link #MIN_MINUTES} хвилин.
     *
     * @param trainings тренування будь-яких підтипів
     * @return незмінний список у початковому порядку
     */
    public static List<Training> longTrainings(List<? extends Training> trainings) {
        Objects.requireNonNull(trainings, "Список не може бути null");
        return trainings.stream()
                .filter(training -> training.getMinutes() >= MIN_MINUTES)
                .map(Training.class::cast)
                .toList();
    }

    /**
     * Запит 2 (map): перетворює тренування на їхні дистанції.
     *
     * @param trainings тренування
     * @return незмінний список дистанцій у кілометрах у початковому порядку
     */
    public static List<Double> distances(List<? extends Training> trainings) {
        Objects.requireNonNull(trainings, "Список не може бути null");
        return trainings.stream()
                .map(Training::getDistanceKm)
                .toList();
    }

    /**
     * Запит 3 (groupingBy): сумарна дистанція за видом тренування.
     *
     * <p>Ключ має явний тип {@link TrainingKind}; використано {@link EnumMap}, тому порядок
     * ключів стабільний і збігається з порядком оголошення констант. Види без тренувань у
     * результат не потрапляють.
     *
     * @param trainings тренування
     * @return мапа «вид → сума кілометрів»; порожня для порожнього входу
     */
    public static Map<TrainingKind, Double> distanceByKind(List<? extends Training> trainings) {
        Objects.requireNonNull(trainings, "Список не може бути null");
        return trainings.stream()
                .collect(Collectors.groupingBy(
                        Training::getTrainingKind,
                        () -> new EnumMap<>(TrainingKind.class),
                        Collectors.summingDouble(Training::getDistanceKm)));
    }

    /**
     * Запит 4 (summarizingInt): зведена статистика калорій — кількість, мінімум, максимум, сума,
     * середнє.
     *
     * @param trainings тренування
     * @return статистика; для порожнього входу {@code count = 0}
     */
    public static IntSummaryStatistics caloriesStatistics(List<? extends Training> trainings) {
        Objects.requireNonNull(trainings, "Список не може бути null");
        return trainings.stream()
                .collect(Collectors.summarizingInt(Training::getCalories));
    }

    /**
     * Запит 5 (sorted + limit): перші {@code n} тренувань за дистанцією (спадання), за рівної
     * дистанції — за датою (зростання).
     *
     * <p>Порядок операцій: компаратор → {@code sorted} → {@code limit}. Сортування стабільне, тому
     * записи з однаковою дистанцією й датою лишаються у початковому порядку.
     *
     * @param trainings тренування
     * @param n скільки елементів повернути; {@code 0} — порожній результат, завеликий — усі
     * @return незмінний список довжиною {@code min(n, size)}
     * @throws IllegalArgumentException якщо {@code n < 0}
     */
    public static List<Training> topByDistance(List<? extends Training> trainings, int n) {
        Objects.requireNonNull(trainings, "Список не може бути null");
        if (n < 0) {
            throw new IllegalArgumentException("N не може бути від'ємним: " + n);
        }
        Comparator<Training> byDistanceDescThenDate =
                Comparator.comparingDouble(Training::getDistanceKm).reversed()
                        .thenComparing(Training::getLocalDate);
        return trainings.stream()
                .map(Training.class::cast)
                .sorted(byDistanceDescThenDate)
                .limit(n)
                .toList();
    }

    /**
     * Пошук за датою: повертає <em>перше</em> тренування з цією датою у початковому порядку.
     *
     * @param trainings тренування
     * @param date шукана дата
     * @return {@link Optional} із першим збігом або порожній, якщо тренування немає
     */
    public static Optional<Training> findByDate(List<? extends Training> trainings,
            LocalDate date) {
        Objects.requireNonNull(trainings, "Список не може бути null");
        Objects.requireNonNull(date, "Дата не може бути null");
        return trainings.stream()
                .map(Training.class::cast)
                .filter(training -> training.getLocalDate().equals(date))
                .findFirst();
    }
}
