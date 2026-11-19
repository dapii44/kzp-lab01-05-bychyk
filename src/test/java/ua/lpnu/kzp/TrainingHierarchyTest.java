package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TrainingHierarchyTest {
    /* Перевіряє, що фабрика обирає підтип за видом, а базовий тип не створюється напряму. */
    @Test
    void factoryChoosesSubtypeByKind() {
        assertInstanceOf(OutdoorTraining.class, Training.create("2026-09-01", "Біг", 45, 7.5, 520));
        assertInstanceOf(OutdoorTraining.class, Training.create("2026-09-03", "Велосипед", 90, 28.4, 780));
        assertInstanceOf(OutdoorTraining.class, Training.create("2026-09-09", "Ходьба", 70, 5.6, 300));
        assertInstanceOf(IndoorTraining.class, Training.create("2026-09-05", "Плавання", 60, 2.0, 450));
        assertInstanceOf(IndoorTraining.class, Training.create("2026-09-07", "Йога", 50, 0.0, 180));
        assertInstanceOf(IndoorTraining.class, Training.create("2026-09-08", "Силове", 40, 0.0, 250));
        assertTrue(java.lang.reflect.Modifier.isAbstract(Training.class.getModifiers()));
    }

    /* Перевіряє, що fromCsv повертає той самий підтип, що й фабрика. */
    @Test
    void fromCsvReturnsSubtype() {
        assertInstanceOf(OutdoorTraining.class, Training.fromCsv("2026-09-01;Біг;45;7.50;520"));
        assertInstanceOf(IndoorTraining.class, Training.fromCsv("2026-09-07;Йога;50;0.00;180"));
    }

    /* Перевіряє, що підтипи мають різні формули навантаження для спільного методу. */
    @Test
    void subtypesCalculateLoadDifferently() {
        Training outdoor = Training.create("2026-09-01", "Біг", 45, 7.5, 520);
        Training indoor = Training.create("2026-09-05", "Плавання", 60, 2.0, 450);

        // біг: 45*1.2 + 7.5*5 + 520*0.05 = 117.5; плавання: 60*1.1 + 450*0.05 = 88.5
        assertEquals(117.5, outdoor.load(), 0.0001);
        assertEquals(88.5, indoor.load(), 0.0001);
        assertNotEquals(outdoor.load(), indoor.load());
    }

    /* Перевіряє, що дистанція не впливає на навантаження в приміщенні, а на повітрі — впливає. */
    @Test
    void distanceMattersOnlyOutdoors() {
        IndoorTraining shortSwim = new IndoorTraining("2026-09-05", TrainingKind.SWIMMING, 60, 1.0, 450);
        IndoorTraining longSwim = new IndoorTraining("2026-09-05", TrainingKind.SWIMMING, 60, 3.0, 450);
        OutdoorTraining shortRun = new OutdoorTraining("2026-09-01", TrainingKind.RUNNING, 45, 5.0, 520);
        OutdoorTraining longRun = new OutdoorTraining("2026-09-01", TrainingKind.RUNNING, 45, 10.0, 520);

        assertEquals(shortSwim.load(), longSwim.load(), 0.0001);
        assertEquals(25.0, longRun.load() - shortRun.load(), 0.0001);
    }

    /* Перевіряє поліморфну обробку списку без перевірки підтипу. */
    @Test
    void polymorphicCollectionIsProcessedThroughBaseType() {
        List<Training> trainings = List.of(
                Training.create("2026-09-01", "Біг", 45, 7.5, 520),
                Training.create("2026-09-03", "Велосипед", 90, 28.4, 780),
                Training.create("2026-09-05", "Плавання", 60, 2.0, 450),
                Training.create("2026-09-07", "Йога", 50, 0.0, 180),
                Training.create("2026-09-09", "Ходьба", 70, 5.6, 300));

        assertEquals(596.0, TrainingAnalyzer.totalLoad(trainings), 0.0001);
        assertEquals(0.0, TrainingAnalyzer.totalLoad(List.of()), 0.0001);
    }

    /* Перевіряє правила підтипів: вид має відповідати підтипу. */
    @Test
    void subtypesRejectForeignKinds() {
        assertThrows(IllegalArgumentException.class,
                () -> new IndoorTraining("2026-09-01", TrainingKind.RUNNING, 45, 7.5, 520));
        assertThrows(IllegalArgumentException.class,
                () -> new OutdoorTraining("2026-09-05", TrainingKind.SWIMMING, 60, 2.0, 450));
    }

    /* Перевіряє, що спільні інваріанти базового типу діють у кожному підтипі. */
    @Test
    void baseInvariantsApplyToEverySubtype() {
        assertThrows(NullPointerException.class,
                () -> new OutdoorTraining(null, TrainingKind.RUNNING, 1, 1.0, 1));
        assertThrows(NullPointerException.class,
                () -> new IndoorTraining("2026-09-05", null, 1, 1.0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new IndoorTraining(" ", TrainingKind.YOGA, 1, 1.0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new OutdoorTraining("2026-09-01", TrainingKind.RUNNING, -1, 1.0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new OutdoorTraining("2026-09-01", TrainingKind.RUNNING, 1, Double.NaN, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new IndoorTraining("2026-09-05", TrainingKind.YOGA, 1, 1.0, -5));
    }

    /* Перевіряє узгодженість equals і hashCode для логічно рівних об'єктів. */
    @Test
    void equalObjectsHaveEqualHashCodes() {
        Training first = Training.fromCsv("2026-09-01;Біг;45;7.50;520");
        Training second = Training.fromCsv(" 2026-09-01 ; біг ; 45 ; 7.5 ; 520 ");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(first, first);
        assertNotEquals(first, null);
        assertNotEquals(first, "рядок");
    }

    /* Перевіряє, що кожне з п'яти полів впливає на рівність. */
    @Test
    void everyFieldParticipatesInEquality() {
        Training base = Training.create("2026-09-01", "Біг", 45, 7.5, 520);

        assertNotEquals(base, Training.create("2026-09-02", "Біг", 45, 7.5, 520));
        assertNotEquals(base, Training.create("2026-09-01", "Ходьба", 45, 7.5, 520));
        assertNotEquals(base, Training.create("2026-09-01", "Біг", 46, 7.5, 520));
        assertNotEquals(base, Training.create("2026-09-01", "Біг", 45, 7.6, 520));
        assertNotEquals(base, Training.create("2026-09-01", "Біг", 45, 7.5, 521));
    }

    /* Перевіряє, що HashSet не дублює логічно рівні об'єкти. */
    @Test
    void hashSetDoesNotDuplicateEqualTrainings() {
        Set<Training> unique = new HashSet<>();
        unique.add(Training.fromCsv("2026-09-01;Біг;45;7.50;520"));
        unique.add(Training.fromCsv("2026-09-01;Біг;45;7.50;520"));
        unique.add(Training.fromCsv("2026-09-05;Плавання;60;2.00;450"));

        assertEquals(2, unique.size());
    }

    /* Перевіряє використання тренування як ключа HashMap. */
    @Test
    void trainingWorksAsHashMapKey() {
        Map<Training, String> notes = new HashMap<>();
        notes.put(Training.fromCsv("2026-09-01;Біг;45;7.50;520"), "ранок");

        assertEquals("ранок", notes.get(Training.fromCsv("2026-09-01;Біг;45;7.50;520")));
    }

    /* Перевіряє, що різні підтипи з однаковими полями не рівні (порівняння за getClass). */
    @Test
    void differentSubtypesWithSameFieldsAreNotEqual() {
        Training outdoor = new OutdoorTraining("2026-09-01", TrainingKind.RUNNING, 45, 7.5, 520);
        Training imposter = new Training("2026-09-01", TrainingKind.RUNNING, 45, 7.5, 520) {
            @Override
            public double load() {
                return outdoor.load();
            }
        };

        assertNotEquals(outdoor, imposter);
        assertNotEquals(imposter, outdoor);
    }

    /* Перевіряє геттери enum-виду та текстового підпису. */
    @Test
    void kindAccessorsAreConsistent() {
        Training training = Training.create("2026-09-01", "біг", 45, 7.5, 520);

        assertEquals(TrainingKind.RUNNING, training.getTrainingKind());
        assertEquals("Біг", training.getKind());
        assertEquals("2026-09-01, Біг: 45 хв, 7.50 км, 520 ккал", training.toString());
    }
}
