package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Типобезпечне сховище об'єктів у пам'яті.
 *
 * <p>Параметр типу {@code T} підставляє компілятор: {@code Repository<TrainingRecord>} приймає лише
 * {@code TrainingRecord}, без приведень і raw types. Внутрішній список недоступний ззовні.
 *
 * @param <T> тип записів
 */
public final class Repository<T> {
    private final List<T> items = new ArrayList<>();

    /**
     * Додає непорожній запис до сховища.
     *
     * @param item запис
     * @throws NullPointerException якщо {@code item} дорівнює {@code null}
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "Запис не може бути null"));
    }

    /**
     * Додає всі записи зі списку в порядку списку.
     *
     * @param newItems записи; жоден не може бути {@code null}
     * @throws NullPointerException якщо список або елемент дорівнює {@code null}
     */
    public void addAll(List<? extends T> newItems) {
        Objects.requireNonNull(newItems, "Список не може бути null");
        newItems.forEach(this::add);
    }

    /**
     * Повертає незмінний знімок усіх записів у порядку додавання.
     *
     * <p>Подальші зміни сховища не впливають на вже отриманий знімок.
     *
     * @return незмінний список
     */
    public List<T> all() {
        return List.copyOf(items);
    }

    /**
     * Повертає записи, що задовольняють умову.
     *
     * <p>{@code ? super T} дозволяє передати умову, яка приймає {@code T} або його супертип.
     *
     * @param condition умова відбору
     * @return незмінний список у порядку додавання
     * @throws NullPointerException якщо умова дорівнює {@code null}
     */
    public List<T> find(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "Умова не може бути null");
        return items.stream().filter(condition).toList();
    }

    /**
     * Повертає кількість записів.
     *
     * @return кількість записів
     */
    public int size() {
        return items.size();
    }
}
