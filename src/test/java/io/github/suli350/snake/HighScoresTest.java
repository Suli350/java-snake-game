package io.github.suli350.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class HighScoresTest {

    private static final LocalDate DAY = LocalDate.of(2019, 8, 3);

    @Test
    void keepsTopTenSortedAndPersists() throws IOException {
        Path file = Files.createTempDirectory("snake").resolve("scores.txt");
        HighScores scores = new HighScores(file);
        for (int i = 1; i <= 12; i++) {
            scores.add(i * 10, "p" + i, DAY);
        }
        assertEquals(10, scores.getEntries().size());
        assertEquals(120, scores.best());
        assertFalse(scores.qualifies(20));
        assertTrue(scores.qualifies(35));
        assertEquals(1, scores.add(500, "champ;ion", DAY));

        HighScores reloaded = new HighScores(file);
        assertEquals(500, reloaded.best());
        assertEquals("champ ion", reloaded.getEntries().get(0).name);
    }

    @Test
    void zeroNeverQualifies() throws IOException {
        HighScores scores = new HighScores(Files.createTempDirectory("snake").resolve("s.txt"));
        assertEquals(-1, scores.add(0, "x", DAY));
    }
}
