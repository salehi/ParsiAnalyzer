package ir.ac.sbu.parsi.resource;

import ir.ac.sbu.parsi.tokenfilter.PersianNormalizer;
import org.apache.lucene.analysis.CharArraySet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Loads every Persian data file bundled in the plugin jar. No word list, suffix,
 * character mapping or regex lives in Java source — this class is the single
 * place that turns the data files under {@code data/} into runtime structures.
 */
public final class ParsiResources {

    private static final String BASE = "/ir/ac/sbu/parsi/data/";

    public static final String STOPWORDS = "stopwords.txt";
    public static final String SUFFIXES = "suffixes.txt";
    public static final String NORMALIZATION = "normalization.txt";
    public static final String DELETE_CHARS = "delete-chars.txt";
    public static final String ZWNJ_PATTERNS = "zwnj-patterns.txt";
    public static final String LEMMAS = "lemmas.tsv";

    private ParsiResources() {
    }

    /** Lines with inline {@code #} comments stripped and whitespace trimmed; blanks dropped. */
    public static List<String> dataLines(String file) {
        List<String> out = new ArrayList<>();
        for (String raw : rawLines(file)) {
            int hash = raw.indexOf('#');
            String line = (hash >= 0 ? raw.substring(0, hash) : raw).trim();
            if (!line.isEmpty()) {
                out.add(line);
            }
        }
        return out;
    }

    /** Full lines, verbatim (internal spaces preserved); blank and whole-line {@code #} comments dropped. */
    public static List<String> rawLines(String file) {
        List<String> out = new ArrayList<>();
        try (InputStream in = open(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String stripped = line.stripLeading();
                if (stripped.isEmpty() || stripped.startsWith("#")) {
                    continue;
                }
                out.add(line);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed reading bundled resource " + file, e);
        }
        return out;
    }

    public static List<String> stopWords() {
        return dataLines(STOPWORDS);
    }

    public static CharArraySet stopWordSet() {
        List<String> words = stopWords();
        CharArraySet set = new CharArraySet(words.size(), true);
        set.addAll(words);
        return CharArraySet.unmodifiableSet(set);
    }

    public static List<String> suffixes() {
        return dataLines(SUFFIXES);
    }

    /** Builds the data-driven normalizer from {@code normalization.txt} + {@code delete-chars.txt}. */
    public static PersianNormalizer normalizer() {
        Map<Character, Character> mapping = new HashMap<>();
        for (String line : dataLines(NORMALIZATION)) {
            String[] parts = line.split("\\s+");
            char from = (char) Integer.parseInt(parts[0], 16);
            char to = (char) Integer.parseInt(parts[1], 16);
            mapping.put(from, to);
        }
        Set<Character> deletions = new HashSet<>();
        for (String line : dataLines(DELETE_CHARS)) {
            deletions.add((char) Integer.parseInt(line.split("\\s+")[0], 16));
        }
        return new PersianNormalizer(mapping, deletions);
    }

    /** Joins the ZWNJ regex rules into a single alternation pattern. */
    public static Pattern zwnjPattern() {
        List<String> patterns = rawLines(ZWNJ_PATTERNS);
        return Pattern.compile(String.join("|", patterns));
    }

    /** Surface form -> lemma map from {@code lemmas.tsv} (tab separated). */
    public static Map<String, String> lemmas() {
        Map<String, String> map = new LinkedHashMap<>();
        for (String line : rawLines(LEMMAS)) {
            int tab = line.indexOf('\t');
            if (tab <= 0) {
                continue;
            }
            String surface = line.substring(0, tab).trim();
            String lemma = line.substring(tab + 1).trim();
            if (!surface.isEmpty() && !lemma.isEmpty()) {
                map.put(surface, lemma);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    private static InputStream open(String file) {
        InputStream in = ParsiResources.class.getResourceAsStream(BASE + file);
        if (in == null) {
            throw new IllegalStateException("Missing bundled resource " + BASE + file);
        }
        return in;
    }
}
