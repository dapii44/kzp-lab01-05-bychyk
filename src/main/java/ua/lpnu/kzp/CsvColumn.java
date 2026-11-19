package ua.lpnu.kzp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Зберігає назву CSV-стовпця для поля.
 *
 * <p>{@code RUNTIME} залишає анотацію доступною під час виконання, тому рефлексивний експортер
 * може її прочитати; {@code FIELD} обмежує застосування полями. Анотація лише описує контракт і
 * сама нічого не експортує. Порядок стовпців задає не анотація, а сортування Java-імен полів.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn {
    /**
     * Повертає заголовок CSV-стовпця.
     *
     * @return непорожній заголовок
     */
    String value();
}
