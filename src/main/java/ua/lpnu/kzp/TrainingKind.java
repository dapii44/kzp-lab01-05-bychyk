package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Фіксована множина видів тренувань.
 *
 * <p>Кожна константа створюється один раз і містить підпис для звіту (CSV зберігає саме його),
 * ознаку «на відкритому повітрі» та коефіцієнт інтенсивності, що використовується в обчисленні
 * навантаження. Значення enum порівнюють через {@code ==}.
 */
public enum TrainingKind {
    /** Біг. */
    RUNNING("Біг", true, 1.2),
    /** Велосипед. */
    CYCLING("Велосипед", true, 1.0),
    /** Ходьба. */
    WALKING("Ходьба", true, 0.6),
    /** Плавання (басейн). */
    SWIMMING("Плавання", false, 1.1),
    /** Йога. */
    YOGA("Йога", false, 0.5),
    /** Силове тренування (зал). */
    STRENGTH("Силове", false, 1.0);

    private final String label;
    private final boolean outdoor;
    private final double intensity;

    /* Конструктор enum використовується JVM для створення фіксованих констант. */
    TrainingKind(String label, boolean outdoor, double intensity) {
        this.label = label;
        this.outdoor = outdoor;
        this.intensity = intensity;
    }

    /**
     * Повертає підпис виду, який записано в CSV і виводиться у звіті.
     *
     * @return український підпис
     */
    public String label() {
        return label;
    }

    /**
     * Показує, чи відбувається тренування на відкритому повітрі.
     *
     * @return {@code true} для бігу, велосипеда й ходьби
     */
    public boolean isOutdoor() {
        return outdoor;
    }

    /**
     * Повертає коефіцієнт інтенсивності (навантаження на одну хвилину).
     *
     * @return додатний коефіцієнт
     */
    public double intensity() {
        return intensity;
    }

    /**
     * Знаходить вид за підписом без урахування регістру й крайніх пробілів.
     *
     * @param text підпис виду, наприклад {@code Біг}
     * @return відповідна константа
     * @throws NullPointerException якщо {@code text} дорівнює {@code null}
     * @throws IllegalArgumentException якщо текст порожній або вид невідомий
     */
    public static TrainingKind fromLabel(String text) {
        Objects.requireNonNull(text, "Вид тренування не може бути null");
        String normalized = text.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("порожня дата або вид тренування");
        }
        for (TrainingKind kind : values()) {
            if (kind.label.equalsIgnoreCase(normalized)) {
                return kind;
            }
        }
        throw new IllegalArgumentException(
                String.format(Locale.ROOT, "невідомий вид тренування: %s", normalized));
    }
}
