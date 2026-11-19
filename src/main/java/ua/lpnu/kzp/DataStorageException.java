package ua.lpnu.kzp;

/**
 * Повідомляє про помилку зберігання або обробки даних (файл, формат CSV, значення, рефлексія).
 *
 * <p>Виняток перевірюваний (checked): виклик, що може завершитися помилкою зберігання, зобов'язаний
 * її обробити або передати далі з контекстом. Першопричину ({@code IOException},
 * {@code NumberFormatException}, помилку рефлексії) зберігає {@link #getCause()}.
 */
public class DataStorageException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Створює виняток із повідомленням.
     *
     * @param message пояснення операції, що не вдалася
     */
    public DataStorageException(String message) {
        super(message);
    }

    /**
     * Створює виняток із повідомленням і першопричиною.
     *
     * @param message пояснення операції, що не вдалася
     * @param cause початкова технічна помилка
     */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
