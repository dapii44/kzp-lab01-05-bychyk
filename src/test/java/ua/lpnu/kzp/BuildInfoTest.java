package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BuildInfoTest {
    /* Перевіряє формат версії разом із номером збірки. */
    @Test
    void versionContainsProjectVersionAndBuildNumber() {
        String version = BuildInfo.version();

        assertTrue(version.matches("\\d+\\.\\d+\\.\\d+\\+\\w+"),
                "Неочікуваний формат версії: %s".formatted(version));
    }

    /* Перевіряє, що Maven підставив значення, а не залишив шаблон. */
    @Test
    void placeholdersAreFiltered() {
        assertFalse(BuildInfo.version().contains("${"));
    }
}
