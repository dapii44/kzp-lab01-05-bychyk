package ua.lpnu.kzp;

/**
 * Незмінна пара «сумарний час — сумарна дистанція».
 *
 * @param totalMinutes сумарна тривалість у хвилинах, {@code >= 0}
 * @param totalDistanceKm сумарна дистанція в кілометрах, скінченна і {@code >= 0}
 */
public record DurationDistance(long totalMinutes, double totalDistanceKm) {
    /** Перевіряє допустимість підсумкових значень. */
    public DurationDistance {
        if (totalMinutes < 0 || totalDistanceKm < 0 || !Double.isFinite(totalDistanceKm)) {
            throw new IllegalArgumentException("Підсумки не можуть бути від'ємними чи нескінченними");
        }
    }
}
