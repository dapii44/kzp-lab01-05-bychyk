package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RepositoryTest {
    /* Перевіряє додавання й повернення записів у порядку додавання. */
    @Test
    void addStoresItemsInOrder() {
        Repository<String> repository = new Repository<>();
        repository.add("a");
        repository.add("b");

        assertEquals(List.of("a", "b"), repository.all());
        assertEquals(2, repository.size());
    }

    /* Перевіряє, що all() — незмінний знімок: ні зміна знімка, ні подальше додавання не діють. */
    @Test
    void allReturnsImmutableSnapshot() {
        Repository<String> repository = new Repository<>();
        repository.add("a");
        List<String> snapshot = repository.all();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("x"));
        repository.add("b");

        assertEquals(List.of("a"), snapshot);
        assertEquals(List.of("a", "b"), repository.all());
    }

    /* Перевіряє пошук за предикатом, зокрема предикат над супертипом (? super T). */
    @Test
    void findUsesPredicateIncludingSuperTypePredicate() {
        Repository<Integer> repository = new Repository<>();
        repository.addAll(List.of(1, 2, 3, 4));
        java.util.function.Predicate<Number> even = number -> number.intValue() % 2 == 0;

        assertEquals(List.of(2, 4), repository.find(even));
        assertEquals(List.of(), repository.find(value -> value > 10));
    }

    /* Перевіряє порожнє сховище й відхилення null. */
    @Test
    void emptyRepositoryAndNullChecks() {
        Repository<String> repository = new Repository<>();

        assertTrue(repository.all().isEmpty());
        assertEquals(0, repository.size());
        assertThrows(NullPointerException.class, () -> repository.add(null));
        assertThrows(NullPointerException.class, () -> repository.addAll(null));
        assertThrows(NullPointerException.class, () -> repository.find(null));
    }
}
