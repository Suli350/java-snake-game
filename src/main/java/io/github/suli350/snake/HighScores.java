package io.github.suli350.snake;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Top-10 score table saved as "score;name;date" lines. */
public class HighScores {

    public static final int MAX_ENTRIES = 10;

    public static final class Entry {
        public final int score;
        public final String name;
        public final LocalDate date;

        public Entry(int score, String name, LocalDate date) {
            this.score = score;
            this.name = name;
            this.date = date;
        }
    }

    private final Path file;
    private final List<Entry> entries = new ArrayList<>();

    public HighScores(Path file) {
        this.file = file;
        load();
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] p = line.split(";", 3);
                if (p.length == 3) {
                    try {
                        entries.add(new Entry(Integer.parseInt(p[0]), p[1], LocalDate.parse(p[2])));
                    } catch (RuntimeException ignored) {
                        // skip broken lines rather than losing the whole table
                    }
                }
            }
            sortAndTrim();
        } catch (IOException ignored) {
            // no high scores is not fatal
        }
    }

    public boolean qualifies(int score) {
        return score > 0 && (entries.size() < MAX_ENTRIES || score > entries.get(entries.size() - 1).score);
    }

    /** Adds the score if it makes the table; returns its 1-based rank or -1. */
    public int add(int score, String name, LocalDate date) {
        if (!qualifies(score)) {
            return -1;
        }
        String clean = name == null || name.isBlank() ? "Player" : name.replace(";", " ").trim();
        Entry e = new Entry(score, clean.length() > 16 ? clean.substring(0, 16) : clean, date);
        entries.add(e);
        sortAndTrim();
        save();
        return entries.indexOf(e) + 1;
    }

    public int best() {
        return entries.isEmpty() ? 0 : entries.get(0).score;
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    private void sortAndTrim() {
        entries.sort(Comparator.comparingInt((Entry e) -> e.score).reversed());
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Entry e : entries) {
            lines.add(e.score + ";" + e.name + ";" + e.date);
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // game keeps working if the disk is read-only
        }
    }
}
