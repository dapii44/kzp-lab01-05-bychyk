package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TrainingKindTest {
    /* Перевіряє підписи, ознаку «на повітрі» та додатну інтенсивність кожної константи. */
    @Test
    void everyConstantHasLabelAndPositiveIntensity() {
        for (TrainingKind kind : TrainingKind.values()) {
            assertFalse(kind.label().isBlank());
            assertTrue(kind.intensity() > 0);
        }
        assertEquals(6, TrainingKind.values().length);
    }

    /* Перевіряє розподіл на види на повітрі й у приміщенні. */
    @Test
    void outdoorFlagMatchesDomain() {
        assertTrue(TrainingKind.RUNNING.isOutdoor());
        assertTrue(TrainingKind.CYCLING.isOutdoor());
        assertTrue(TrainingKind.WALKING.isOutdoor());
        assertFalse(TrainingKind.SWIMMING.isOutdoor());
        assertFalse(TrainingKind.YOGA.isOutdoor());
        assertFalse(TrainingKind.STRENGTH.isOutdoor());
    }

    /* Перевіряє пошук за підписом без урахування регістру й пробілів; порівняння через ==. */
    @Test
    void fromLabelIsCaseAndSpaceInsensitive() {
        assertSame(TrainingKind.RUNNING, TrainingKind.fromLabel("Біг"));
        assertSame(TrainingKind.RUNNING, TrainingKind.fromLabel("  біг "));
        assertSame(TrainingKind.SWIMMING, TrainingKind.fromLabel("ПЛАВАННЯ"));
    }

    /* Перевіряє null, порожній текст і невідомий вид. */
    @Test
    void fromLabelRejectsNullBlankAndUnknown() {
        assertThrows(NullPointerException.class, () -> TrainingKind.fromLabel(null));
        assertThrows(IllegalArgumentException.class, () -> TrainingKind.fromLabel("  "));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> TrainingKind.fromLabel("Дартс"));
        assertEquals("невідомий вид тренування: Дартс", error.getMessage());
    }
}
